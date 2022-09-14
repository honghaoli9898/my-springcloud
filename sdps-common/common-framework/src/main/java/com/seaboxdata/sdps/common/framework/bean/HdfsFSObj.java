package com.seaboxdata.sdps.common.framework.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: Denny
 * @date: 2021/11/15 17:57
 * @desc: HDFS 文件系统实体类
 */
@Data
public class HdfsFSObj implements Serializable {
    private static final long serialVersionUID = 4453753485597879759L;
    //目录名称
    private String fileName;
    //所属者
    private String owner;
    //所属组
    private String group;
    //修改时间
    private String modificationTime;
    //读写执行权限
    private String fsPermission;
    //blockSize大小。单位：字节。
    private Long blockSize;
    //length
    private Long length;
    //replication
    private Short replication;
    //type
    private HdfsFileType type;
    /**
     * 目录已使用大小，单位：字节
     */
    private Long used;
    /**
     * 集群总容量大小，单位：字节
     */
    private Long totalCapacity;


}
