package com.seaboxdata.sdps.usersync.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@TableName("sdps_user_sync_info")
public class SdpsUserSyncInfo implements Serializable {

	/**
	 * 组件名称
	 */
	@TableField(value = "ass_name")
	private String assName;

	/**
	 * 同步结果
	 */
	@TableField(value = "sync_result")
	private Boolean syncResult;

	/**
	 * 组件类型
	 */
	@TableField(value = "ass_type")
	private String assType;

	/**
	 * 同步时间
	 */
	@TableField(value = "update_time")
	private Date updateTime;

	/**
	 * 用户ID
	 */
	@TableField(value = "user_id")
	private Long userId;

	/**
	 * 集群id
	 */
	@TableField(value = "cluster_id")
	private Integer clusterId;

	/**
	 * 错误
	 */
	private String info;
	
	/**
	 * 操作类型
	 */
	private String operator;
	
	/**
	 * 用户名称
	 */
	private String username;
	
	private static final long serialVersionUID = 1L;
}