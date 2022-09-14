package com.seaboxdata.sdps.common.framework.bean.request;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class FileMergeRequest implements Serializable {
	private static final long serialVersionUID = 6850492943631418567L;

	/**
	 * 集群id
	 */
	@NonNull
	private Integer clusterId;

	/**
	 * 路径名
	 */
	private String path;
	/**
	 * 库名
	 */
	private String dbName;
	/**
	 * 表名
	 */
	private String table;
	/**
	 * 库表类型：hive/hive_external
	 */
	private String type;
}
