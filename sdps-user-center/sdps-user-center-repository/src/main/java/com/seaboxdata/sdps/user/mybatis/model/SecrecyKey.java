package com.seaboxdata.sdps.user.mybatis.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName(value = "sdps_secrecy_key")
public class SecrecyKey implements Serializable {

    /**
     * 主键
     */
    @TableField(value = "id")
    public Integer id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    public Integer userId;

    /**
     * 密钥名称
     */
    @TableField(value = "name")
    public String name;

    /**
     * 密钥
     */
    @TableField(value = "secrecy_key")
    public String secrecyKey;

    /**
     * 描述
     */
    @TableField(value = "description")
    public String description;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    public String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Timestamp createTime;


    /**
     * 更新者
     */
    @TableField(value = "update_by")
    public String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public Timestamp updateTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    public String remark;
}
