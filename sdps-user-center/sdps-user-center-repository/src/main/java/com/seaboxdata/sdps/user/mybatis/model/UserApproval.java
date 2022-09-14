package com.seaboxdata.sdps.user.mybatis.model;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@TableName(value = "user_approval")
public class UserApproval extends Model<UserApproval> implements Serializable {

    @TableId(value = "id")
    private Long id;


    /**
     * 申请单号
     */
    @TableField(value = "sq_id")
    private Long sqId;

    /**
     * 审批人id
     */
    @TableField(value = "sp_userid")
    private Long spUserId;

    /**
     * 审批人
     */
    @TableField(value = "sp_username")
    private String spUserName;

    /**
     * 申请人id
     */
    @TableField(value = "sq_userid")
    private Long sqUserId;

    /**
     * 申请人
     */
    @TableField(value = "sq_username")
    private String sqUserName;

    /**
     * 申请内容
     */
    @TableField(value = "apply_content")
    private String applyContent;

    /**
     * 申请原因
     */
    @TableField(value = "apply_reason")
    private String applyReason;

    /**
     * 申请时间
     */
    @TableField(value = "apply_time")
    private Date applyTime;

    /**
     * 审批状态
     */
    @TableField(value = "sp_status")
    private Integer spStatus;

    /**
     * 审批时间
     */
    @TableField(value = "sp_time",updateStrategy = FieldStrategy.IGNORED)
    private Date spTime;

    /**
     * 操作
     */
    @TableField(value = "action")
    private Integer action;

    /**
     * 原因
     */
    @TableField(value = "sp_reason",updateStrategy = FieldStrategy.IGNORED)
    private String spReason;

    /**
     * 角色
     */
    @TableField(value = "role")
    private String role;


    /**
     * 类型 0：连接管理
     */
    @TableField(value = "type")
    private Integer type;


    /**
     * 角色id
     */
    @TableField(value = "role_id")
    private Long roleId;


}
