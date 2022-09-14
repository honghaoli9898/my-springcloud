package com.seaboxdata.sdps.bigdataProxy.feign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.framework.bean.*;
import com.seaboxdata.sdps.common.framework.bean.dto.*;
import com.seaboxdata.sdps.common.framework.bean.merge.MergeSummaryInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileRankingTopN;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerGroupUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXGroups;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;

import feign.Response;

@Component
@FeignClient(value = "seabox-proxy-server", configuration = MultipartSupportConfig.class)
public interface SeaBoxFeignService {

	@GetMapping(value = "/seabox/makeHdfsPath")
	Boolean makeHdfsPath(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);

	@DeleteMapping("/seabox/deleteFile")
	Result deleteFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPath,
			@RequestParam("flag") Boolean flag);

	@DeleteMapping("/seabox/cleanHdfsDir")
	Boolean cleanHdfsDir(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPathList") ArrayList<String> hdfsPathList);

	@PostMapping(value = "/seabox/updataHdfsQNAndSQNAndOwner")
	Result<Boolean> updataHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@PostMapping("/seabox/createHdfsQNAndSQNAndOwner")
	Boolean createHdfsQNAndSQNAndOwner(@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@GetMapping(value = "/seabox/getHdfsSaveObjList")
	ArrayList<HdfsDirObj> getHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);

	@GetMapping(value = "/seabox/selectHdfsSaveObjList")
	Result<List<HdfsFSObj>> selectHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);

	@GetMapping(value = "/seabox/selectHdfsQNAndSQN")
	HdfsDirObj selectHdfsQNAndSQN(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);

	@GetMapping(value = "/seabox/selectAllQueueTree")
	Map selectAllQueueTree(@RequestParam("clusterId") Integer clusterId);

	@GetMapping("/seabox/getRangerUserByName")
	VXUsers getRangerUserByName(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	@PostMapping("/seabox/addRangerUser")
	Boolean addRangerUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody ArrayList<VXUsers> rangerObjList);

	@DeleteMapping("/seabox/deleteRangerUserByName")
	Boolean deleteRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	@PutMapping("/seabox/updateRangerUserByName")
	Boolean updateRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXUsers rangerUserObj);

	@GetMapping("/seabox/getRangerGroupByName")
	VXGroups getRangerGroupByName(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName);

	@PostMapping("/seabox/addRangerGroup")
	Boolean addRangerGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj);

	@DeleteMapping("/seabox/deleteRangerGroupByName")
	Boolean deleteRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName);

	@PutMapping("/seabox/updateRangerGroupByName")
	Boolean updateRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj);

	@GetMapping("/seabox/getUsersByGroupName")
	RangerGroupUser getUsersByGroupName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName);

	@PostMapping("/seabox/addUsersToGroup")
	Result addUsersToGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers);

	@DeleteMapping("/seabox/deleteUsersToGroup")
	Result deleteUsersToGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers);

	@PostMapping("/seabox/addRangerPolicy")
	public Result addRangerPolicy(RangerPolicyObj rangerPolicyObj);

	@GetMapping("/seabox/queryRangerPolicy")
	public Result queryRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName);

	@GetMapping("/seabox/likeRangerPolicy")
	public Result likeRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName);

	@DeleteMapping("/seabox/deleteRangerPolicy")
	public Result deleteRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName);

	/**
	 * 删除队列
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param jsonObject
	 *            队列名
	 * @return 新的集群参数数据
	 */
	@PutMapping("/yarn/modifyQueue")
	JSONObject modifyQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject jsonObject);

	/**
	 * 查询集群队列信息列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 集群返回的信息
	 */
	@GetMapping("/yarn/listScheduler")
	JSONObject listScheduler(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 任务列表
	 */
	@GetMapping("/yarn/listApps")
	JSONObject listApps(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群资源配置信息
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 集群资源配置信息。
	 */
	@GetMapping("/yarn/listMetrics")
	JSONObject listMetrics(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群各个节点信息
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 集群各个节点信息
	 */
	@GetMapping("/yarn/listNodes")
	JSONObject listNodes(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群告警数
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getWarningCnt")
	Integer getWarningCnt(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询hdfs和yarn信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getHdfsAndYarnMetrics")
	JSONObject getYarnAndHdfsMetrics(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * put操作
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param file
	 *            源文件
	 * @param path
	 *            目标路径
	 * @return 是否上传成功
	 */
	@PostMapping(value = "/seabox/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Result copyFromLocalFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam("isUserFile") boolean isUserFile,
			@RequestParam("isCrypto") boolean isCrypto);

	/**
	 * 获取集群版本信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterStackAndVersions")
	JSONObject getClusterStackAndVersions(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取集群版本信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @param repositoryVersion
	 *            仓库版本
	 */
	@GetMapping("/seabox/getClusterStackAndVersionsNew")
	JSONObject getClusterStackAndVersionsNew(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("repositoryVersion") String repositoryVersion);

	/**
	 * 获取集群service的users和groups
	 * 
	 * @param clusterId
	 * @param services
	 * @return
	 */
	@GetMapping("/seabox/getServiceUsersAndGroups")
	JSONObject getServiceUsersAndGroups(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("services") String services);

	/**
	 * 查询服务配置历史
	 * 
	 * @param clusterId
	 *            集群id
	 * @param page_size
	 *            查询条数
	 * @param from
	 *            查询起点
	 * @param sortBy
	 *            排序字段
	 * @param service_name
	 *            服务名
	 * @param createtime
	 *            查询时间范围
	 * @return
	 */
	@GetMapping("/seabox/getServiceConfigVersions")
	JSONObject getServiceConfigVersions(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("page_size") Integer page_size,
			@RequestParam("from") Integer from,
			@RequestParam("sortBy") String sortBy,
			@RequestParam("service_name") String service_name,
			@RequestParam("createtime") String createtime);

	/**
	 * 根据集群和服务获取组件和节点的关系
	 * 
	 * @param clusterId
	 *            集群id
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	@GetMapping("/seabox/getComponentAndHost")
	JSONObject getComponentAndHost(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * 根据集群和服务名查询个别配置的样式
	 * 
	 * @param clusterId
	 *            集群id
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	@GetMapping("/seabox/configThemes")
	JSONObject configThemes(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * 根据集群和服务名查询配置信息
	 * 
	 * @param clusterId
	 *            集群id
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	@GetMapping("/seabox/getConfigInfo")
	JSONObject getConfigInfo(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * 根据集群和服务名查询配置版本
	 * 
	 * @param clusterId
	 *            集群id
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	@GetMapping("/seabox/getConfigAllVersion")
	JSONObject getConfigAllVersion(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * 根据集群和服务名查询配置组
	 * 
	 * @param clusterId
	 *            集群id
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	@GetMapping("/seabox/getConfigGroup")
	JSONObject getConfigGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * 查询配置组显示的节点信息
	 * 
	 * @param clusterId
	 *            集群id
	 * @return
	 */
	@GetMapping("/seabox/getConfigHostInfo")
	JSONObject getConfigHostInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 更新配置组
	 * 
	 * @param configGroup
	 *            配置组信息
	 * @return
	 */
	@PostMapping("/seabox/updateConfigGroup")
	JSONObject updateConfigGroup(@RequestBody ConfigGroup configGroup);

	/**
	 * 删除配置组
	 * 
	 * @param clusterId
	 *            集群id
	 * @param groupId
	 *            配置组id
	 * @return
	 */
	@DeleteMapping("/seabox/deleteConfigGroup")
	JSONObject deleteConfigGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupId") Integer groupId);

	/**
	 * 校验配置
	 * 
	 * @param clusterId
	 *            集群id
	 * @param settings
	 *            配置
	 * @return
	 */
	@PostMapping("/seabox/configValidations")
	JSONObject configValidations(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings);

	/**
	 * 校验资源库操作系统URL配置
	 *
	 * @param clusterId
	 *            集群id
	 * @param stackName
	 *            集群名称
	 * @param name
	 *            repo名称
	 * @param version
	 *            版本
	 * @param osType
	 *            操作系统烈性
	 * @param repositories
	 *            配置
	 */
	@PostMapping("/seabox/resourceOsUrlValidation")
	JSONObject resourceOsUrlValidation(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("name") String name,
			@RequestParam("version") String version,
			@RequestParam("osType") String osType,
			@RequestBody JSONObject repositories);

	/**
	 * 集群版本及组件信息保存
	 *
	 * @param clusterId
	 *            集群id
	 * @param stackName
	 *            集群名称
	 * @param stackVersion
	 *            集群版本
	 * @param id
	 *            id
	 * @param repositories
	 *            配置
	 */
	@PutMapping("/seabox/clusterVersionSave")
	JSONObject clusterVersionSave(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("stackVersion") String stackVersion,
			@RequestParam("id") Integer id, @RequestBody JSONObject repositories);

	/**
	 * 获取集群版本历史信息
	 *
	 * @param clusterId
	 *            集群id
	 */
	@GetMapping("/seabox/stackHistory")
	JSONObject stackHistory(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取所有集群信息
	 *
	 * @param clusterId
	 *            集群id
	 */
	@GetMapping("/seabox/clusters")
	JSONObject clusters(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 验证参数
	 * 
	 * @param clusterId
	 *            集群id
	 * @param settings
	 *            配置
	 * @return
	 */
	@PostMapping("/seabox/configRecommendations")
	JSONObject configRecommendations(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings);

	/**
	 * 更新配置
	 * 
	 * @param clusterId
	 *            集群id
	 * @param settings
	 *            配置
	 * @return
	 */
	@PostMapping("/seabox/updateConfig")
	JSONObject updateConfig(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONArray settings);

	/**
	 * 查询已安装组件及状态
	 * 
	 * @param clusterId
	 *            集群id
	 * @return
	 */
	@GetMapping("/seabox/queryInstalledService")
	JSONObject queryInstalledService(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取集群host信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterHostInfo")
	JSONObject getClusterHostInfo(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query);

	/**
	 * 获取集群各节点磁盘信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterHostDiskInfo")
	JSONObject getClusterHostDiskInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query);

	/**
	 * 查询集群自启动服务
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterServiceAutoStart")
	JSONObject getClusterServiceAutoStart(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群service名称和display名称
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterServiceAutoStart")
	JSONObject getServiceDisplayName(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询已安装的服务
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getServiceInstalled")
	JSONObject getServiceInstalled(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询集群警告信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getWarningInfo")
	JSONObject getWarningInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 查询某个服务的报警
	 * 
	 * @param clusterId
	 * @param definitionId
	 * @param from
	 * @param size
	 * @return
	 */
	@GetMapping("/seabox/getServiceWarningInfo")
	JSONObject getServiceWarningInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("definition_id") Integer definitionId,
			@RequestParam("from") Integer from,
			@RequestParam("page_size") Integer pageSize);

	/**
	 * 更新服务自启动
	 * 
	 * @param obj
	 * @return
	 */
	@PostMapping("/seabox/updateServiceAutoStart")
	Result updateServiceAutoStart(@RequestBody AmbariServiceAutoStartObj obj);

	/**
	 * 复制HDFS文件
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param srcPath
	 *            待复制的文件路径
	 * @param destPath
	 *            目标文件路径
	 * @return 是否复制成功
	 */
	@PostMapping("/seabox/copyFileFromHDFS")
	Result<JSONObject> copyFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath);

	/**
	 * 移动HDFS文件
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param srcPath
	 *            待移动的文件路径
	 * @param destPath
	 *            目标文件路径
	 * @return 是否移动成功
	 */
	@PostMapping("/seabox/moveFileFromHDFS")
	Result<JSONObject> moveFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath);

	/**
	 * 获取集群ip
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getIpInfo")
	JSONObject getClusterIp(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取集群host
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getHostInfo")
	JSONObject getClusterHost(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取集群host 跳过 sdpServerInfo 数据库查询
	 */
	@PostMapping("/seabox/getHostInfos")
	JSONObject getClusterHosts(@RequestBody SdpsServerInfo sdpServerInfo);

	/**
	 * 校验主机登录信息
	 *
	 * @param hostMap
	 *            主机信息
	 */
	@PostMapping("/seabox/validateHostMsg")
	JSONObject validatePlatformAccountPaaswd(
			@RequestBody Map<String, Object> hostMap);

	/**
	 * 获取集群名称信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterName")
	JSONObject getClusterName(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 开启或停止服务
	 * 
	 * @param obj
	 * @return
	 */
	@PostMapping("/seabox/startOrStopService")
	JSONObject startOrStopService(@RequestBody AmbariStartOrStopServiceObj obj);

	/**
	 * 获取组件信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getComponentInfo")
	JSONObject getComponentInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 重启组件
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/seabox/restartAllComponent")
	JSONObject restartAllComponent(@RequestBody JSONObject data);

	/**
	 * 启动或者停止组件
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/seabox/startOrStopComponent")
	JSONObject startOrStopComponent(@RequestBody JSONObject data);

	/**
	 * 重启组件
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/restartComponent")
	JSONObject restartComponent(@RequestBody JSONObject data);

	/**
	 * 修改文件权限
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param path
	 *            待修改的文件路径
	 * @param permission
	 *            新权限
	 * @return 是否修改成功
	 */
	@GetMapping("/seabox/setPermission")
	boolean permission(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("permission") String permission);

	/**
	 * 文件重命名
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param oldPath
	 *            旧文件路径
	 * @param newPath
	 *            新文件路径
	 * @return 是否重命名成功
	 */
	@GetMapping("/seabox/rename")
	boolean rename(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("oldPath") String oldPath,
			@RequestParam("newPath") String newPath);

	/**
	 * hdfs文件下载
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param path
	 *            待下载文件路径
	 * @return 是否下载成功
	 */
	@GetMapping(value = "/seabox/download", consumes = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
	Response download(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path);

	/**
	 * 得到项目存储信息
	 * 
	 * @param clusterId
	 * @param itemNames
	 * @param orderColumn
	 * @param period
	 */
	@PostMapping("/seabox/storage/itemStorageResource")
	public PageResult<StorgeDirInfo> getItemStorage(
			@RequestBody StorgeRequest storgeRequest);

	/**
	 * 资源管理 -> 存储资源 -> 增长趋势
	 * 
	 * @param clusterId
	 *            集群id
	 * @param durationDay
	 *            时间范围
	 * @param topNum
	 *            前几名
	 * @param type
	 *            查询类型：1存储量 2文件数 3小文件数
	 * @return
	 */
	@GetMapping("/seabox/getServerConfByConfName")
	public String getServerConfByConfName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serverName") String serverName,
			@RequestParam("confStrs") List<String> confStrs);

	/**
	 * 获取YarnApplicationLog的Url地址
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getYarnApplicationLogUrl")
	public String getYarnApplicationLogUrl(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 资源管理 -> 存储资源 -> 增长趋势
	 * 
	 * @param clusterId
	 *            集群id
	 * @param durationDay
	 *            时间范围
	 * @param topNum
	 *            前几名
	 * @param type
	 *            查询类型：1存储量 2文件数 3小文件数
	 * @return
	 */
	@GetMapping("/seabox/stat/topN")
	List<TopDTO> topN(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("period") String period,
			@RequestParam("type") Integer type,
			@RequestParam("topNum") Integer topNum);

	/**
	 * 资源管理 -> 存储资源 -> 增长趋势
	 * 
	 * @param clusterId
	 *            集群id
	 * @param durationDay
	 *            时间范围
	 * @param orderColumn
	 *            排序列
	 * @param pageNo
	 *            起始页
	 * @param pageSize
	 *            每页数量
	 * @param desc
	 *            正序倒序
	 * @return
	 */
	@GetMapping("/seabox/stat/getResourceStatByPage")
	Page<DirInfoDTO> getResourceStatByPage(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("durationDay") Integer durationDay,
			@RequestParam("orderColumn") String orderColumn,
			@RequestParam("pageSize") Integer pageSize,
			@RequestParam("pageNo") Integer pageNo,
			@RequestParam("desc") Boolean desc);

	/**
	 * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
	 * 
	 * @param clusterId
	 *            集群id
	 * @param durationDay
	 *            时间范围
	 * @param orderColumn
	 *            排序列
	 * @param tenant
	 *            项目名
	 * @param desc
	 *            正序倒序
	 * @return
	 */
	@GetMapping("/seabox/stat/getResourceByTenant")
	List<DirInfoDTO> getResourceByTenant(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("durationDay") Integer durationDay,
			@RequestParam("orderColumn") String orderColumn,
			@RequestParam("tenant") String tenant,
			@RequestParam("desc") Boolean desc);

	/**
	 * 查询集群和服务信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @param stackName
	 *            堆栈名称
	 * @param repositoryVersion
	 *            仓库版本
	 */
	@GetMapping("/seabox/clusterAndService")
	JSONObject clusterAndService(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("repositoryVersion") String repositoryVersion);

	/**
	 * 获取资源库操作系统列表
	 *
	 * @param clusterId
	 *            集群id
	 * @param stackName
	 *            堆栈名称
	 * @param version
	 *            版本
	 */
	@GetMapping("/seabox/resourceOSList")
	JSONObject resourceOSList(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("version") String version);

	/**
	 * 校验gpl的license
	 *
	 * @param clusterId
	 *            集群id
	 */
	@GetMapping("/seabox/validateGPLLicense")
	JSONObject validateGPLLicense(@RequestParam("clusterId") Integer clusterId);

	/**
	 * 集群版本注销
	 *
	 * @param clusterId
	 *            集群ID
	 * @param id
	 *            数据ID
	 */
	@DeleteMapping("/seabox/stackVersionDel/{id}")
	JSONObject stackVersionDel(@RequestParam("clusterId") Integer clusterId,
			@PathVariable("id") Long id);

	@PostMapping("/seabox/storage/getFileStorageByTenant")
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			@RequestBody StorgeRequest storgeRequest);

	/**
	 * 查询存储资源变化趋势
	 *
	 * @param clusterId
	 *            集群ID
	 * @param startDay
	 *            开始日期
	 * @param endDay
	 *            结束日期
	 * @param storageType
	 *            资源类型
	 * @param path
	 *            hdfs路径
	 * @param dbName
	 *            库名
	 */
	@GetMapping("/seabox/stat/selectStorageTrend")
	List<DirInfoDTO> selectStorageTrend(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("startDay") String startDay,
			@RequestParam("endDay") String endDay,
			@RequestParam("path") String path,
			@RequestParam("storageType") String storageType,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("category") String category);

	/**
	 * 查询存储资源变化趋势
	 *
	 * @param clusterId
	 *            集群ID
	 * @param storageType
	 *            资源类型
	 * @param path
	 *            hdfs路径
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectPathSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("storageType") String storageType);

	/**
	 * 查询存储资源变化趋势
	 *
	 * @param clusterId
	 *            集群ID
	 * @param storageType
	 *            资源类型
	 * @param dbName
	 *            库名
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectDatabaseSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("storageType") String storageType,
			@RequestParam("dbName") String dbName);

	/**
	 * 查询存储资源变化趋势
	 *
	 * @param clusterId
	 *            集群ID
	 * @param storageType
	 *            资源类型
	 * @param dbName
	 *            库名
	 * @param category
	 *            表名
	 * @param subPath
	 *            父级目录
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectTableSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("storageType") String storageType,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("subPath") String subPath);

	/**
	 * 存储地图 -> 开始、结束时间存储量
	 * 
	 * @param clusterId
	 *            集群id
	 * @param startDay
	 *            开始时间
	 * @param endDay
	 *            结束时间
	 * @param storageType
	 *            存储类型
	 * @param path
	 *            hdfs路径
	 * @param dbName
	 *            库名
	 * @param table
	 *            表名
	 * @param category
	 *            库表类型：hbase、hive
	 * @return
	 */
	@GetMapping("/seabox/stat/selectDiffStorage")
	DirInfoDTO selectDiffStorage(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("startDay") String startDay,
			@RequestParam("endDay") String endDay,
			@RequestParam("storageType") String storageType,
			@RequestParam("path") String path,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("category") String category);

	/**
	 * 存储地图 -> 开始、结束时间存储量
	 * 
	 * @param clusterId
	 *            集群id
	 * @param type
	 *            排序维度：1存储量 2文件数 3小文件数
	 * @param pathDepth
	 *            路径深度
	 * @param storageType
	 *            存储类型
	 * @param path
	 *            hdfs路径
	 * @param dbName
	 *            库名
	 * @param category
	 *            库表类型：hbase、hive
	 * @param topNum
	 *            取前几
	 * @return
	 */
	@GetMapping("/seabox/stat/selectStorageRank")
	List<DirInfoDTO> selectStorageRank(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("storageType") String storageType,
			@RequestParam("type") Integer type,
			@RequestParam("path") String path,
			@RequestParam("pathDepth") Integer pathDepth,
			@RequestParam("dbName") String dbName,
			@RequestParam("category") String category,
			@RequestParam("topNum") Integer topNum);

	@PostMapping("/seabox/storage/subStorgeTrend")
	PageResult<StorgeDirInfo> subStorgeTrend(
			@RequestBody StorgeRequest storgeRequest);

	@PostMapping("/seabox/storage/subStorgeRank")
	PageResult<StorgeDirInfo> subStorgeRank(
			@RequestBody StorgeRequest storgeRequest);

	/**
	 * 获取hdfs已使用容量和总容量
	 * 
	 * @param clusterId
	 *            集群id
	 * @param path
	 *            hdfs路径
	 * @return
	 */
	@GetMapping("/seabox/stat/getFsContent")
	HdfsFSObj getFsContent(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path);

	/**
	 * 实时显示使用的核数和内存
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 集群使用的核数和内存数
	 */
	@GetMapping("/yarn/usedMemoryInfo")
	List<ApplicationDTO> usedMemoryInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("topN") Integer topN,
			@RequestParam("startTime") Long startTime,
			@RequestParam("endTime") Long endTime);

	/**
	 * 根据用户筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @return 该用户下的任务列表
	 */
	@GetMapping("yarn/listAppsByUser")
	JSONObject listAppsByUser(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user);

	@GetMapping("/seabox/performOperation")
	JSONObject performOperation(@RequestParam("id") Integer id);

	/**
	 * 根据任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param states
	 *            任务状态
	 * @return 该状态下的任务列表
	 */
	@GetMapping("yarn/listAppsByStates")
	JSONObject listAppsByStates(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("states") String[] states);

	/**
	 * 获取集群执行操作内容
	 *
	 * @param id
	 *            集群ID
	 * @param nodeId
	 *            节点ID
	 */
	@GetMapping("/seabox/performOperationDetail")
	JSONObject performOperationDetail(@RequestParam("id") Integer id,
			@RequestParam("nodeId") Integer nodeId);

	/**
	 * 告警消息
	 *
	 * @param id
	 *            集群ID
	 */
	@GetMapping("/seabox/alarmMsg")
	JSONObject alarmMsg(@RequestParam("id") Integer id);

	/**
	 * 根据用户和任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @param states
	 *            任务状态
	 * @return 该综合状态下的任务列表
	 */
	@GetMapping("yarn/listAppsByUserAndStates")
	JSONObject listAppsByUserAndStates(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user,
			@RequestParam("states") String[] states);

	/**
	 * 校验表是否能合并
	 * 
	 * @param clusterId
	 *            集群id
	 * @param dbName
	 *            库名
	 * @param table
	 *            表名
	 * @return
	 */
	@GetMapping("/seabox/merge/checkTableMerge")
	HiveTableDTO checkTableMerge(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("type") String type);

	/**
	 * 回显要合并的路径信息
	 * 
	 * @param clusterId
	 *            集群id
	 * @param dbName
	 *            库名
	 * @param table
	 *            表名
	 * @param type
	 *            类型：hdfs/hive/hive外表
	 * @param path
	 *            hdfs路径
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @return
	 */
	@GetMapping("/seabox/merge/getFileMergeDetail")
	FileMergeDTO getFileMergeDetail(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("type") String type,
			@RequestParam("path") String path,
			@RequestParam("startTime") String startTime,
			@RequestParam("endTime") String endTime);

	/**
	 * 存储资源->新增资源
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/seabox/storageManager/batchAddHdfsDir")
	void batchAddHdfsDir(@RequestBody StorageManageRequest request);

	/**
	 * 存储资源->新增资源
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/seabox/storageManager/batchSetQuota")
	void batchSetQuota(@RequestBody StorageManageRequest request);

	/**
	 * 存储资源->新增资源
	 * 
	 * @param clusterId
	 *            集群id
	 * @param parentPath
	 *            父级目录
	 * @return
	 */
	@GetMapping("/seabox/storageManager/getStorageList")
	List<StorageDTO> getStorageList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("parentPath") String parentPath);

	/**
	 * 获取yarn queue配置信息
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getYarnQueueConfigurate")
	public JSONObject getYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取合并小文件信息(小文件总数，以合并的小文件数，合并的小文件块大小)
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/merge/getMergeSummaryInfo")
	MergeSummaryInfo getMergeSummaryInfo(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * 获取小文合并TopN
	 * 
	 * @param clusterId
	 * @param topN
	 * @return
	 */
	@GetMapping("/seabox/merge/getMergeFileTopN")
	SmallFileRankingTopN getMergeFileTopN(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("topN") Integer topN, @RequestParam("day") Integer day);

	@PostMapping("/seabox/merge/mergeHdfsFileExec")
	Result mergeHdfsFileExec(@RequestBody DispatchJobRequest dispatchJobRequest);

	/**
	 * 重试合并文件
	 * 
	 * @param clusterId
	 * @param submitId
	 * @return
	 */
	@GetMapping("/seabox/merge/mergeHdfsFileRetry")
	Result mergeHdfsFileRetry(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("submitId") Integer submitId);

	/**
	 * 重试分析HDFS元数据文件
	 * 
	 * @param clusterId
	 * @param taskId
	 * @return
	 */
	@GetMapping("/seabox/merge/analyseHdfsMetaDataRetry")
	public Result analyseHdfsMetaDataRetry(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("taskId") Integer taskId);

	/**
	 * 更新yarn queue配置信息
	 */
	@PostMapping("/seabox/upateYarnQueueConfigurate")
	public Result updateYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	@PostMapping("/seabox/deleteYarnQueueConfigurate")
	public Result deleteYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	/**
	 * 新增yarn queue配置信息
	 */
	@PostMapping("/seabox/insertYarnQueueConfigurate")
	public Result insertYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	// 集群接口开始
	@PostMapping("/seabox/saveClusterComponentNode")
	JSONObject saveClusterComponentNode(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody List<JSONObject> componentNode);

	@PostMapping("seabox/saveClusterConfigurations")
	JSONObject saveClusterConfigurations(
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject properties);

	// 更新用户加入的配置

	@PostMapping("/seabox/saveClusterComponentHost")
	JSONObject saveClusterComponentHost(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody String hostRequestInfo);

	@PutMapping("seabox/clusterInstall")
	JSONObject clusterInstall(@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody JSONObject serviceInfo);

	@PutMapping("seabox/clusterStart")
	JSONObject clusterStart(@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody JSONObject serviceInfo);

	@PostMapping("/seabox/saveClusterWithName")
	JSONObject saveClusterWithName(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject reJson);

	@PostMapping("/seabox/saveClusterService")
	JSONObject saveClusterService(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody List<JSONObject> serviceInfos);

	/*
	 * @PutMapping("/seabox/saveClusterServiceXmlConfigurations") JSONObject
	 * saveClusterServiceXmlConfigurations(@RequestParam("masterIp") String
	 * masterIp, @RequestParam("clusterName") String clusterName, @RequestBody
	 * List<JSONObject> reqJson);
	 */

	@PutMapping("/seabox/saveClusterServiceXmlConfigurations")
	JSONObject saveClusterServiceXmlConfigurations(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody String reqJson);

	@PostMapping("/seabox/saveVersionDefinition")
	JSONObject saveVersionDefinition(@RequestParam("masterIp") String masterIp,
			@RequestParam("versionDefinition") String versionDefinition);

	@PutMapping("/seabox/saveCustomVersionDefinition")
	JSONObject saveCustomVersionDefinition(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("versionVersion") String versionVersion,
			@RequestParam("customVersionDefinition") String customVersionDefinition);

	@PutMapping("/seabox/putOsRepositoriesData")
	JSONObject putOsRepositoriesData(@RequestParam("masterIp") String masterIp,
			@RequestParam("repoJson") String repoJson);

	@PostMapping("/seabox/postClusterCurrentStatus")
	JSONObject postClusterCurrentStatus(
			@RequestParam("masterIp") String masterIp,
			@RequestBody String clusterCurrentStatus);

	@PostMapping("/seabox/saveBootstrap")
	JSONObject saveBootstrap(@RequestParam("masterIp") String masterIp,
			@RequestBody List<SdpsClusterHost> hostList);

	@PostMapping("/seabox/saveRequests")
	JSONObject saveRequests(@RequestParam("masterIp") String masterIp);

	@PostMapping("/seabox/saveRecommendations")
	JSONObject saveRecommendations(@RequestParam("masterIp") String masterIp,
			@RequestBody String reqBody);

	@PostMapping("/seabox/saveValidations")
	JSONObject saveValidations(@RequestParam("masterIp") String masterIp,
			@RequestBody String reqBody);

	@PostMapping("/seabox/saveClusterComponentNodes")
	JSONObject saveClusterComponentNodes(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody String componentNodes);

	@PutMapping("/seabox/saveClusterComponentNodeState")
	JSONObject saveClusterComponentNodeState(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("state") String state);

	@PutMapping("/seabox/saveClusterComponentNodeStatusNewZk")
	JSONObject saveClusterComponentNodeStatusNewZk(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("state") String state);

	@GetMapping("/seabox/getBootStrapStatus")
	JSONObject getBootStrapStatus(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId);

	@GetMapping("/seabox/getClusterCurrentStatus")
	JSONObject getClusterCurrentStatus(@RequestParam("masterIp") String masterIp);

	// 集群接口结束

	@PostMapping("/seabox/stopOrRunningYarnQueue")
	Result stopOrRunningYarnQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	@GetMapping("/seabox/getRequestStatus")
	JSONObject getRequestStatus(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("requestId") String requestId);

	@PostMapping("/seabox/saveRequestsThird")
	JSONObject saveRequestsThird(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/getOneRequest")
	JSONObject getOneRequest(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId);

	@GetMapping("/seabox/getSecondRequest")
	JSONObject getSecondRequest(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId);

	@GetMapping("/seabox/hostsfieldsHostshost_status")
	JSONObject hostsfieldsHostshost_status(
			@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/fieldsconfigurations31")
	JSONObject fieldsconfigurations31(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/servicesStackServices")
	JSONObject servicesStackServices(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/servicesthemes")
	JSONObject servicesthemes(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/hostsfieldsHoststotal_mem")
	JSONObject hostsfieldsHoststotal_mem(
			@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/servicesfieldsStackServices")
	JSONObject servicesfieldsStackServices(
			@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/hostsHostshost_name")
	JSONObject hostsHostshost_name(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/stackOne")
	JSONObject stackOne(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/AMBARI_SERVER")
	JSONObject AMBARI_SERVER(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/version_definitionsfieldsVersionDefinition")
	JSONObject version_definitionsfieldsVersionDefinition(
			@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/stacksHDPversions31")
	JSONObject stacksHDPversions31(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/stacksHDPversions30")
	JSONObject stacksHDPversions30(@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/clustersfieldsClustersprovisioning_state")
	JSONObject clustersfieldsClustersprovisioning_state(
			@RequestParam("masterIp") String masterIp);

	@GetMapping("/seabox/getRequestTaskResult")
	JSONObject getRequestTaskResult(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("requestId") Long requestId,
			@RequestParam("taskId") Long taskId);

	@PostMapping("/seabox/addAmbariUser")
	public Result addAmbariUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);

	@DeleteMapping("/seabox/deleteAmbariUser")
	public Result deleteAmbariUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("username") String username);

	@PutMapping("/seabox/updateAmbariUserPassword")
	public Result updateAmbariUserPassword(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);

	@PostMapping("/seabox/createItemResource")
	public Result createItemResource(@RequestParam("itemIden") String itemIden,
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@DeleteMapping("/seabox/deleteItemFile")
	public Result deleteItemFile(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths);

	@GetMapping("/seabox/selectHdfsSaveObjListByUser")
	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(
			@RequestParam("userId") Long userId,
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);
	
	/**
	 * 获取ambari用户信息
	 *
	 * @return
	 */
	@GetMapping("/seabox/getAmbariUsers")
	public Result<JSONArray> getAmbariUsers(
			@RequestParam("clusterId") Integer clusterId);
	
	@GetMapping("/seaboxKeytab/findServerKerberosInfo")
	public Result<JSONObject> findServerKerberosInfo(
			@RequestParam("clusterId") Integer clusterId) ;
}
