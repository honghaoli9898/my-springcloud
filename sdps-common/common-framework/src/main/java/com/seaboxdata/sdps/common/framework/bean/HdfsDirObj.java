package com.seaboxdata.sdps.common.framework.bean;

import lombok.*;

import java.io.Serializable;

/**
 * HDFS 存储资源对象
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HdfsDirObj implements Serializable {
    private static final long serialVersionUID = 4453753485597879759L;
    //目录名称
    private String dirName;
    //所属者
    private String owner;
    //所属组
    private String group;
    //空间消耗值(字节)
    private long spaceConsumed;
    //存储空间最大配额(字节)
    private long spaceQuotaNum;
    //文件数限制
    private long quotaNum;
    //修改时间
    private String modificationTime;
    //读写执行权限
    private String fsPermission;
    //保留天数，-1为未做限制
    private Integer expireDay = -1;

}
