package com.seaboxdata.sdps.user.mybatis.dto.user;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserApplyDto implements Serializable {

    // 申请单号
    private Long sqId;

    // 申请人id
    private long sqUserId;

    // 申请人
    private String sqUserName;

    // 申请内容
    private String applyContent;

    // 申请原因
    private String applyReason;

    // 申请时间
    private Date applyTime;

    // 申请次数
    private Long applyNum;

    // 审批单号
    private Long spId;

    // 审批人id
    private Long spUserId;

    // 审批人
    private String spUserName;

    // 审批状态
    private Integer spStatus;

    // 审批时间
    private Date spTime;

    // 操作（审批动作，0无法操作，1通过，2驳回）
    private Integer action;

    // 审批通过原因
    private String spReason;

}
