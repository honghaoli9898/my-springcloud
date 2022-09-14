package com.seaboxdata.sdps.item.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "datasource_item")
public class DatasourceItem extends Model<DatasourceItem> implements
		Serializable {
	/**
	 * 数据源id
	 */
	@TableField(value = "datasource_id")
	private Long datasourceId;

	/**
	 * 项目id
	 */
	@TableField(value = "item_id")
	private Long itemId;

	private static final long serialVersionUID = 1L;

}