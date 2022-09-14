package com.seaboxdata.sdps.user.mybatis.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@TableName(value = "user_tenant_role")
public class UserTenantRole implements Serializable {
	/**
	 * 用户id
	 */
	@TableField(value = "user_id")
	private Long userId;

	/**
	 * 租户id
	 */
	@TableField(value = "tenant_id")
	private Long tenantId;

	/**
	 * 角色id
	 */
	@TableField(value = "role_id")
	private Long roleId;

	private static final long serialVersionUID = 1L;
}