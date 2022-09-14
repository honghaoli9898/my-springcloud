package com.seaboxdata.sdps.extendAnalysis.merge;

import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.extendAnalysis.common.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;

import java.io.IOException;
import java.util.*;

@Slf4j
public class FileManager {
    /**
     * 默认合并阈值
     */
    public static final Long DEFAULT_MERGE_THRESHOLD = 67108864L;
    /**
     * 默认合并文件阈值
     */
    public static final Long DEFAULT_MERGED_FILE_THRESHOLD = 1342177280L;

    public List<DirPathInfo> getDirPathInfoList(FileSystem fs, String sourcePath, String confCodec, String confFormat, String mergerTempPath) {
        List<DirPathInfo> dirList = new ArrayList();
        Path path = new Path(sourcePath);
        dirList.add(getDirPathInfo(fs, sourcePath, confCodec, confFormat, false,mergerTempPath));
        addSubDirInfo(fs, path, confCodec, confFormat, dirList,mergerTempPath);
        return dirList;
    }

    /**
     * 添加子目录信息
     *
     * @param fs
     * @param path
     * @param confCodec
     * @param confFormat
     * @param dirList
     */
    private void addSubDirInfo(FileSystem fs, Path path, String confCodec, String confFormat, List<DirPathInfo> dirList,String mergerTempPath) {
        try {
            FileStatus[] fileStatuses = fs.listStatus(path);
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.isDirectory()) {
                    Path subPath = fileStatus.getPath();
                    dirList.add(getDirPathInfo(fs, subPath.toString(), confCodec, confFormat, false,mergerTempPath));
                    addSubDirInfo(fs, fileStatus.getPath(), confCodec, confFormat, dirList,mergerTempPath);
                }
            }
        } catch (IOException e) {
            log.error("addSubDirInfo异常:", e);
            System.exit(1);
        }
    }


    public DirPathInfo getDirPathInfo(FileSystem fs, String sourcePath, String confCodec, String confFormat, boolean recursive, String mergerTempPath) {

        DirPathInfo dirPathInfo = new DirPathInfo(sourcePath,mergerTempPath);
        long modifTime = 0L;
        try {
            RemoteIterator<LocatedFileStatus> fileIterator = fs.listFiles(new Path(sourcePath), recursive);
            while (fileIterator.hasNext()) {
                LocatedFileStatus file = (LocatedFileStatus) fileIterator.next();
                //文件大小大于合并阈值
                if (file.getLen() < DEFAULT_MERGE_THRESHOLD) {
                    dirPathInfo.addFile(file);
                }
                //修改时间大于0
                if (file.getModificationTime() > modifTime) {
                    modifTime = file.getModificationTime();
                }
            }
        } catch (Exception e) {
            log.error("遍历目录信息异常:", e);
            System.exit(1);
        }

        //取最大的修改时间
        dirPathInfo.setLastModifTime(modifTime);

        if (CollectionUtils.isEmpty(dirPathInfo.getFilePaths())) {
            log.info("路径:[" + sourcePath + "]没有小文件");
            return dirPathInfo;
        } else {
            log.info("路径:[" + sourcePath + "],获得小文件:[" + dirPathInfo.getFilePaths().size() + "]个");
            FormatType format = FormatType.getByName(confFormat);
            //检测文件类型
            FormatType detectFormat = detectFileType(fs, dirPathInfo);
            //正确格式
            boolean correctFormat = detectFormat != null && (format == null || format == detectFormat);
            Preconditions.checkArgument(correctFormat, "编解码器格式不正确:当前类型=" + format + ", 检测类型=" + detectFormat);
            dirPathInfo.setFormatType(detectFormat);
            log.info("完成获取格式类型:{}", dirPathInfo.getFormatType());

            CodecType codec = CodecType.getTypeByName(confCodec);
            CodecType detectCodec = detectCodecType(fs, dirPathInfo);
            boolean correctCodec = detectCodec != null && (codec == null || codec == detectCodec);
            Preconditions.checkArgument(correctCodec, "编解码器类型不正确 conf=" + codec + ", detect=" + detectCodec);
            dirPathInfo.setCodecType(detectCodec);
            log.info("finish get codec type of = {}", dirPathInfo.getCodecType());
            return dirPathInfo;
        }
    }

    private CodecType detectCodecType(FileSystem fs, DirPathInfo dirPathInfo) {
        List<Path> randomPaths = getRandomPaths(dirPathInfo.getFilePaths());
        CodecType finalCodec = null;
        boolean same = true;
        FormatType format = dirPathInfo.getFormatType();

        for (Path randomPath : randomPaths) {
            CodecType codec;
            switch (format) {
                case AVRO:
                    codec = readAvroCodec(fs, randomPath);
                    break;
                case SEQ:
                    codec = readSeqCodec(fs, randomPath);
                    break;
                case ORC:
                    codec = readORCCodec(fs, randomPath);
                    break;
                case PARQUET:
                    codec = readParquetCodec(fs, randomPath);
                    break;
                case TEXT:
                default:
                    codec = this.detectCodecTypeByExt(dirPathInfo);
            }

            Preconditions.checkNotNull(codec, "解析不到的codecType,目录路径:" + randomPath.toString());
            if (finalCodec == null) {
                finalCodec = codec;
            }
            if (finalCodec.getClass() != codec.getClass()) {
                same = false;
                break;
            }
        }

        Preconditions.checkArgument(same, "文件编解码器类型不相同,目录:" + dirPathInfo.getDirPath());
        return finalCodec;
    }

    private CodecType readAvroCodec(FileSystem fs, Path path) {
        DataFileStream reader = null;
        CodecType codecType = null;
        try {
            reader = new DataFileStream(fs.open(path), new GenericDatumReader());
            Schema schema = reader.getSchema();
            String codec = reader.getMetaString("avro.codec");
            log.info("通过头信息读取avro文件 schema={}, codec={}", schema, codec);
            codecType = CodecType.getTypeByName(codec, CodecType.UNCOMPRESSED);
        } catch (IOException e) {
            log.error("读Avro编解码器异常:", e);
            System.exit(1);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                log.error("关闭 DataFileStream 流异常:");
                System.exit(1);
            }
        }
        return codecType;
    }

    /**
     * 读取Seq文件获取编码类型
     *
     * @param fs
     * @param path
     * @return
     */
    private CodecType readSeqCodec(FileSystem fs, Path path) {
        SequenceFile.Reader reader = null;
        CodecType codecType = null;
        try {
            reader = new SequenceFile.Reader(fs.getConf(), new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(path)});
            CompressionCodec compressionCodec = reader.getCompressionCodec();
            if(Objects.isNull(compressionCodec)){
//                log.info("通过压缩读取seq文件 type={}, codec={}", "Null", "Null");
                codecType = CodecType.getTypeByCodec("", CodecType.UNCOMPRESSED);
            }else {
                log.info("通过压缩读取seq文件 type={}, codec={}", reader.getCompressionType().name(), compressionCodec.getCompressorType().getName());
                codecType = CodecType.getTypeByCodec(compressionCodec.getClass().getName(), CodecType.UNCOMPRESSED);
            }



        } catch (IOException e) {
            log.error("读取Seq文件获取编码类型异常:", e);
            System.exit(1);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                log.error("关闭 DataFileStream 流异常:");
                System.exit(1);
            }
        }
        return codecType;
    }

    /**
     * 读取ORC文件获取编码类型
     *
     * @param fs
     * @param path
     * @return
     */
    private CodecType readORCCodec(FileSystem fs, Path path) {
        CodecType codecType = null;
        try {
            org.apache.orc.Reader reader = OrcFile.createReader(path, OrcFile.readerOptions(fs.getConf()));
            CompressionKind codec = reader.getCompressionKind();
            log.info("读取ORC文件获取编码类型 path={}, codec={}", path, codec);
            codecType = CodecType.getTypeByName(codec.name(), CodecType.UNCOMPRESSED);

        } catch (IOException e) {
            log.error("读取ORC文件获取编码类型异常:", e);
            System.exit(1);
        }
        return codecType;
    }

    /**
     * 读取Parquet文件获取编码类型
     *
     * @param fs
     * @param path
     * @return
     */
    private CodecType readParquetCodec(FileSystem fs, Path path) {
        CompressionCodecName codec = CompressionCodecName.UNCOMPRESSED;
        try {
            ParquetMetadata metadata = ParquetFileReader.readFooter(fs.getConf(), path, ParquetMetadataConverter.NO_FILTER);
            codec = ((ColumnChunkMetaData) ((BlockMetaData) metadata.getBlocks().get(0)).getColumns().get(0)).getCodec();
            log.info("读取Parquet文件获取编码类型 path={}, codec={}", path, codec);

        } catch (IOException e) {
            log.error("读取Parquet文件获取编码类型异常:", e);
            System.exit(1);
        }
        return CodecType.getTypeByName(codec.getParquetCompressionCodec().name(), CodecType.UNCOMPRESSED);
    }

    /**
     * 检测文件类型
     */
    private FormatType detectFileType(FileSystem fs, DirPathInfo dirPathInfo) {
        //获取随机路径
        List<Path> randomPaths = this.getRandomPaths(dirPathInfo.getFilePaths());
        FormatType finalType = null;
        boolean same = true;
        for (Path randomPath : randomPaths) {
            FSDataInputStream input = null;
            FormatType formatType = null;
            try {
                input = fs.open(randomPath);
                byte[] header = new byte[100];
                input.read(header);
                String magicHeader = new String(Arrays.copyOfRange(header, 0, 3));
                formatType = FormatType.getByMagic(magicHeader);
                log.info("get header info of path={}, formatType={}, magicHeader={}", randomPath.getName(), formatType, magicHeader);
            } catch (IOException e) {
                log.error("hdfs读取文件异常:", e);
                System.exit(1);
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    log.error("关闭 FSDataInputStream 流异常:", e);
                    System.exit(1);
                }
            }

            if (formatType == null) {
                CodecType detectCodec = this.detectCodecTypeByExt(dirPathInfo);
                formatType = this.getTextFormatType(fs, randomPath, detectCodec);
            }

            Preconditions.checkNotNull(formatType, "不能检测路径信息的formatType:" + randomPath.toString());
            if (finalType == null) {
                finalType = formatType;
            }

            if (finalType != formatType) {
                same = false;
                break;
            }
        }

        Preconditions.checkArgument(same, "文件格式类型与目录不同:" + dirPathInfo.getDirPath());
        log.info("获取随机文件类型 path={}, result={}", randomPaths, finalType);
        return finalType;
    }

    /**
     * 获取随机路径
     *
     * @param paths
     * @return
     */
    private List<Path> getRandomPaths(final List<Path> paths) {
        int count = 3;
        List<Path> randomPaths = new ArrayList(3);
        if (CollectionUtils.isEmpty(paths)) {
            return randomPaths;
        } else if (paths.size() <= count) {
            return paths;
        } else {
            while (count > 0) {
                int random = (new Random()).nextInt(paths.size());
                randomPaths.add(paths.get(random));
                --count;
            }
            return randomPaths;
        }
    }

    /**
     * 检测文件类型根据后缀
     *
     * @param dirPathInfo
     * @return
     */
    private CodecType detectCodecTypeByExt(DirPathInfo dirPathInfo) {
        List<Path> randomPaths = this.getRandomPaths(dirPathInfo.getFilePaths());
        CodecType finalCodec = null;
        boolean same = true;
        for (Path randomPath : randomPaths) {
            //获取文件的扩展名
            String extension = FilenameUtils.getExtension(randomPath.getName());
            CodecType codec = CodecType.getTypeByExtension(extension);
            Preconditions.checkNotNull(codec, "文件:" + randomPath.toString() + "不能获得文件压缩格式为 null");
            if (finalCodec == null) {
                finalCodec = codec;
            }
            if (finalCodec.getClass() != codec.getClass()) {
                same = false;
                break;
            }
        }
        Preconditions.checkArgument(same, "目录:" + dirPathInfo.getDirPath() + "下的文件,文件类型检测不到");
        return finalCodec;
    }

    /**
     * 获得格式类型
     *
     * @param fs
     * @param path
     * @param codecType
     * @return
     */
    private FormatType getTextFormatType(FileSystem fs, Path path, CodecType codecType) {
        FormatType formatType = null;
        FSDataInputStream input = null;
        try {
            input = fs.open(path);
            byte[] header = new byte[100];
            if (codecType == CodecType.UNCOMPRESSED) {
                input.read(header);
            } else {
                Class<?> codecClass = Class.forName(codecType.getCodec());
                CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, fs.getConf());
                CompressionInputStream codecHeader = codec.createInputStream(input);
                codecHeader.read(header);
            }
            //文件类型检查(text/plain)
            String mimeType = Magic.getMagicMatch(header, false).getMimeType();
            formatType = FormatType.getByMagic(mimeType);
        } catch (Exception e) {
            log.error("读取HDFS文件异常:", e);
            System.exit(1);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                log.error("关闭 FSDataInputStream 流异常:", e);
                System.exit(1);
            }
        }
        return formatType;
    }

    /**
     * 获取随机文件权限
     *
     * @param parameters
     * @param dirPathInfo
     * @return
     */
    public PermissionInfo getRandomFilePermission(Properties parameters, DirPathInfo dirPathInfo, FileSystem fs) {
        PermissionInfo permissionInfo = null;
        try {
            Path path = getRandomPath(dirPathInfo);
            FileStatus sourceFile = fs.getFileStatus(path);
            FsPermission permission = sourceFile.getPermission();
            String realUser = parameters.getProperty(Constants.HDFS_MERGE_REAL_USER);
            String realGroup = parameters.getProperty(Constants.HDFS_MERGE_REAL_GROUP);
            permissionInfo = new PermissionInfo(realUser, realGroup, permission);
        } catch (Exception e) {
            log.error("获取随机文件权限异常:", e);
            System.exit(1);
        }
        return permissionInfo;
    }

    public Path getRandomPath(DirPathInfo dirPathInfo) {
        Preconditions.checkNotNull(dirPathInfo);
        List<Path> paths = dirPathInfo.getFilePaths();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(paths), "dir has empty files");
        int random = (new Random()).nextInt(paths.size());
        return (Path) paths.get(random);
    }

    /**
     * 删除合并的文件
     *
     * @param dirList
     * @param fs
     * @throws IOException
     */
    public void deleteCombinedFiles(List<DirPathInfo> dirList, FileSystem fs) throws IOException {
        if (!CollectionUtils.isEmpty(dirList)) {
            for (DirPathInfo dirPathInfo : dirList) {
                log.info("begin to delete paths of dir path={}, files={} date={}",
                        new Object[]{dirPathInfo.getDirPath(), dirPathInfo.getFilePaths().size(), System.currentTimeMillis()});
                List<Path> filePaths = dirPathInfo.getFilePaths();
                if (!CollectionUtils.isEmpty(filePaths)) {

                    for (Path path : filePaths) {
                        if (fs.exists(path)) {
                            (new Trash(fs, fs.getConf())).moveToTrash(path);
                        }
                    }

                    log.info("finish to delete paths of dir date={}", System.currentTimeMillis());
                }
            }
        }

    }
}
