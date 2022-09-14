package com.seaboxdata.sdps.seaboxProxy.image;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto.INodeSection.INodeFile;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SeaboxImageTextWriter extends PBImageTextWriter {
    static final String DEFAULT_DELIMITER = "\t";
    private static final String ROOT_PATH = "/";
    private final String delimiter;
    private final boolean fileInflate;

    public SeaboxImageTextWriter(PrintStream out, String delimiter, String tempPath, boolean fileInflate) throws IOException {
        super(out, tempPath);
        this.delimiter = delimiter;
        this.fileInflate = fileInflate;
    }

    @Override
    protected String getEntry(String parent, FsImageProto.INodeSection.INode inode) {
        StringBuffer buffer = new StringBuffer();
        String inodeName = inode.getName().toStringUtf8();
        parent = parent.isEmpty() ? "/" : parent;
        Path path = new Path(parent, inodeName.isEmpty() ? "/" : inodeName);
        String pathStr = path.toString();
        buffer.append(pathStr);
        switch (inode.getType()) {
            case FILE:
                INodeFile file = inode.getFile();
                this.append(buffer, file.getReplication());
                this.append(buffer, file.getModificationTime());
                this.append(buffer, file.getAccessTime());
                this.append(buffer, file.getPreferredBlockSize());
                this.append(buffer, file.getBlocksCount());
                this.append(buffer, FSImageLoader.getFileSize(file));
                this.append(buffer, -1);
                this.append(buffer, -1);
                this.append(buffer, file.getStoragePolicyID());
                break;
            case DIRECTORY:
                FsImageProto.INodeSection.INodeDirectory dir = inode.getDirectory();
                this.append(buffer, 0);
                this.append(buffer, dir.getModificationTime());
                this.append(buffer, dir.getModificationTime());
                this.append(buffer, 0);
                this.append(buffer, 0);
                this.append(buffer, 0);
                this.append(buffer, dir.getNsQuota());
                this.append(buffer, dir.getDsQuota());
                this.append(buffer, 7);
        }
        this.append(buffer, inode.getType().getNumber());
        if (this.fileInflate) {
            this.append(buffer, parent);
        }
        return buffer.toString();
    }

    @Override
    protected List<String> getEntries(String parent, FsImageProto.INodeSection.INode inode) {
        parent = parent.isEmpty() ? "/" : parent;
        List<String> entries = new ArrayList();
        String entry = this.getEntry(parent, inode);
        entries.add(entry);
        if (inode.getType() != FsImageProto.INodeSection.INode.Type.FILE) {
            return entries;
        } else if ("/".equals(parent)) {
            return entries;
        } else {
            String[] splits = parent.split("/");
            StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < splits.length - 1; ++i) {
                if (i == 1) {
                    buffer.append(splits[i]);
                } else {
                    buffer.append("/").append(splits[i]);
                }

                String subPath = buffer.toString();
                String subEntry = StringUtils.substringBeforeLast(entry, this.delimiter);
                subEntry = subEntry + this.delimiter + subPath;
                entries.add(subEntry);
            }

            return entries;
        }
    }

    @Override
    protected String getHeader() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Path");//路径
        this.append(buffer, "Replication");//副本数
        this.append(buffer, "ModificationTime");//修改时间
        this.append(buffer, "AccessTime");//访问时间、存取时间
        this.append(buffer, "PreferredBlockSize");//首选的块大小
        this.append(buffer, "BlocksCount");//块数量
        this.append(buffer, "FileSize");//文件大小
        this.append(buffer, "NSQUOTA");//文件目录数量配额
        this.append(buffer, "DSQUOTA");//目录空间配额
        this.append(buffer, "StoragePolicy");//存储策略
        this.append(buffer, "FileType");//节点类型(文件:1,目录:2,链接:3)
        return buffer.toString();
    }

    private void append(StringBuffer buffer, int field) {
        buffer.append(this.delimiter);
        buffer.append(field);
    }

    private void append(StringBuffer buffer, long field) {
        buffer.append(this.delimiter);
        buffer.append(field);
    }

    private void append(StringBuffer buffer, String field) {
        buffer.append(this.delimiter);
        buffer.append(field);
    }

}
