package com.seaboxdata.sdps.item.model;

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
@TableName(value = "sdps_table")
public class SdpsTable extends SuperEntity<SdpsTable> implements Serializable {

	@TableField(value = "type_id")
	private Long typeId;

	/**
	 * 数据表英文名称
	 */
	@TableField(value = "en_name")
	private String enName;

	/**
	 * 数据表中文名称
	 */
	@TableField(value = "cn_name")
	private String cnName;

	/**
	 * 关联项目id
	 */
	@TableField(value = "item_id")
	private Long itemId;

	/**
	 * 责任人id
	 */
	private Long owner;

	/**
	 * 关联数据库id
	 */
	@TableField(value = "database_id")
	private Long databaseId;

	/**
	 * 表描述
	 */
	private String description;

	/**
	 * 存活周期
	 */
	@TableField(value = "life_time")
	private Integer lifeTime;

	/**
	 * 是否外表
	 */
	@TableField(value = "is_external")
	private Boolean isExternal;

	/**
	 * 业务描述
	 */
	@TableField(value = "bus_desc")
	private String busDesc;

	/**
	 * 所属集群id
	 */
	@TableField(value = "cluster_id")
	private Long clusterId;

	/**
	 * 高级配置
	 */
	private String senior;

	/**
	 * 是否手动创建
	 */
	@TableField(value = "create_mode")
	private Boolean createMode;

	/**
	 * sql
	 */
	@TableField(value = "field_sql")
	private String fieldSql;

	private static final long serialVersionUID = 1L;

}