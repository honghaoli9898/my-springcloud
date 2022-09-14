package com.seaboxdata.sdps.item.model;

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
@TableName(value = "sdps_datasource_type")
public class SdpsDatasourceType extends SuperEntity<SdpsDatasourceType> {

	/**
	 * 数据源类型
	 */
	private String category;

	/**
	 * 数据源名称
	 */
	private String name;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 是否有效
	 */
	@TableField(value = "is_valid")
	private Boolean isValid;

	/**
	 * 是否可见
	 */
	@TableField(value = "is_visible")
	private Boolean isVisible;
	

	@TableField(exist = false)
	private List<SdpsDatasourceParam> params;

	private static final long serialVersionUID = 1L;
}