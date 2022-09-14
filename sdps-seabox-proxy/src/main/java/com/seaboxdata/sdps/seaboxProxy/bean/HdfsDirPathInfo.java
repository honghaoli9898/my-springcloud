package com.seaboxdata.sdps.seaboxProxy.bean;

import lombok.Data;
import lombok.ToString;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class HdfsDirPathInfo implements Serializable {

	private static final long serialVersionUID = 7475707957023559127L;
	/**
     * hdfs路径
     */
    private String dirPath;
    /**
     * 文件总数
     */
    private Long totalFileNum = 0L;
    /**
     * 总文件大小
     */
    private Long bytes = 0L;
    /**
     * 总文件块大小
     */
    private Long totalBlockBytes = 0L;
    /**
     * 小文件总数
     */
    private Long totalSmallFileNum = 0L;
    /**
     * 小文件大小Map(key:路径,value:文件大小bytes)
     */
    private Map<Path, Long> pathsSmallFileByBytes = new HashMap<Path, Long>();
    /**
     * 父级路径
     */
    private Map<String, Path> pathsDir = new HashMap<String, Path>();

    public HdfsDirPathInfo(String dirPath) {
        this.dirPath = dirPath;
    }

    public void addFile(LocatedFileStatus file) {
        //文件总数
        totalFileNum += 1;
        //总文件大小
        bytes = bytes + file.getLen();
        //总文件块大小
        totalBlockBytes = totalBlockBytes + file.getBlockSize();
        Path filePath = file.getPath();
        Path parent = filePath.getParent();
        pathsDir.put(parent.toUri().getPath(), parent);
    }

    /**
     * 增加小文件信息
     * @param file
     */
    public void addSmallFile(LocatedFileStatus file){
        totalSmallFileNum += 1;
        //所有小文件大小Map
        pathsSmallFileByBytes.put(file.getPath(), file.getLen());
    }

}
