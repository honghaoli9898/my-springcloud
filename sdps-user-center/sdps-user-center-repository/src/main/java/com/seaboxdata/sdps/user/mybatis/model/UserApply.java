package com.seaboxdata.sdps.user.mybatis.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@TableName(value = "user_apply")
public class UserApply extends Model<UserApply> implements Serializable {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

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
     * 申请次数
     */
    @TableField(value = "apply_num")
    private Long applyNum;

    /**
     * 审批人
     */
    @TableField(value = "sp_username")
    private String spUserName;


}
