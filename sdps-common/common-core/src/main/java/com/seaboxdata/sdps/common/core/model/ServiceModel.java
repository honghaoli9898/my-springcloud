package com.seaboxdata.sdps.common.core.model;

import lombok.Data;

/**
 * 服务模块实体
 */
@Data
public class ServiceModel{

    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务描述
     */
    private String desc;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 有效期
     */
    private String expire;
}
