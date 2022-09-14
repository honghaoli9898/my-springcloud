package com.seaboxdata.sdps.common.framework.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SdpsDirExpire implements Serializable {
	private static final long serialVersionUID = 8416689724596770172L;
	private Integer id;
    /**
     * 集群id
     */
    private Integer clusterId;
    /**
     * hdfs路径
     */
    private String path;
    /**
     * 过期天数
     */
    private Integer expireDay;

    private Date createTime;

    private Date updateTime;
}
