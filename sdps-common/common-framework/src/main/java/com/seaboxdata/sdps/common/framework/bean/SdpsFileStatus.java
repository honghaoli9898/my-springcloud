package com.seaboxdata.sdps.common.framework.bean;

import lombok.Data;

import java.io.Serializable;
@Data
public class SdpsFileStatus implements Serializable {
	private static final long serialVersionUID = -8005956028789292213L;
	/**
     * 文件路径
     */
    private String path;
    /**
     * 文件名
     */
    private String fileName;
    private String type;
    private String fileId;
    private String owner;
    private String group;
    private String permission;
    /**
     * 副本数量
     */
    private Integer replication;
    /**
     * block大小
     */
    private Long blockSize;
    /**
     * 子文件和子文件夹数量
     */
    private Long childrenNum;
    private Long accessTime;
    private Long modificationTime;
    /**
     *
     */
    private Long length;
    /**
     * 文件数量
     */
    private Long totalFileNum;
    /**
     * 文件总大小
     */
    private Long totalFileSize;
    /**
     * block块数量
     */
    private Long totalBlockNum;
    /**
     * 小文件数量
     */
    private Long totalSmallFileNum;
    /**
     * 空文件数量
     */
    private Long totalEmptyFileNum;
    /**
     * 是否是小文件
     */
    private Boolean isSmallFile = false;
    /**
     * 对当前目录树中的文件和目录名称的数量的硬限制
     */
    private Long nsQuota;
    /**
     * 目录树中的文件使用的字节数量的硬限制
     */
    private Long dsQuota;

    public boolean isFile() {
        return "FILE".equalsIgnoreCase(this.type);
    }

    public boolean isDirectory() {
        return "DIRECTORY".equalsIgnoreCase(this.type);
    }

    public String getFullPath(String prefix) {
        return prefix.endsWith("/") ? prefix + getFileName() : prefix + "/" + getFileName();
    }
}
