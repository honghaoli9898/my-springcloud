package com.seaboxdata.sdps.common.framework.bean.task;

import java.io.Serializable;
import java.util.Date;

import lombok.*;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sdps_task_info")
public class SdpsTaskInfo implements Serializable {
	/**
	 * 任务ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 提交任务ID
	 */
	@TableField(value = "submit_id")
	private Long submitId;

	/**
	 * 集群ID
	 */
	@TableField(value = "cluster_id")
	private Integer clusterId;

	/**
	 * 集群名称
	 */
	@TableField(value = "cluster_name")
	private String clusterName;

	/**
	 * 任务提交用户
	 */
	@TableField(value = "user_name")
	private String userName;

	/**
	 * yarn的applicationId
	 */
	@TableField(value = "yarn_app_id")
	private String yarnAppId;

	/**
	 * yarn的applicationName
	 */
	@TableField(value = "yarn_app_name")
	private String yarnAppName;

	/**
	 * yarn队列名
	 */
	@TableField(value = "yarn_queue")
	private String yarnQueue;

	/**
	 * yarn追踪日志Url
	 */
	@TableField(value = "yarn_tracking_url")
	private String yarnTrackingUrl;

	/**
	 * 任务类型(SPAKR,TEZ)
	 */
	@TableField(value = "application_type")
	private String applicationType;

	/**
	 * 关联xxlLog ID
	 */
	@TableField(value = "xxl_log_id")
	private Long xxlLogId;

	/**
	 * 关联xxlJob任务ID
	 */
	@TableField(value = "xxl_job_id")
	private Long xxlJobId;

	/**
	 * 执行任务shell路径
	 */
	@TableField(value = "shell_path")
	private String shellPath;

	/**
	 * 执行shell内容(名称,路径,内容)
	 */
	@TableField(value = "shell_context")
	private String shellContext;

	/**
	 * 创建时间
	 */
	@TableField(value = "create_time")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@TableField(value = "update_time")
	private Date updateTime;

	/**
	 * 扩展字段
	 */
	@TableField(value = "ext_0")
	private String ext0;

	@TableField(value = "ext_1")
	private String ext1;

	@TableField(value = "ext_2")
	private String ext2;

	@TableField(value = "ext_3")
	private String ext3;

	@TableField(value = "ext_4")
	private String ext4;

	private static final long serialVersionUID = 1L;
}