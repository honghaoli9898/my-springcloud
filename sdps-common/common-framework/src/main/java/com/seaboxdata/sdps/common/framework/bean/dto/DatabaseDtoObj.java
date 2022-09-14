package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DatabaseDtoObj implements Serializable {
    private static final long serialVersionUID = -8229460564996341944L;
    private String name;
    private Long id;
    private Long typeId;
    private String type;
    private String assItemName;
    private Long assItemId;
    private String createUser;
    private Date updateTime;
    private String desc;
    private Long clusterId;
    private String clusterShowName;
    private String assItemIden;
}
