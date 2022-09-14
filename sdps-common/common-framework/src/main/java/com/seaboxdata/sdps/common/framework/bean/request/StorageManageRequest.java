package com.seaboxdata.sdps.common.framework.bean.request;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageManageRequest implements Serializable {
	private static final long serialVersionUID = -1425530531282626666L;
	/**
	 * 集群id
	 */
	@NonNull
	private Integer clusterId;
	/**
	 * hdfs路径
	 */
	private List<String> paths;
	/**
	 * 文件、文件夹数量配额
	 */
	private Long nsQuota;
	/**
	 * 空间配额
	 */
	private Long dsQuota;
	/**
	 * 启用逻辑配额
	 */
	private Boolean enableNsQuota = false;
	/**
	 * 启用物理配额
	 */
	private Boolean enableDsQuota = false;
	/**
	 * 是否是新增操作
	 */
	private Boolean isInsert = false;
	/**
	 * 父级目录
	 */
	private String parentPath;
	/**
	 * 过期时间
	 */
	private Integer expireDay;
}
