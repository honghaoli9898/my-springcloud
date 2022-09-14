package com.seaboxdata.sdps.common.framework.bean.ranger;

import java.io.Serializable;
import java.util.List;

import com.seaboxdata.sdps.common.framework.bean.ranger.resource.HbaseOperateResource;
import com.seaboxdata.sdps.common.framework.bean.ranger.resource.HiveOperateResource;
import lombok.Data;

@Data
public class RangerPolicyObj implements Serializable {
	private static final long serialVersionUID = -6039392830951905660L;
	/**
	 * 集群ID
	 */
	private Integer clusterId;
	/**
	 * 策略名称
	 */
	private String policyName;
	/**
	 * 策略服务名称
	 */
	private String serviceType;
	/**
	 * hdfs资源路径
	 */
	private List<String> resourcePaths;
	/**
	 * hive资源路径
	 */
	private HiveOperateResource hiveOperateResource;
	/**
	 * hbase资源路径
	 */
	private HbaseOperateResource hbaseOperateResource;
	/**
	 * yarn资源路径
	 */
	private List<String> yarnOperateResource;
	/**
	 * kafka资源路径
	 */
	private List<String> kafkaOperateResource;
	/**
	 * 用户组
	 */
	private List<String> groups;
	/**
	 * 用户
	 */
	private List<String> users;
	/**
	 * 权限(Hdfs,Hive,Hbase,Yarn,Kafka)
	 */
	private List<String> accesses;
}
