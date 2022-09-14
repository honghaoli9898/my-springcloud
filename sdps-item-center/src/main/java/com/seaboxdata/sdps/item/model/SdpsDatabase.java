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
@TableName(value = "sdps_database")
public class SdpsDatabase extends SuperEntity<SdpsDatabase> implements
		Serializable {

	/**
	 * 创建人
	 */
	@TableField(value = "create_user")
	private String createUser;

	@TableField(value = "create_user_id")
	private Long createUserId;

	/**
	 * 更新人
	 */
	@TableField(value = "update_user")
	private String updateUser;

	@TableField(value = "update_user_id")
	private Long updateUserId;
	/**
	 * 数据库类型id
	 */
	@TableField(value = "type_id")
	private Long typeId;

	/**
	 * 数据库名称
	 */
	@TableField(value = "`name`")
	private String name;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 关联项目id
	 */
	@TableField(value = "item_id")
	private Long itemId;

	/**
	 * 数据源id
	 */
	@TableField(value = "cluster_id")
	private Long clusterId;

	private static final long serialVersionUID = 1L;
}