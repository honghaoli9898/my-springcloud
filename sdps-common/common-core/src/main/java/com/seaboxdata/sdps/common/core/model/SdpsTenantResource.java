package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@TableName(value = "sdps_tenant_resource")
public class SdpsTenantResource implements Serializable {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 集群id
	 */
	@TableField(value = "ass_cluster_id")
	private Long assClusterId;

	/**
	 * 集群名称
	 */
	@TableField(value = "ass_cluster_name")
	private String assClusterName;

	/**
	 * 租户id
	 */
	@TableField(value = "tenant_id")
	private Long tenantId;

	/**
	 * hdfs目录
	 */
	@TableField(value = "hdfs_dir")
	private String hdfsDir;

	/**
	 * 可用空间最大值
	 */
	@TableField(value = "space_quota")
	private Long spaceQuota;

	/**
	 * 文件限制数
	 */
	@TableField(value = "file_num_quota")
	private Long fileNumQuota;

	/**
	 * 队列全名
	 */
	@TableField(value = "resource_full_name")
	private String resourceFullName;

	/**
	 * 队列名
	 */
	@TableField(value = "resource_name")
	private String resourceName;

	/**
	 * 最大core数
	 */
	@TableField(value = "max_core")
	private Integer maxCore;

	/**
	 * 最大内存
	 */
	@TableField(value = "max_memory")
	private Integer maxMemory;

	/**
	 * 最大app数量
	 */
	@TableField(value = "max_app_num")
	private Integer maxAppNum;

	/**
	 * 最大app份额
	 */
	@TableField(value = "max_app_master_ratio")
	private Integer maxAppMasterRatio;

	/**
	 * 权重
	 */
	private Integer weight;

	@TableField(value = "item_id")
	private Long itemId;

	@TableField(exist = false)
	private Long cnt;

	@TableField(exist = false)
	private Long usedSpace;

	private static final long serialVersionUID = 1L;

}