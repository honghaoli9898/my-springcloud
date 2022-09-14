package com.seaboxdata.sdps.user.mybatis.dto.user;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class UserApprovalDto implements Serializable {

    // 审批单号
    private Long spId;

    // 申请单号
    private Long sqId;

    // 审批人id
    private Long spUserId;

    // 审批人
    private String spUserName;

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

    // 审批状态
    private Integer spStatus;

    // 审批时间
    private Date spTime;

    // 操作（审批动作，0无法操作，1通过，2驳回）
    private Integer action;

    // 审批通过原因
    private String spReason;


}
