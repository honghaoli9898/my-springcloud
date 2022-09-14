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
@TableName(value = "sdps_resources")
public class SdpsResources extends SuperEntity implements Serializable {

	/**
	 * 项目id
	 */
	@TableField(value = "item_id")
	private Long itemId;

	/**
	 * yarn资源
	 */
	@TableField(value = "yarn_queue")
	private String yarnQueue;

	/**
	 * hdfs文件目录
	 */
	@TableField(value = "hdfs_path")
	private String hdfsPath;

	/**
	 * 文件数限制
	 */
	@TableField(value = "file_limit")
	private Long fileLimit;

	/**
	 * 项目存储空间最大配额(GB)
	 */
	@TableField(value = "max_store")
	private Long maxStore;

	/**
	 * 集群id
	 */
	@TableField(value = "cluster_id")
	private Long clusterId;
	
	@TableField(exist = false)
	private String clusterName;
	
	@TableField(exist = false)
	private Long cnt;

	private static final long serialVersionUID = 1L;

}