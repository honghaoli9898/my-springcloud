package com.seaboxdata.sdps.extendAnalysis.merge;

import lombok.Data;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class DirPathInfo {
    /**
     * 合并临时目录
     */
    private String mergeTmpPath;
    /**
     * 目录
     */
    private String dirPath;
    /**
     * HDFS压缩格式
     */
    private CodecType codecType;
    /**
     * HDFS文件类型
     */
    private FormatType formatType;
    /**
     *
     */
    private Long bytes = 0L;
    //文件大小Map(key:路径,value:文件大小bytes)
    private Map<Path, Long> pathsByBytes = new HashMap();
    //文件块大小Map(key:路径,value:文件块大小bytes)
    private Map<Path, Long> pathsByBlockBytes = new HashMap();
    //路径Map(key:父级路径,value:路径)
    private Map<String, Path> pathsDir = new HashMap();
    private Long lastModifTime;

    public DirPathInfo(String dirPath, String mergeTmpPath) {
        this.dirPath = dirPath;
        this.mergeTmpPath = mergeTmpPath;
    }

    public void addFile(final LocatedFileStatus file) {
        this.bytes = this.bytes + file.getLen();
        Path filePath = file.getPath();
        this.pathsByBytes.put(filePath, file.getLen());
        this.pathsByBlockBytes.put(filePath,file.getBlockSize());
        Path parent = filePath.getParent();
        this.pathsDir.put(parent.toString(), parent);
    }

    public List<Path> getFilePaths() {
        return new ArrayList(this.pathsByBytes.keySet());
    }

    public String getTargetPath() {
        URI uri = (new Path(this.getDirPath())).toUri();
        StringBuilder builder = new StringBuilder();
        if (uri.getScheme() != null) {
            builder.append(uri.getScheme());
            builder.append(":");
        }

        if (uri.getAuthority() != null) {
            builder.append("//");
            builder.append(uri.getAuthority());
        }
        return builder.append(mergeTmpPath).append(uri.getPath()).toString();
    }

    public Seq<String> getFilePathSeq() {
        List<String> filePaths = this.getFilePathList();
        return JavaConverters.iterableAsScalaIterableConverter(filePaths).asScala().toSeq();
    }

    public List<String> getFilePathList() {
        return (List) this.pathsByBytes.keySet().stream().map(Path::toString).collect(Collectors.toList());
    }

    public String getFilePathNames() {
        StringBuilder builder = new StringBuilder();
        Set<Path> pathSet = pathsByBytes.keySet();
        for (Path path : pathSet) {
            builder.append(path.toString()).append(",");
        }

        return builder.length() > 0 ? builder.substring(0, builder.length() - 1) : builder.toString();
    }
}
