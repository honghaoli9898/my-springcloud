package com.seaboxdata.sdps.common.framework.bean.yarn;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Denny
 * @date: 2021/10/28 14:36
 * @desc: 队列实体类
 */
@Data
public class QueueNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 队列ID标识
     */
    private Integer queueId;

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 最大容量，默认100.
     */
    private Integer maximumCapacity;

    /**
     * 队列容量,【0,100】区间，0表示队列挂起。
     */
    private Integer capacity;

    /**
     * 队列状态，RUNNING和STOPPED两个状态。
     */
    private String state;

    /**
     * 子队列，没有则为null。
     */
    private String queues;

    /**
     * 相关yarnId
     */
    private Integer yarnId;

}
