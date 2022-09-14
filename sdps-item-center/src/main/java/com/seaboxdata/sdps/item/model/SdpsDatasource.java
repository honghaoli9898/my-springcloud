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
@TableName(value = "sdps_datasource")
public class SdpsDatasource extends SuperEntity<SdpsDatasource> {

	/**
	 * 创建人
	 */
	@TableField(value = "create_user")
	private String createUser;

	/**
	 * 更新人
	 */
	@TableField(value = "update_user")
	private String updateUser;

	/**
	 * 创建人id
	 */
	@TableField(value = "create_user_id")
	private Long createUserId;

	/**
	 * 更新人id
	 */
	@TableField(value = "update_user_id")
	private Long updateUserId;

	/**
	 * 数据源类型
	 */
	@TableField(value = "type_id")
	private Long typeId;

	/**
	 * 数据源名称
	 */
	@TableField(value = "`name`")
	private String name;

	/**
	 * 描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	/**
	 * 配置
	 */
	private String properties;

	/**
	 * 是否系统初始化
	 */
	@TableField(value = "system_init")
	private Boolean systemInit;

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

	/**
	 * 关联集群id
	 */
	@TableField(value = "cluster_id")
	private Long clusterId;

	/**
	 * 集群配置
	 */
	private String config;

	@TableField(exist = false)
	private List<Long> itemIds;

	private static final long serialVersionUID = 1L;
}
