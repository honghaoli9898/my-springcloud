package com.seaboxdata.sdps.user.mybatis.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;

@Getter
@Setter
@ToString
@TableName(value = "sys_organization")
public class SysOrganization extends SuperEntity implements Serializable {

	/**
	 * 组织code
	 */
	private String code;

	/**
	 * 父组织id
	 */
	@TableField(value = "parent_id")
	private Long parentId;

	/**
	 * 组织名
	 */
	private String name;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	@TableField(exist = false)
	private List<SysOrganization> subOrgan;

	private static final long serialVersionUID = 1L;

}