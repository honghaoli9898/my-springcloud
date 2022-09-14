package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ItemDtoObj implements Serializable {
    private static final long serialVersionUID = 3180721789333786426L;
    private Long id;
    private String name;
    private String iden;
    private Integer members;
    private Long clusterId;
    private Date createTime;
    private Long userId;
    private List<ItemDtoObj> datas;
    private String nickname;
    private Long groupId;
    private String clusterName;
    private String desc;
    private Boolean isSecurity;
    private List<Long> ids;
    private Integer userCnt;
}
