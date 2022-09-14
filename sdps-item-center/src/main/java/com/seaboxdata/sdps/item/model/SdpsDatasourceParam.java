package com.seaboxdata.sdps.item.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;

@Getter
@Setter
@ToString
@TableName(value = "sdps_datasource_param")
public class SdpsDatasourceParam extends SuperEntity<SdpsDatasourceParam> {

	/**
	 * 数据源类型id
	 */
	@TableField(value = "type_id")
	private Long typeId;

	/**
	 * 显示名
	 */
	@TableField(value = "display_name")
	private String displayName;

	/**
	 * 名字
	 */
	private String name;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 排名
	 */
	private Integer rank;
	
	private Boolean must;
	
	/**
	 * 字段类型
	 */
	@TableField(value = "field_type")
	private String fieldType;

	private static final long serialVersionUID = 1L;
}
