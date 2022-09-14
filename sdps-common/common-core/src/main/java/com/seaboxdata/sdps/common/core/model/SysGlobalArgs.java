package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@TableName(value = "sys_global_args")
public class SysGlobalArgs implements Serializable {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	@TableField(value = "arg_type")
	private String argType;

	@TableField(value = "arg_key")
	private String argKey;

	@TableField(value = "arg_key_desc")
	private String argKeyDesc;

	@TableField(value = "arg_value")
	private String argValue;

	@TableField(value = "arg_value_desc")
	private String argValueDesc;

	private static final long serialVersionUID = 1L;

}