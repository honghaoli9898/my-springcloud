package com.seaboxdata.sdps.seaboxProxy.feign;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;

@Component
@FeignClient(value = "bigdataCommon-proxy-server")
public interface BigdataCommonFegin {

	/**
	 * 根据集群ID查询集群主机
	 * 
	 * @param clusterId
	 * @return
	 */
	@RequestMapping(value = "/clusterCommon/queryClusterHost")
	public String queryClusterHost(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 根据集群ID查询集群配置
	 * 
	 * @param clusterId
	 * @return
	 */
	@RequestMapping(value = "/clusterCommon/queryClusterConfPath")
	public String queryClusterConfPath(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 根据集群ID和类型查询sdps_server_info表信息
	 * 
	 * @param clusterId
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/clusterCommon/queryClusterServerInfo")
	public SdpsServerInfo queryClusterServerInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("type") String type);

	/**
	 * 根据yarn id 查询yarn主节点&端口信息
	 *
	 * @param clusterId
	 *            yarn ID
	 * @return yarn 主节点&端口信息
	 */
	@RequestMapping("/yarnCommon/selectYarnInfo")
	public SdpsServerInfo selectYarnInfo(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 根据clusterId查询ambari-server节点信息。
	 *
	 * @param clusterId
	 *            集群id
	 * @return ambari-server的IP和端口。
	 */
	@RequestMapping("/ambariCommon/selectAmbariInfo")
	public SdpsServerInfo selectAmbariInfo(
			@RequestParam("clusterId") Integer clusterId);

	@RequestMapping("/devops/param")
	public Result<SysGlobalArgs> getGlobalParam(
			@RequestParam("type") String type, @RequestParam("key") String key);

	@RequestMapping("/devops/params")
	public Result<List<SysGlobalArgs>> getGlobalParams(
			@RequestParam("type") String type);

	/**
	 * 根据集群id查询出集群名
	 *
	 * @param clusterId
	 *            集群id
	 * @return
	 */
	@RequestMapping("/clusterCommon/selectClusterNameById")
	String queryClusterName(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群列表
	 * 
	 * @return
	 */
	@RequestMapping("/bigdataCommon/selectClusterList")
	Result<List<Map>> selectClusterList();

	/**
	 * 根据集群ID查询集群表信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/clusterCommon/querySdpsClusterById")
	SdpsCluster querySdpsClusterById(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 *
	 * @param storgeRequest
	 * @return
	 */
	@PostMapping("/storageResource/getDbOrTableList")
	public PageResult<SdpsHdfsDbTable> getDbOrTableList(
			@RequestBody StorgeRequest storgeRequest);

	/**
	 *
	 * @param taskType
	 * @param cluster_type
	 * @return
	 */
	@GetMapping("/bigdataCommon/getTaskConfByClusterTypeAndTaskType")
	List<TaskConfig> getTaskConfByClusterTypeAndTaskType(
			@RequestParam("taskType") String taskType,
			@RequestParam("cluster_type") String cluster_type);

	/**
	 *
	 * @param clusterId
	 * @param path
	 * @return
	 */
	@GetMapping("/storageManager/selectByPath")
	SdpsDirExpire selectByPath(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path);

	/**
	 *
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/bigdataCommon/getServerInfo")
	public SdpsServerInfo getServerInfo(
			@RequestParam("username") String username);

	@GetMapping("/bigdataCommon/findServerInfo")
	public Result<SdpsServerInfo> selectServerInfo(
			@RequestParam("serverId") Long serverId,
			@RequestParam("type") String type);


	@GetMapping("/bigdataCommon/findServerInfo")
	public Result<SdpsServerInfo> selectAmbariInfo(@RequestParam("serverId") Integer serverId, @RequestParam("type") String type);

	@GetMapping("/clusterCommon/getEnableKerberosClusters")
	Result getEnableKerberosClusters();
}
