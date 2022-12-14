package com.seaboxdata.sdps.bigdataProxy.platform.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.assertj.core.util.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.bigdataProxy.mapper.HdfsFileStatsMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.MultiPlatformService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.utils.MathUtil;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.merge.MergeSummaryInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileRankingTopN;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerGroupUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXGroups;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.request.ApplicationRequest;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;

import feign.Response;

@Slf4j
@Service
public class CommonBigData {

	/**
	 * ?????????????????????????????????????????????Map?????????????????????key??????????????????????????? concurrentHashMap?????????????????????????????????????????????
	 */
	@Autowired
	private final Map<String, MultiPlatformService> strategyMap = new ConcurrentHashMap<>();

	@Autowired
	private SdpsClusterMapper sdpsClusterMapper;

	@Autowired
	IClusterDevOpsService clusterDevOpsService;

	@Autowired
	HdfsFileStatsMapper hdfsFileStatsMapper;

	/**
	 * ????????????
	 *
	 * @param clusterId
	 */
	private MultiPlatformService strategyRouting(Integer clusterId) {
		String clusterType = sdpsClusterMapper
				.queryClusterTypeByClusterId(clusterId);
		MultiPlatformService multiPlatformService = strategyMap
				.get(clusterType);
		return multiPlatformService;
	}

	public Boolean makeHdfsPath(String username, Integer clusterId,
			String hdfsPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.makeHdfsPath(username, clusterId,
				hdfsPath);
		return result;
	}

	public Result deleteFile(String username, Integer clusterId,
			List<String> hdfsPaths, Boolean flag) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.deleteFile(username, clusterId, hdfsPaths,
				flag);
	}

	public Boolean cleanHdfsDir(Integer clusterId,
			ArrayList<String> hdfsPathList) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.cleanHdfsDir(clusterId,
				hdfsPathList);
		return result;
	}

	public Boolean createHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj) {
		MultiPlatformService multiPlatformService = strategyRouting(hdfsSetDirObj
				.getClusterId());
		Boolean bool = multiPlatformService
				.createHdfsQNAndSQNAndOwner(hdfsSetDirObj);
		return bool;
	}

	public Result<Boolean> updataHdfsQNAndSQNAndOwner(
			HdfsSetDirObj hdfsSetDirObj) {
		MultiPlatformService multiPlatformService = strategyRouting(hdfsSetDirObj
				.getClusterId());
		return multiPlatformService.updataHdfsQNAndSQNAndOwner(hdfsSetDirObj);
	}

	public ArrayList<HdfsDirObj> getHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getHdfsSaveObjList(clusterId, hdfsPath);
	}

	public Result<List<HdfsFSObj>> selectHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result<List<HdfsFSObj>> result = multiPlatformService
				.selectHdfsSaveObjList(clusterId, hdfsPath);
		return result;
	}

	public HdfsDirObj selectHdfsQNAndSQN(Integer clusterId, String hdfsPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		HdfsDirObj result = multiPlatformService.selectHdfsQNAndSQN(clusterId,
				hdfsPath);
		return result;
	}

	public Map selectAllQueueTree(Integer clusterId) {
		if (null == clusterId || "".equals(clusterId)) {
			return null;
		}
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Map result = multiPlatformService.selectAllQueueTree(clusterId);
		return result;
	}

	public VXUsers getRangerUserByName(Integer clusterId, String userName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		VXUsers rangerUserObj = multiPlatformService.getRangerUserByName(
				clusterId, userName);
		return rangerUserObj;
	}

	public Boolean addRangerUser(Integer clusterId,
			ArrayList<VXUsers> rangerObjList) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.addRangerUser(clusterId,
				rangerObjList);
		return result;
	}

	public Boolean deleteRangerUserByName(Integer clusterId, String userName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.deleteRangerUserByName(clusterId,
				userName);
		return result;
	}

	public Boolean updateRangerUserByName(Integer clusterId,
			VXUsers rangerUserObj) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.updateRangerUserByName(clusterId,
				rangerUserObj);
		return result;
	}

	public VXGroups getRangerGroupByName(Integer clusterId, String groupName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		VXGroups result = multiPlatformService.getRangerGroupByName(clusterId,
				groupName);
		return result;
	}

	public Boolean addRangerGroup(Integer clusterId, VXGroups rangerGroupObj) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.addRangerGroup(clusterId,
				rangerGroupObj);
		return result;
	}

	public Boolean deleteRangerGroupByName(Integer clusterId, String groupName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.deleteRangerGroupByName(
				clusterId, groupName);
		return result;
	}

	public Boolean updateRangerGroupByName(Integer clusterId,
			VXGroups rangerGroupObj) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Boolean result = multiPlatformService.updateRangerGroupByName(
				clusterId, rangerGroupObj);
		return result;
	}

	public RangerGroupUser getUsersByGroupName(Integer clusterId,
			String groupName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		RangerGroupUser result = multiPlatformService.getUsersByGroupName(
				clusterId, groupName);
		return result;
	}

	public Result addUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result result = multiPlatformService.addUsersToGroup(clusterId,
				groupName, rangerUsers);
		return result;
	}

	public Result deleteUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result result = multiPlatformService.deleteUsersToGroup(clusterId,
				groupName, rangerUsers);
		return result;
	}

	public Result addRangerPolicy(RangerPolicyObj rangerPolicyObj) {
		MultiPlatformService multiPlatformService = strategyRouting(rangerPolicyObj
				.getClusterId());
		Result result = multiPlatformService.addRangerPolicy(rangerPolicyObj);
		return result;
	}

	public Result queryRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result result = multiPlatformService.queryRangerPolicy(clusterId,
				serviceType, policyName);
		return result;
	}

	public Result likeRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result result = multiPlatformService.likeRangerPolicy(clusterId,
				serviceType, policyName);
		return result;
	}

	public Result deleteRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		Result result = multiPlatformService.deleteRangerPolicy(clusterId,
				serviceType, policyName);
		return result;
	}

	/**
	 * ???????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param jsonObject
	 *            ??????json??????
	 * @return ?????????????????????1:????????? 0:??????.
	 */
	public JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject) {
		if (null == clusterId || "".equals(clusterId)) {
			return null;
		}
		if (null == jsonObject || "".equals(jsonObject)) {
			return null;
		}
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.modifyQueue(clusterId, jsonObject);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????
	 */
	public JSONObject listScheduler(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listScheduler(clusterId);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param request
	 * @return ????????????
	 */
	public JSONObject listApps(ApplicationRequest request) {
		MultiPlatformService multiPlatformService = strategyRouting(request
				.getClusterId());
		return multiPlatformService.listApps(request);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ???????????????????????????
	 */
	public JSONObject listMetrics(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listMetrics(clusterId);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????????????????
	 */
	public JSONObject listNodes(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listNodes(clusterId);
	}

	public Integer getWarningCnt(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getWarningCnt(clusterId);
	}

	public JSONObject getYarnAndHdfsMetrics(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getYarnAndHdfsMetrics(clusterId);
	}

	/**
	 * put??????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param file
	 *            ??????????????????
	 * @param path
	 *            ????????????
	 * @return ??????????????????
	 */
	public Result copyFromLocalFile(String username, Integer clusterId,
			MultipartFile file, String path, boolean isUserFile, boolean isCrypto) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.copyFromLocalFile(username, clusterId,
				file, path, isUserFile, isCrypto);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterStackAndVersions(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterStackAndVersions(clusterId);
	}

	/**
	 * ????????????????????????New
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param repositoryVersion
	 *            ????????????
	 */
	public JSONObject getClusterStackAndVersionsNew(Integer clusterId,
			String repositoryVersion) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterStackAndVersionsNew(clusterId,
				repositoryVersion);
	}

	/**
	 * ????????????service????????????
	 * 
	 * @param clusterId
	 * @param services
	 * @return
	 */
	public JSONObject getServiceUsersAndGroups(Integer clusterId,
			String services) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getServiceUsersAndGroups(clusterId,
				services);
	}

	/**
	 * ????????????host??????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterHostInfo(Integer clusterId, String query) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterHostInfo(clusterId, query);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterHostDiskInfo(Integer clusterId, String query) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterHostDiskInfo(clusterId, query);
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterServiceAutoStart(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterServiceAutoStart(clusterId);
	}

	/**
	 * ????????????service?????????display??????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getServiceDisplayName(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getServiceDisplayName(clusterId);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getServiceInstalled(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getServiceInstalled(clusterId);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getWarningInfo(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getWarningInfo(clusterId);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param clusterId
	 * @param size
	 * @param from
	 * @param definitionId
	 * @return
	 */
	public JSONObject getServiceWarningInfo(Integer clusterId,
			Integer definitionId, Integer from, Integer size) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getServiceWarningInfo(clusterId,
				definitionId, from, size);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param obj
	 * @return
	 */
	public Result updateServiceAutoStart(AmbariServiceAutoStartObj obj) {
		MultiPlatformService multiPlatformService = strategyRouting(obj
				.getClusterId());
		return multiPlatformService.updateServiceAutoStart(obj);
	}

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
	public Result<JSONObject> copyFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.copyFileFromHDFS(username, clusterId,
				srcPath, destPath);
	}

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
	public Result<JSONObject> moveFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.moveFileFromHDFS(username, clusterId,
				srcPath, destPath);
	}

	/**
	 * ????????????ip
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterIp(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterIp(clusterId);
	}

	/**
	 * ????????????host
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterHost(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterHost(clusterId);
	}

	/**
	 * ??????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getClusterName(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getClusterName(clusterId);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param obj
	 * @return
	 */
	public JSONObject startOrStopService(AmbariStartOrStopServiceObj obj) {
		MultiPlatformService multiPlatformService = strategyRouting(obj
				.getClusterId());
		return multiPlatformService.startOrStopService(obj);
	}

	/**
	 * ??????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	public JSONObject getComponentInfo(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getComponentInfo(clusterId);
	}

	/**
	 * ??????????????????
	 * 
	 * @param data
	 * @return
	 */
	public JSONObject restartAllComponent(JSONObject data) {
		MultiPlatformService multiPlatformService = strategyRouting(data
				.getInteger("clusterId"));
		return multiPlatformService.restartAllComponent(data);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param data
	 * @return
	 */
	public JSONObject startOrStopComponent(JSONObject data) {
		MultiPlatformService multiPlatformService = strategyRouting(data
				.getInteger("clusterId"));
		return multiPlatformService.startOrStopComponent(data);
	}

	public JSONObject restartComponent(JSONObject data) {
		MultiPlatformService multiPlatformService = strategyRouting(data
				.getInteger("clusterId"));
		return multiPlatformService.restartComponent(data);
	}

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
	public boolean permission(Integer clusterId, String path, String permission) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.permission(clusterId, path, permission);
	}

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
	public boolean rename(Integer clusterId, String oldPath, String newPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.rename(clusterId, oldPath, newPath);
	}

	/**
	 * hdfs????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param path
	 *            ?????????????????????
	 * @return ??????????????????
	 */
	public Response download(String username, Integer clusterId, String path) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.download(username, clusterId, path);
	}

	/**
	 * ????????????????????????
	 */
	public PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(storgeRequest
				.getClusterId());
		return multiPlatformService.getItemStorage(storgeRequest);
	}

	public String getYarnApplicationLogUrl(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getYarnApplicationLogUrl(clusterId);
	}

	public String getServerConfByConfName(Integer clusterId, String serverName,
			List<String> confStrs) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getServerConfByConfName(clusterId,
				serverName, confStrs);
	}

	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			StorgeRequest storgeRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(storgeRequest
				.getClusterId());
		return multiPlatformService.getFileStorageByTenant(storgeRequest);
	}

	public PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(storgeRequest
				.getClusterId());
		return multiPlatformService.subStorgeTrend(storgeRequest);
	}

	public PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(storgeRequest
				.getClusterId());
		return multiPlatformService.subStorgeRank(storgeRequest);
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ?????????????????????????????????
	 */
	public List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN,
			Long startTime, Long endTime) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.usedMemoryInfo(clusterId, topN, startTime,
				endTime);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @return ???????????????????????????
	 */
	public JSONObject listAppsByUser(Integer clusterId, String user) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listAppsByUser(clusterId, user);
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param states
	 *            ??????
	 * @return ?????????????????????????????????
	 */
	public JSONObject listAppsByStates(Integer clusterId, String[] states) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listAppsByStates(clusterId, states);
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @param states
	 *            ??????
	 * @return ??????????????????????????????????????????
	 */
	public JSONObject listAppsByUserAndStates(Integer clusterId, String user,
			String[] states) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.listAppsByUserAndStates(clusterId, user,
				states);
	}

	public JSONObject getYarnQueueConfigurate(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getYarnQueueConfigurate(clusterId);
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param type
	 *            ???????????????
	 * @return
	 */
	public Result<List<FileStatsDTO>> getStatsByType(Integer clusterId, String type) {
		Result<List<FileStatsDTO>> result = new Result<List<FileStatsDTO>>();
		result.setCode(0);
		// ???????????????????????????
		clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
		try {
			List<FileStatsDTO> dtos = Lists.newArrayList();
			List<HdfsFileStats> stats = hdfsFileStatsMapper.selectByType(
					clusterId, type);
			Long totalFileNum = 0L;
			Long totalFileSize = 0L;
			// ????????????
			for (HdfsFileStats dto : stats) {
				totalFileSize += dto.getTypeValueSize();
				totalFileNum += dto.getTypeValueNum();
			}
			// ???????????????
			for (HdfsFileStats dto : stats) {
				FileStatsDTO statsDTO = new FileStatsDTO();
				BeanUtils.copyProperties(dto, statsDTO);
				statsDTO.setPercentNum(MathUtil.divisionToPercent(
						dto.getTypeValueNum(), totalFileNum));
				statsDTO.setPercentSize(MathUtil.divisionToPercent(
						dto.getTypeValueSize(), totalFileSize));
				dtos.add(statsDTO);
			}
			log.info("getStatsByType:{}", dtos);
			result.setData(dtos);
		} catch (Exception e) {
			log.error("??????????????????", e);
			result.setCode(1);
			result.setMsg("??????????????????");
		}
		return result;
	}

	/**
	 * ???????????? -> ???????????? -> ????????????
	 * 
	 * @param dirRequest
	 *            ????????????
	 * @return
	 */
	public Result getStorageTopN(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.getStorageTopN(dirRequest);
	}

	/**
	 * ???????????? -> ???????????? -> ???????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result getResourceStatByPage(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.getResourceStatByPage(dirRequest);
	}

	/**
	 * ????????????tenant???????????????????????????????????????????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result getResourceByTenant(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.getResourceByTenant(dirRequest);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectStorageTrend(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectStorageTrend(dirRequest);
	}

	/**
	 * ??????????????????->???????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectPathSelections(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectPathSelections(dirRequest);
	}

	/**
	 * ??????????????????->????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectDatabaseSelections(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectDatabaseSelections(dirRequest);
	}

	/**
	 * ??????????????????->????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectTableSelections(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectTableSelections(dirRequest);
	}

	/**
	 * ???????????? -> ??????????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectDiffStorage(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectDiffStorage(dirRequest);
	}

	/**
	 * ???????????? -> ????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result selectStorageRank(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.selectStorageRank(dirRequest);
	}

	/**
	 * ??????hdfs???????????????????????????
	 * 
	 * @param dirRequest
	 * @return
	 */
	public Result getFsContent(DirRequest dirRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dirRequest
				.getClusterId());
		return multiPlatformService.getFsContent(dirRequest);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param request
	 *            ????????????
	 * @return
	 */
	public Result checkTableMerge(FileMergeRequest request) {
		MultiPlatformService multiPlatformService = strategyRouting(request
				.getClusterId());
		return multiPlatformService.checkTableMerge(request);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param request
	 * @return
	 */
	public Result getFileMergeDetail(FileMergeDetailRequest request) {
		MultiPlatformService multiPlatformService = strategyRouting(request
				.getClusterId());
		return multiPlatformService.getFileMergeDetail(request);
	}

	/**
	 * ???????????????????????????(????????????????????????????????????????????????????????????????????????)
	 * 
	 * @param clusterId
	 * @return
	 */
	public MergeSummaryInfo getMergeSummaryInfo(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getMergeSummaryInfo(clusterId);
	}

	public SmallFileRankingTopN getMergeFileTopN(Integer clusterId,
			Integer topN, Integer day) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getMergeFileTopN(clusterId, topN, day);
	}

	public Result mergeHdfsFileExec(DispatchJobRequest dispatchJobRequest) {
		MultiPlatformService multiPlatformService = strategyRouting(dispatchJobRequest
				.getClusterId());
		return multiPlatformService.mergeHdfsFileExec(dispatchJobRequest);
	}

	public Result mergeHdfsFileRetry(Integer clusterId, Integer submitId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.mergeHdfsFileRetry(clusterId, submitId);
	}

	public Result analyseHdfsMetaDataRetry(Integer clusterId, Integer taskId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.analyseHdfsMetaDataRetry(clusterId, taskId);
	}

	public Result updateYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService
				.updateYarnQueueConfigurate(clusterId, infos);
	}

	public List<SdpsCoresMBInfo> sdpsCoresAndMemoryRank(Integer clusterId,
			String type, Integer topN, String startTime, String endTime) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.sdpsCoresAndMemoryRank(clusterId, type,
				topN, startTime, endTime);
	}

	public Result deleteYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService
				.deleteYarnQueueConfigurate(clusterId, infos);
	}

	public Result insertYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService
				.insertYarnQueueConfigurate(clusterId, infos);
	}

	public Result stopOrRunningYarnQueue(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.stopOrRunningYarnQueue(clusterId, infos);
	}

	public Result addAmbariUser(Integer clusterId, AmbariUser ambariUser) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.addAmbariUser(clusterId, ambariUser);
	}

	public Result deleteAmbariUser(Integer clusterId, String username) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.deleteAmbariUser(clusterId, username);
	}

	public Result updateAmbariUserPassword(Integer clusterId,
			AmbariUser ambariUser) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.updateAmbariUserPassword(clusterId,
				ambariUser);
	}

	public Result createItemResource(String itemIden,
			HdfsSetDirObj hdfsSetDirObj) {
		MultiPlatformService multiPlatformService = strategyRouting(hdfsSetDirObj
				.getClusterId());
		return multiPlatformService.createItemResource(itemIden, hdfsSetDirObj);
	}

	public Result deleteItemFile(Integer clusterId, List<String> hdfsPaths) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.deleteItemFile(clusterId, hdfsPaths);
	}

	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(Long userId,
			String username, Integer clusterId, String hdfsPath) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.selectHdfsSaveObjListByUser(userId,
				username, clusterId, hdfsPath);
	}

	public Result<JSONArray> getAmbariUsers(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.getAmbariUsers(clusterId);
	}

	public Result<JSONObject> findServerKerberosInfo(Integer clusterId) {
		MultiPlatformService multiPlatformService = strategyRouting(clusterId);
		return multiPlatformService.findServerKerberosInfo(clusterId);
	}

}
