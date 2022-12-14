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
	 * ????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param jsonObject
	 *            ?????????
	 * @return ????????????????????????
	 */
	@PutMapping("/yarn/modifyQueue")
	JSONObject modifyQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject jsonObject);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ?????????????????????
	 */
	@GetMapping("/yarn/listScheduler")
	JSONObject listScheduler(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????
	 */
	@GetMapping("/yarn/listApps")
	JSONObject listApps(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ???????????????????????????
	 */
	@GetMapping("/yarn/listMetrics")
	JSONObject listMetrics(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????????????????
	 */
	@GetMapping("/yarn/listNodes")
	JSONObject listNodes(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ?????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getWarningCnt")
	Integer getWarningCnt(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ??????hdfs???yarn??????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getHdfsAndYarnMetrics")
	JSONObject getYarnAndHdfsMetrics(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * put??????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param file
	 *            ?????????
	 * @param path
	 *            ????????????
	 * @return ??????????????????
	 */
	@PostMapping(value = "/seabox/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Result copyFromLocalFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam("isUserFile") boolean isUserFile,
			@RequestParam("isCrypto") boolean isCrypto);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterStackAndVersions")
	JSONObject getClusterStackAndVersions(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param repositoryVersion
	 *            ????????????
	 */
	@GetMapping("/seabox/getClusterStackAndVersionsNew")
	JSONObject getClusterStackAndVersionsNew(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("repositoryVersion") String repositoryVersion);

	/**
	 * ????????????service???users???groups
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
	 * ????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param page_size
	 *            ????????????
	 * @param from
	 *            ????????????
	 * @param sortBy
	 *            ????????????
	 * @param service_name
	 *            ?????????
	 * @param createtime
	 *            ??????????????????
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
	 * ???????????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/seabox/getComponentAndHost")
	JSONObject getComponentAndHost(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * ???????????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/seabox/configThemes")
	JSONObject configThemes(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/seabox/getConfigInfo")
	JSONObject getConfigInfo(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/seabox/getConfigAllVersion")
	JSONObject getConfigAllVersion(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/seabox/getConfigGroup")
	JSONObject getConfigGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName);

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @return
	 */
	@GetMapping("/seabox/getConfigHostInfo")
	JSONObject getConfigHostInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ???????????????
	 * 
	 * @param configGroup
	 *            ???????????????
	 * @return
	 */
	@PostMapping("/seabox/updateConfigGroup")
	JSONObject updateConfigGroup(@RequestBody ConfigGroup configGroup);

	/**
	 * ???????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param groupId
	 *            ?????????id
	 * @return
	 */
	@DeleteMapping("/seabox/deleteConfigGroup")
	JSONObject deleteConfigGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupId") Integer groupId);

	/**
	 * ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????
	 * @return
	 */
	@PostMapping("/seabox/configValidations")
	JSONObject configValidations(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings);

	/**
	 * ???????????????????????????URL??????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param stackName
	 *            ????????????
	 * @param name
	 *            repo??????
	 * @param version
	 *            ??????
	 * @param osType
	 *            ??????????????????
	 * @param repositories
	 *            ??????
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
	 * ?????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param stackName
	 *            ????????????
	 * @param stackVersion
	 *            ????????????
	 * @param id
	 *            id
	 * @param repositories
	 *            ??????
	 */
	@PutMapping("/seabox/clusterVersionSave")
	JSONObject clusterVersionSave(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("stackVersion") String stackVersion,
			@RequestParam("id") Integer id, @RequestBody JSONObject repositories);

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 */
	@GetMapping("/seabox/stackHistory")
	JSONObject stackHistory(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 */
	@GetMapping("/seabox/clusters")
	JSONObject clusters(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????
	 * @return
	 */
	@PostMapping("/seabox/configRecommendations")
	JSONObject configRecommendations(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings);

	/**
	 * ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????
	 * @return
	 */
	@PostMapping("/seabox/updateConfig")
	JSONObject updateConfig(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONArray settings);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @return
	 */
	@GetMapping("/seabox/queryInstalledService")
	JSONObject queryInstalledService(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????host??????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterHostInfo")
	JSONObject getClusterHostInfo(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query);

	/**
	 * ?????????????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterHostDiskInfo")
	JSONObject getClusterHostDiskInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query);

	/**
	 * ???????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterServiceAutoStart")
	JSONObject getClusterServiceAutoStart(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????service?????????display??????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterServiceAutoStart")
	JSONObject getServiceDisplayName(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getServiceInstalled")
	JSONObject getServiceInstalled(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getWarningInfo")
	JSONObject getWarningInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ???????????????????????????
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
	 * ?????????????????????
	 * 
	 * @param obj
	 * @return
	 */
	@PostMapping("/seabox/updateServiceAutoStart")
	Result updateServiceAutoStart(@RequestBody AmbariServiceAutoStartObj obj);

	/**
	 * ??????HDFS??????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param srcPath
	 *            ????????????????????????
	 * @param destPath
	 *            ??????????????????
	 * @return ??????????????????
	 */
	@PostMapping("/seabox/copyFileFromHDFS")
	Result<JSONObject> copyFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath);

	/**
	 * ??????HDFS??????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param srcPath
	 *            ????????????????????????
	 * @param destPath
	 *            ??????????????????
	 * @return ??????????????????
	 */
	@PostMapping("/seabox/moveFileFromHDFS")
	Result<JSONObject> moveFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath);

	/**
	 * ????????????ip
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getIpInfo")
	JSONObject getClusterIp(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????host
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getHostInfo")
	JSONObject getClusterHost(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????host ?????? sdpServerInfo ???????????????
	 */
	@PostMapping("/seabox/getHostInfos")
	JSONObject getClusterHosts(@RequestBody SdpsServerInfo sdpServerInfo);

	/**
	 * ????????????????????????
	 *
	 * @param hostMap
	 *            ????????????
	 */
	@PostMapping("/seabox/validateHostMsg")
	JSONObject validatePlatformAccountPaaswd(
			@RequestBody Map<String, Object> hostMap);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getClusterName")
	JSONObject getClusterName(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ?????????????????????
	 * 
	 * @param obj
	 * @return
	 */
	@PostMapping("/seabox/startOrStopService")
	JSONObject startOrStopService(@RequestBody AmbariStartOrStopServiceObj obj);

	/**
	 * ??????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getComponentInfo")
	JSONObject getComponentInfo(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ????????????
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/seabox/restartAllComponent")
	JSONObject restartAllComponent(@RequestBody JSONObject data);

	/**
	 * ????????????????????????
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/seabox/startOrStopComponent")
	JSONObject startOrStopComponent(@RequestBody JSONObject data);

	/**
	 * ????????????
	 * 
	 * @param data
	 * @return
	 */
	@PostMapping("/restartComponent")
	JSONObject restartComponent(@RequestBody JSONObject data);

	/**
	 * ??????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param path
	 *            ????????????????????????
	 * @param permission
	 *            ?????????
	 * @return ??????????????????
	 */
	@GetMapping("/seabox/setPermission")
	boolean permission(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("permission") String permission);

	/**
	 * ???????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param oldPath
	 *            ???????????????
	 * @param newPath
	 *            ???????????????
	 * @return ?????????????????????
	 */
	@GetMapping("/seabox/rename")
	boolean rename(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("oldPath") String oldPath,
			@RequestParam("newPath") String newPath);

	/**
	 * hdfs????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param path
	 *            ?????????????????????
	 * @return ??????????????????
	 */
	@GetMapping(value = "/seabox/download", consumes = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
	Response download(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path);

	/**
	 * ????????????????????????
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
	 * ???????????? -> ???????????? -> ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param durationDay
	 *            ????????????
	 * @param topNum
	 *            ?????????
	 * @param type
	 *            ???????????????1????????? 2????????? 3????????????
	 * @return
	 */
	@GetMapping("/seabox/getServerConfByConfName")
	public String getServerConfByConfName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serverName") String serverName,
			@RequestParam("confStrs") List<String> confStrs);

	/**
	 * ??????YarnApplicationLog???Url??????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getYarnApplicationLogUrl")
	public String getYarnApplicationLogUrl(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ???????????? -> ???????????? -> ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param durationDay
	 *            ????????????
	 * @param topNum
	 *            ?????????
	 * @param type
	 *            ???????????????1????????? 2????????? 3????????????
	 * @return
	 */
	@GetMapping("/seabox/stat/topN")
	List<TopDTO> topN(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("period") String period,
			@RequestParam("type") Integer type,
			@RequestParam("topNum") Integer topNum);

	/**
	 * ???????????? -> ???????????? -> ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param durationDay
	 *            ????????????
	 * @param orderColumn
	 *            ?????????
	 * @param pageNo
	 *            ?????????
	 * @param pageSize
	 *            ????????????
	 * @param desc
	 *            ????????????
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
	 * ????????????tenant???????????????????????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param durationDay
	 *            ????????????
	 * @param orderColumn
	 *            ?????????
	 * @param tenant
	 *            ?????????
	 * @param desc
	 *            ????????????
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
	 * ???????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param stackName
	 *            ????????????
	 * @param repositoryVersion
	 *            ????????????
	 */
	@GetMapping("/seabox/clusterAndService")
	JSONObject clusterAndService(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("repositoryVersion") String repositoryVersion);

	/**
	 * ?????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param stackName
	 *            ????????????
	 * @param version
	 *            ??????
	 */
	@GetMapping("/seabox/resourceOSList")
	JSONObject resourceOSList(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("version") String version);

	/**
	 * ??????gpl???license
	 *
	 * @param clusterId
	 *            ??????id
	 */
	@GetMapping("/seabox/validateGPLLicense")
	JSONObject validateGPLLicense(@RequestParam("clusterId") Integer clusterId);

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param id
	 *            ??????ID
	 */
	@DeleteMapping("/seabox/stackVersionDel/{id}")
	JSONObject stackVersionDel(@RequestParam("clusterId") Integer clusterId,
			@PathVariable("id") Long id);

	@PostMapping("/seabox/storage/getFileStorageByTenant")
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			@RequestBody StorgeRequest storgeRequest);

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param startDay
	 *            ????????????
	 * @param endDay
	 *            ????????????
	 * @param storageType
	 *            ????????????
	 * @param path
	 *            hdfs??????
	 * @param dbName
	 *            ??????
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
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param storageType
	 *            ????????????
	 * @param path
	 *            hdfs??????
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectPathSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("storageType") String storageType);

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param storageType
	 *            ????????????
	 * @param dbName
	 *            ??????
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectDatabaseSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("storageType") String storageType,
			@RequestParam("dbName") String dbName);

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param storageType
	 *            ????????????
	 * @param dbName
	 *            ??????
	 * @param category
	 *            ??????
	 * @param subPath
	 *            ????????????
	 */
	@GetMapping("/seabox/stat/selectStorageSelections")
	List<String> selectTableSelections(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("storageType") String storageType,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("subPath") String subPath);

	/**
	 * ???????????? -> ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param startDay
	 *            ????????????
	 * @param endDay
	 *            ????????????
	 * @param storageType
	 *            ????????????
	 * @param path
	 *            hdfs??????
	 * @param dbName
	 *            ??????
	 * @param table
	 *            ??????
	 * @param category
	 *            ???????????????hbase???hive
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
	 * ???????????? -> ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param type
	 *            ???????????????1????????? 2????????? 3????????????
	 * @param pathDepth
	 *            ????????????
	 * @param storageType
	 *            ????????????
	 * @param path
	 *            hdfs??????
	 * @param dbName
	 *            ??????
	 * @param category
	 *            ???????????????hbase???hive
	 * @param topNum
	 *            ?????????
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
	 * ??????hdfs???????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param path
	 *            hdfs??????
	 * @return
	 */
	@GetMapping("/seabox/stat/getFsContent")
	HdfsFSObj getFsContent(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path);

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ?????????????????????????????????
	 */
	@GetMapping("/yarn/usedMemoryInfo")
	List<ApplicationDTO> usedMemoryInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("topN") Integer topN,
			@RequestParam("startTime") Long startTime,
			@RequestParam("endTime") Long endTime);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @return ???????????????????????????
	 */
	@GetMapping("yarn/listAppsByUser")
	JSONObject listAppsByUser(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user);

	@GetMapping("/seabox/performOperation")
	JSONObject performOperation(@RequestParam("id") Integer id);

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param states
	 *            ????????????
	 * @return ???????????????????????????
	 */
	@GetMapping("yarn/listAppsByStates")
	JSONObject listAppsByStates(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("states") String[] states);

	/**
	 * ??????????????????????????????
	 *
	 * @param id
	 *            ??????ID
	 * @param nodeId
	 *            ??????ID
	 */
	@GetMapping("/seabox/performOperationDetail")
	JSONObject performOperationDetail(@RequestParam("id") Integer id,
			@RequestParam("nodeId") Integer nodeId);

	/**
	 * ????????????
	 *
	 * @param id
	 *            ??????ID
	 */
	@GetMapping("/seabox/alarmMsg")
	JSONObject alarmMsg(@RequestParam("id") Integer id);

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @param states
	 *            ????????????
	 * @return ?????????????????????????????????
	 */
	@GetMapping("yarn/listAppsByUserAndStates")
	JSONObject listAppsByUserAndStates(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user,
			@RequestParam("states") String[] states);

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param dbName
	 *            ??????
	 * @param table
	 *            ??????
	 * @return
	 */
	@GetMapping("/seabox/merge/checkTableMerge")
	HiveTableDTO checkTableMerge(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("dbName") String dbName,
			@RequestParam("table") String table,
			@RequestParam("type") String type);

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param dbName
	 *            ??????
	 * @param table
	 *            ??????
	 * @param type
	 *            ?????????hdfs/hive/hive??????
	 * @param path
	 *            hdfs??????
	 * @param startTime
	 *            ????????????
	 * @param endTime
	 *            ????????????
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
	 * ????????????->????????????
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/seabox/storageManager/batchAddHdfsDir")
	void batchAddHdfsDir(@RequestBody StorageManageRequest request);

	/**
	 * ????????????->????????????
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/seabox/storageManager/batchSetQuota")
	void batchSetQuota(@RequestBody StorageManageRequest request);

	/**
	 * ????????????->????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param parentPath
	 *            ????????????
	 * @return
	 */
	@GetMapping("/seabox/storageManager/getStorageList")
	List<StorageDTO> getStorageList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("parentPath") String parentPath);

	/**
	 * ??????yarn queue????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/getYarnQueueConfigurate")
	public JSONObject getYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ???????????????????????????(????????????????????????????????????????????????????????????????????????)
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/seabox/merge/getMergeSummaryInfo")
	MergeSummaryInfo getMergeSummaryInfo(
			@RequestParam("clusterId") Integer clusterId);

	/**
	 * ??????????????????TopN
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
	 * ??????????????????
	 * 
	 * @param clusterId
	 * @param submitId
	 * @return
	 */
	@GetMapping("/seabox/merge/mergeHdfsFileRetry")
	Result mergeHdfsFileRetry(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("submitId") Integer submitId);

	/**
	 * ????????????HDFS???????????????
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
	 * ??????yarn queue????????????
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
	 * ??????yarn queue????????????
	 */
	@PostMapping("/seabox/insertYarnQueueConfigurate")
	public Result insertYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	// ??????????????????
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

	// ???????????????????????????

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

	// ??????????????????

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
	 * ??????ambari????????????
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
