package com.seaboxdata.sdps.common.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRole extends SuperEntity {
	private static final long serialVersionUID = 4497149010220586111L;
	private String code;
	private String name;
	@TableField(exist = false)
	private Long userId;
	/**
	 * 角色类型 S为系统角色,O为项目角色
	 */
	private String type;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 是否启用
	 */
	private Boolean enabled;
	
	@TableField(value = "tenant_id")
	private String tenantId;

}
