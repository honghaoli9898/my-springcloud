package com.seaboxdata.sdps.common.framework.bean.task;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskConfig implements Serializable {
    private static final long serialVersionUID = 8134250268550097228L;

    /**
     * 任务配置ID
     */
    private Integer id;
    /**
     * 集群类型ID
     */
    private Integer clusterTypeId;
    /**
     * 任务类型
     */
    private String taskType;
    /**
     * 任务参数key
     */
    private String argKey;

    /**
     * 任务参数value
     */
    private String argValue;

    /**
     * 任务参数说明
     */
    private String argDesc;
}
