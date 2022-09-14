package com.seaboxdata.sdps.common.core.model;

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
@TableName(value = "sdps_item")
public class SdpsItem extends SuperEntity implements Serializable {

	/**
	 * 项目名称
	 */
	private String name;

	/**
	 * 项目标识 只能填写英文、数字和下划线
	 */
	private String iden;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	@TableField(exist = false)
	private List<Long> ids;
	@TableField(value = "is_security")
	private Boolean isSecurity;
	
	@TableField(value = "tenant_id")
	private Long tenantId;
	

	private static final long serialVersionUID = 1L;

}