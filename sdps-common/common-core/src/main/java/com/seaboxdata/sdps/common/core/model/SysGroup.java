package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;

@Getter
@Setter
@ToString
@TableName(value = "sys_group")
public class SysGroup extends SuperEntity implements Serializable {
	/**
	 * 角色code
	 */
	private String code;

	/**
	 * 角色名
	 */
	private String name;


	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	
	@TableField(exist = false)
	private Integer members;

	private static final long serialVersionUID = 1L;

}