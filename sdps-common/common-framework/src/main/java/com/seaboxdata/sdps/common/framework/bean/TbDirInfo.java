package com.seaboxdata.sdps.common.framework.bean;

import lombok.*;

/**
 * 资源统计实体类
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TbDirInfo {
    /**
     * 集群名
     */
    private String cluster;
    /**
     * hdfs路径
     */
    private String path;
    /**
     * 文件总数
     */
    private Long totalFileNum;
    /**
     * 文件总大小
     */
    private Long totalFileSize;
    /**
     * 平均文件大小
     */
    private Double avgFileSize;
    /**
     * block块总数
     */
    private Long totalBlockNum;
    /**
     * 小文件总数
     */
    private Long totalSmallFileNum;
    /**
     * 空文件总数
     */
    private Long totalEmptyFileNum;
    /**
     * hdfs总目录层数
     */
    private Integer pathIndex;
    /**
     * 类型：file/hive/hbase的编号
     */
    private Integer type;
    /**
     * 具体的类型值
     */
    private String typeValue;
    /**
     * 租户名
     */
    private String tenant;
    /**
     * 最近更新时间
     */
    private String modificationTime;
    /**
     * 最近访问时间
     */
    private String accessTime;
    /**
     * 数据热度  1：hot  2：warn  3:cold  4:frozen
     */
    private Integer temperature;
    /**
     * 统计日期
     */
    private String dayTime;
}
