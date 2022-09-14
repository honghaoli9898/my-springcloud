package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author pengsong
 * 基类：包含createTime、modifyTime、creator、modifier基本字段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicObject{

    private String creator;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private String modifier;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

}
