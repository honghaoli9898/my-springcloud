package com.seaboxdata.sdps.seaboxProxy.image;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.namenode.FSImageFormatProtobuf;
import org.apache.hadoop.hdfs.server.namenode.FSImageUtil;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto.FileSummary;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto.INodeSection;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto.INodeSection.INode;
import org.apache.hadoop.hdfs.server.namenode.INodeId;
import org.apache.hadoop.hdfs.tools.offlineImageViewer.IgnoreSnapshotException;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.LimitInputStream;
import org.apache.hadoop.util.Time;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
abstract class PBImageTextWriter implements Closeable {

    private String[] stringTable;
    private PrintStream out;
    private MetadataMap metadataMap = null;

    PBImageTextWriter(PrintStream out, String tempPath) throws IOException {
        this.out = out;
        if (tempPath.isEmpty()) {
            metadataMap = new InMemoryMetadataDB();
        } else {
            metadataMap = new LevelDBMetadataMap(tempPath);
        }
    }

    static void ignoreSnapshotName(long inode) throws IOException {
        throw new IgnoreSnapshotException();
    }

    @Override
    public void close() throws IOException {
        out.flush();
        IOUtils.cleanup(null, metadataMap);
    }

    abstract protected String getEntry(String parent, INode inode);

    protected abstract List<String> getEntries(String parent, INode inode);

    abstract protected String getHeader();

    public void visit(RandomAccessFile file) throws IOException {
        Configuration conf = new Configuration();
        if (!FSImageUtil.checkFileFormat(file)) {
            throw new IOException("Unrecognized FSImage");
        }

        FileSummary summary = FSImageUtil.loadSummary(file);

        try (FileInputStream fin = new FileInputStream(file.getFD())) {
            InputStream is;
            ArrayList<FileSummary.Section> sections =
                    Lists.newArrayList(summary.getSectionsList());
            Collections.sort(sections,
                    new Comparator<FileSummary.Section>() {
                        @Override
                        public int compare(FsImageProto.FileSummary.Section s1,
                                           FsImageProto.FileSummary.Section s2) {
                            FSImageFormatProtobuf.SectionName n1 =
                                    FSImageFormatProtobuf.SectionName.fromString(s1.getName());
                            FSImageFormatProtobuf.SectionName n2 =
                                    FSImageFormatProtobuf.SectionName.fromString(s2.getName());
                            if (n1 == null) {
                                return n2 == null ? 0 : -1;
                            } else if (n2 == null) {
                                return -1;
                            } else {
                                return n1.ordinal() - n2.ordinal();
                            }
                        }
                    });

            ImmutableList<Long> refIdList = null;
            for (FileSummary.Section section : sections) {
                fin.getChannel().position(section.getOffset());
                is = FSImageUtil.wrapInputStreamForCompression(conf,
                        summary.getCodec(), new BufferedInputStream(new LimitInputStream(
                                fin, section.getLength())));
                switch (FSImageFormatProtobuf.SectionName.fromString(section.getName())) {
                    case STRING_TABLE:
                        log.info("Loading string table");
                        stringTable = FSImageLoader.loadStringTable(is);
                        break;
                    case INODE_REFERENCE:
                        // Load INodeReference so that all INodes can be processed.
                        // Snapshots are not handled and will just be ignored for now.
                        log.info("Loading inode references");
                        refIdList = FSImageLoader.loadINodeReferenceSection(is);
                        break;
                    default:
                        break;
                }
            }

            loadDirectories(fin, sections, summary, conf);
            loadINodeDirSection(fin, sections, summary, conf, refIdList);
            metadataMap.sync();
            output(conf, summary, fin, sections);
        }
    }

    private void output(Configuration conf, FileSummary summary,
                        FileInputStream fin, ArrayList<FileSummary.Section> sections)
            throws IOException {
        InputStream is;
        long startTime = Time.monotonicNow();
        out.println(getHeader());
        for (FileSummary.Section section : sections) {
            if (FSImageFormatProtobuf.SectionName.fromString(section.getName()) == FSImageFormatProtobuf.SectionName.INODE) {
                fin.getChannel().position(section.getOffset());
                is = FSImageUtil.wrapInputStreamForCompression(conf,
                        summary.getCodec(), new BufferedInputStream(new LimitInputStream(
                                fin, section.getLength())));
                outputINodes(is);
            }
        }
        long timeTaken = Time.monotonicNow() - startTime;
        log.debug("Time to output inodes: {}ms", timeTaken);
    }

    private void outputINodes(InputStream in) throws IOException {
        INodeSection s = INodeSection.parseDelimitedFrom(in);
        log.info("Found {} INodes in the INode section", s.getNumInodes());
        long ignored = 0;
        long ignoredSnapshots = 0;
        for (int i = 0; i < s.getNumInodes(); ++i) {
            INode p = INode.parseDelimitedFrom(in);
            try {
                String parentPath = metadataMap.getParentPath(p.getId());
                out.println(getEntry(parentPath, p));
            } catch (IOException ioe) {
                ignored++;
                if (!(ioe instanceof IgnoreSnapshotException)) {
                    log.warn("Exception caught, ignoring node:{}", p.getId(), ioe);
                } else {
                    ignoredSnapshots++;
                    if (log.isDebugEnabled()) {
                        log.debug("Exception caught, ignoring node:{}.", p.getId(), ioe);
                    }
                }
            }

            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("Outputted {} INodes.", i);
            }
        }
        if (ignored > 0) {
            log.warn("Ignored {} nodes, including {} in snapshots. Please turn on"
                    + " debug log for details", ignored, ignoredSnapshots);
        }
        log.info("Outputted {} INodes.", s.getNumInodes());
    }

    private void loadDirectories(
            FileInputStream fin, List<FileSummary.Section> sections,
            FileSummary summary, Configuration conf)
            throws IOException {
        log.info("Loading directories");
        long startTime = Time.monotonicNow();
        for (FileSummary.Section section : sections) {
            if (FSImageFormatProtobuf.SectionName.fromString(section.getName())
                    == FSImageFormatProtobuf.SectionName.INODE) {
                fin.getChannel().position(section.getOffset());
                InputStream is = FSImageUtil.wrapInputStreamForCompression(conf,
                        summary.getCodec(), new BufferedInputStream(new LimitInputStream(
                                fin, section.getLength())));
                loadDirectoriesInINodeSection(is);
            }
        }
        long timeTaken = Time.monotonicNow() - startTime;
        log.info("Finished loading directories in {}ms", timeTaken);
    }

    private void loadINodeDirSection(
            FileInputStream fin, List<FileSummary.Section> sections,
            FileSummary summary, Configuration conf, List<Long> refIdList)
            throws IOException {
        log.info("Loading INode directory section.");
        long startTime = Time.monotonicNow();
        for (FileSummary.Section section : sections) {
            if (FSImageFormatProtobuf.SectionName.fromString(section.getName())
                    == FSImageFormatProtobuf.SectionName.INODE_DIR) {
                fin.getChannel().position(section.getOffset());
                InputStream is = FSImageUtil.wrapInputStreamForCompression(conf,
                        summary.getCodec(), new BufferedInputStream(
                                new LimitInputStream(fin, section.getLength())));
                buildNamespace(is, refIdList);
            }
        }
        long timeTaken = Time.monotonicNow() - startTime;
        log.info("Finished loading INode directory section in {}ms", timeTaken);
    }

    private void buildNamespace(InputStream in, List<Long> refIdList)
            throws IOException {
        int count = 0;
        while (true) {
            FsImageProto.INodeDirectorySection.DirEntry e =
                    FsImageProto.INodeDirectorySection.DirEntry.parseDelimitedFrom(in);
            if (e == null) {
                break;
            }
            count++;
            if (log.isDebugEnabled() && count % 10000 == 0) {
                log.debug("Scanned {} directories.", count);
            }
            long parentId = e.getParent();
            for (int i = 0; i < e.getChildrenCount(); i++) {
                long childId = e.getChildren(i);
                metadataMap.putDirChild(parentId, childId);
            }
            for (int i = e.getChildrenCount();
                 i < e.getChildrenCount() + e.getRefChildrenCount(); i++) {
                int refId = e.getRefChildren(i - e.getChildrenCount());
                metadataMap.putDirChild(parentId, refIdList.get(refId));
            }
        }
        log.info("Scanned {} INode directories to build namespace.", count);
    }

    private void loadDirectoriesInINodeSection(InputStream in) throws IOException {
        INodeSection s = INodeSection.parseDelimitedFrom(in);
        log.info("Loading directories in INode section.");
        int numDirs = 0;
        for (int i = 0; i < s.getNumInodes(); ++i) {
            INode p = INode.parseDelimitedFrom(in);
            if (log.isDebugEnabled() && i % 10000 == 0) {
                log.debug("Scanned {} inodes.", i);
            }
            if (p.hasDirectory()) {
                metadataMap.putDir(p);
                numDirs++;
            }
        }
        log.info("Found {} directories in INode section.", numDirs);
    }

    private static interface MetadataMap extends Closeable {
        public void putDirChild(long parentId, long childId) throws IOException;

        public void putDir(INode dir) throws IOException;

        public String getParentPath(long inode) throws IOException;

        public void sync() throws IOException;

    }

    private static class InMemoryMetadataDB implements MetadataMap {

        private Map<Long, Dir> dirMap = new HashMap<>();
        private Map<Long, Dir> dirChildMap = new HashMap<>();

        InMemoryMetadataDB() {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void putDirChild(long parentId, long childId) {
            Dir parent = dirMap.get(parentId);
            Dir child = dirMap.get(childId);
            if (child != null) {
                child.setParent(parent);
            }
            Preconditions.checkState(!dirChildMap.containsKey(childId));
            dirChildMap.put(childId, parent);
        }

        @Override
        public void putDir(INode p) {
            Preconditions.checkState(!dirMap.containsKey(p.getId()));
            Dir dir = new Dir(p.getId(), p.getName().toStringUtf8());
            dirMap.put(p.getId(), dir);
        }

        @Override
        public String getParentPath(long inode) throws IOException {
            if (inode == INodeId.ROOT_INODE_ID) {
                return "";
            }
            Dir parent = dirChildMap.get(inode);
            if (parent == null) {
                // The inode is an INodeReference, which is generated from snapshot.
                // For delimited oiv tool, no need to print out metadata in snapshots.
                ignoreSnapshotName(inode);
            }
            return parent.getPath();
        }

        @Override
        public void sync() {
        }

        private static class Dir {
            private final long inode;
            private Dir parent = null;
            private String name;
            private String path = null;  // cached full path of the directory.

            Dir(long inode, String name) {
                this.inode = inode;
                this.name = name;
            }

            private void setParent(Dir parent) {
                Preconditions.checkState(this.parent == null);
                this.parent = parent;
            }

            private String getPath() {
                if (this.parent == null) {
                    return "/";
                }
                if (this.path == null) {
                    this.path = new Path(parent.getPath(), name.isEmpty() ? "/" : name).
                            toString();
                    this.name = null;
                }
                return this.path;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof Dir && inode == ((Dir) o).inode;
            }

            @Override
            public int hashCode() {
                return Long.valueOf(inode).hashCode();
            }
        }
    }

    private static class LevelDBMetadataMap implements MetadataMap {

        private LevelDBStore dirChildMap = null;
        private LevelDBStore dirMap = null;
        private DirPathCache dirPathCache = new DirPathCache();

        LevelDBMetadataMap(String baseDir) throws IOException {
            File dbDir = new File(baseDir);
            if (dbDir.exists()) {
                FileUtils.deleteDirectory(dbDir);
            }
            if (!dbDir.mkdirs()) {
                throw new IOException("Failed to mkdir on " + dbDir);
            }
            try {
                dirChildMap = new LevelDBStore(new File(dbDir, "dirChildMap"));
                dirMap = new LevelDBStore(new File(dbDir, "dirMap"));
            } catch (IOException e) {
                log.error("Failed to open LevelDBs", e);
                IOUtils.cleanup(null, this);
            }
        }

        private static byte[] toBytes(long value) {
            return ByteBuffer.allocate(8).putLong(value).array();
        }

        private static byte[] toBytes(String value)
                throws UnsupportedEncodingException {
            return value.getBytes("UTF-8");
        }

        private static long toLong(byte[] bytes) {
            Preconditions.checkArgument(bytes.length == 8);
            return ByteBuffer.wrap(bytes).getLong();
        }

        private static String toString(byte[] bytes) throws IOException {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            IOUtils.cleanup(null, dirChildMap, dirMap);
            dirChildMap = null;
            dirMap = null;
        }

        @Override
        public void putDirChild(long parentId, long childId) throws IOException {
            dirChildMap.put(toBytes(childId), toBytes(parentId));
        }

        @Override
        public void putDir(INode dir) throws IOException {
            Preconditions.checkArgument(dir.hasDirectory(),
                    "INode %s (%s) is not a directory.", dir.getId(), dir.getName());
            dirMap.put(toBytes(dir.getId()), toBytes(dir.getName().toStringUtf8()));
        }

        @Override
        public String getParentPath(long inode) throws IOException {
            if (inode == INodeId.ROOT_INODE_ID) {
                return "/";
            }
            byte[] bytes = dirChildMap.get(toBytes(inode));
            if (bytes == null) {
                // The inode is an INodeReference, which is generated from snapshot.
                // For delimited oiv tool, no need to print out metadata in snapshots.
                ignoreSnapshotName(inode);
            }
            if (bytes.length != 8) {
                throw new IOException(
                        "bytes array length error. Actual length is " + bytes.length);
            }
            long parent = toLong(bytes);
            if (!dirPathCache.containsKey(parent)) {
                bytes = dirMap.get(toBytes(parent));
                if (parent != INodeId.ROOT_INODE_ID && bytes == null) {
                    // The parent is an INodeReference, which is generated from snapshot.
                    // For delimited oiv tool, no need to print out metadata in snapshots.
                    ignoreSnapshotName(parent);
                }
                String parentName = toString(bytes);
                String parentPath =
                        new Path(getParentPath(parent),
                                parentName.isEmpty() ? "/" : parentName).toString();
                dirPathCache.put(parent, parentPath);
            }
            return dirPathCache.get(parent);
        }

        @Override
        public void sync() throws IOException {
            dirChildMap.sync();
            dirMap.sync();
        }

        private static class LevelDBStore implements Closeable {
            private static final int BATCH_SIZE = 1024;
            private DB db = null;
            private WriteBatch batch = null;
            private int writeCount = 0;

            LevelDBStore(final File dbPath) throws IOException {
                Options options = new Options();
                options.createIfMissing(true);
                options.errorIfExists(true);
                db = JniDBFactory.factory.open(dbPath, options);
                batch = db.createWriteBatch();
            }

            @Override
            public void close() throws IOException {
                if (batch != null) {
                    IOUtils.cleanup(null, batch);
                    batch = null;
                }
                IOUtils.cleanup(null, db);
                db = null;
            }

            public void put(byte[] key, byte[] value) throws IOException {
                batch.put(key, value);
                writeCount++;
                if (writeCount >= BATCH_SIZE) {
                    sync();
                }
            }

            public byte[] get(byte[] key) throws IOException {
                return db.get(key);
            }

            public void sync() throws IOException {
                try {
                    db.write(batch);
                } finally {
                    batch.close();
                    batch = null;
                }
                batch = db.createWriteBatch();
                writeCount = 0;
            }
        }

        private static class DirPathCache extends LinkedHashMap<Long, String> {
            private final static int CAPACITY = 16 * 1024;
            private static final long serialVersionUID = -7514084253373772744L;

            DirPathCache() {
                super(CAPACITY);
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, String> entry) {
                return super.size() > CAPACITY;
            }
        }
    }
}
