package com.seaboxdata.sdps.seaboxProxy.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;

@Data
public class SdpsDirPathInfo implements Serializable {

	private static final long serialVersionUID = -5669190871852243123L;
	/**
     * hdfs路径
     */
    private String dirPath;
    /**
     * 压缩编码
     */
    private CodecType codecType;
    /**
     * 文件格式
     */
    private FormatType formatType;
    private Long bytes = 0L;
    private Map<Path, Long> pathsByBytes = new HashMap();
    private Map<String, Path> pathsDir = new HashMap();
    private Long lastModifyTime;

    public SdpsDirPathInfo(String dirPath) {
        this.dirPath = dirPath;
    }

    public void addFile(LocatedFileStatus file) {
        bytes = bytes + file.getLen();
        Path filePath = file.getPath();
        pathsByBytes.put(filePath, file.getLen());
        Path parent = filePath.getParent();
        pathsDir.put(parent.toString(), parent);
    }

//    public String getFilePathNames() {
//        StringBuilder builder = new StringBuilder();
//        pathsByBytes.keySet().forEach(x -> builder.append(x.toString()).append(","));
//
//        return builder.length() > 0 ? builder.substring(0, builder.length() - 1) : builder.toString();
//    }

//    public List<String> getFilePathList() {
//        return (List)pathsByBytes.keySet().stream().map(Path::toString).collect(Collectors.toList());
//    }

    public List<Path> getFilePaths() {
        return new ArrayList(pathsByBytes.keySet());
    }

//    public List<Path> getDirPaths() {
//        return new ArrayList(pathsDir.values());
//    }

//    public Seq<String> getFilePathSeq() {
//        List<String> filePaths = this.getFilePathList();
//        return ((Iterable)JavaConverters.iterableAsScalaIterableConverter(filePaths).asScala()).toSeq();
//    }

}
