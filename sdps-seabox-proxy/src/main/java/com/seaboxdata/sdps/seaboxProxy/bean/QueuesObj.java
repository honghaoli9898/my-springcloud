package com.seaboxdata.sdps.seaboxProxy.bean;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 队列节点
 */
@Data
@ToString
public class QueuesObj implements Serializable {

    private static final long serialVersionUID = -3102910444125314152L;
    /**
     * 队列名称
     */
    private String queueName;
    /**
     * 队列集合
     */
    private List<QueuesObj> childQueues;
}
