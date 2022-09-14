package com.seaboxdata.sdps.common.core.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUser extends SuperEntity {
	private static final long serialVersionUID = -5886012896705137070L;

	private String username;
	private String password;
	private String nickname;
	private String headImgUrl;
	private Integer sex;
	private Boolean enabled;
	private boolean isDel;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;
	@TableField(exist = false)
	private List<SysRole> roles;
	
	@TableField(exist = false)
	private String userRoles;

	@TableField(exist = false)
	private String oldPassword;
	@TableField(exist = false)
	private String newPassword;
	@TableField(exist = false)
	private List<Long> roleIds;
	
	@TableField(value = "sync_user_status")
	private String syncUserStatus;
	
	@TableField(value = "sync_cluster_ids")
	private String syncClusterIds;
	
	@TableField(exist = false)
	private Boolean syncUser;
	
	@TableField(exist = false)
	private String tenants;
	
	@TableField(exist = false)
	private List<Long> tenantList;
	
	@TableField(exist = false)
	private String tenantIds;
}
