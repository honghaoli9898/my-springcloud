package com.seaboxdata.sdps.common.framework.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * HDFS 设置存储对象
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HdfsSetDirObj implements Serializable {
    private static final long serialVersionUID = 6878713944002363590L;
    /**
     * 集群ID
     */
    private Integer clusterId;
    /**
     * hdfs目录
     */
    private String hdfsPath;
    private Long quotaNum = -1L;
    private Long spaceQuotaNum = -1L;
    private String owner;
    private String ownergroup;
    private String sourceHdfsPath;
}
