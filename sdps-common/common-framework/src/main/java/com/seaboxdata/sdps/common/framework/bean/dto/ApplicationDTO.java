package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDTO implements Serializable {
	private static final long serialVersionUID = 8579021605291916502L;

	private String applicationId;

    private String applicationName;
    /**
     * 任务类型：mapReduce/spark/tez
     */
    private String type;

    private String tag;
    /**
     * 任务所属用户
     */
    private String user;
    /**
     * 任务状态
     */
    private String state;
    /**
     * 队列
     */
    private String queue;
    /**
     * 进度
     */
    private Double progress;
    /**
     * 任务开始时间
     */
    private Date startTime;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 资源使用在集群中的占比
     */
    private Double clusterUsagePercentage;
    /**
     * 每秒内存使用总和
     */
    private Long memorySeconds;
    /**
     * am所在地址
     */
    private String amHostHttpAddress;

    public ApplicationDTO(String id, String name, String queue, String type, String amHostHttpAddress, Long memorySeconds) {
        this.applicationId = id;
        this.applicationName = name;
        this.queue = queue;
        this.type = type;
        this.amHostHttpAddress = amHostHttpAddress;
        this.memorySeconds = memorySeconds;
    }
}
