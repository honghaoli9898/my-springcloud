package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
@TableName("sdps_tenant")
public class SdpsTenant implements Serializable {
	/**
	 * 租户id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 父id
	 */
	@TableField(value = "parent_id")
	private Long parentId;

	/**
	 * 租户名称
	 */
	private String name;

	/**
	 * 租户标识
	 */
	private String iden;

	/**
	 * 关联集群id
	 */
	@TableField(exist = false)
	private Long assClusterId;
	
	@TableField(exist = false)
	private String assClusterName;

	/**
	 * 父级租户树
	 */
	private String ancestors;

	@TableField(value="`desc`")
	private String desc;

	/**
	 * 租户管理员id
	 */
	@TableField(exist = false)
	private List<Long> userIdList;
	
	@TableField(exist = false)
	private Long leaderRoleId;

	/**
	 * 创建者id
	 */
	@TableField(value = "creater_id")
	private Long createrId;

	/**
	 * 创建者
	 */
	private String creater;

	/**
	 * 创建时间
	 */
	@TableField(value = "create_time")
	private Date createTime;

	@TableField(exist = false)
	private List<SdpsTenant> subTenants;
	
	@TableField(exist = false)
	private List<SdpsTenantResource> tenantResources;
	
	/**
	 * 创建者id
	 */
	@TableField(value = "level")
	private Integer level;

	private static final long serialVersionUID = 1L;

}