package com.seaboxdata.sdps.bigdataProxy.platform.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsDirExpireMapper;
import com.seaboxdata.sdps.bigdataProxy.service.ISdpsDirExpireService;
import com.seaboxdata.sdps.common.framework.bean.*;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;

import lombok.extern.slf4j.Slf4j;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfoDTO;
import com.seaboxdata.sdps.bigdataProxy.common.DatabaseConstants;
import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsCoresAndMBInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.MultiPlatformService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.merge.MergeSummaryInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileRankingTopN;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerGroupUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXGroups;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.request.ApplicationRequest;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.common.framework.enums.QueryEnum;

import feign.Response;

@Slf4j
@Service("seabox")
public class SeaBoxPlatform implements MultiPlatformService {

	@Autowired
	SeaBoxFeignService seaBoxFeignService;

	@Autowired
	IClusterDevOpsService clusterDevOpsService;

	@Autowired
	SdpsCoresAndMBInfoMapper coresAndMBInfoMapper;

	@Autowired
	ISdpsDirExpireService dirExpireService;

	@Override
	public Boolean makeHdfsPath(String username, Integer clusterId, String hdfsPath) {
		Boolean result = seaBoxFeignService.makeHdfsPath(username, clusterId, hdfsPath);
		return result;
	}

	@Override
	public Result deleteFile(String username, Integer clusterId,
			List<String> hdfsPaths, Boolean flag) {
		return seaBoxFeignService.deleteFile(username, clusterId, hdfsPaths,flag);
	}

	@Override
	public Boolean cleanHdfsDir(Integer clusterId,
			ArrayList<String> hdfsPathList) {
		return seaBoxFeignService.cleanHdfsDir(clusterId, hdfsPathList);
	}

	@Override
	public Boolean createHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj) {
		Boolean bool = seaBoxFeignService
				.createHdfsQNAndSQNAndOwner(hdfsSetDirObj);
		return bool;
	}

	@Override
	public Result<Boolean> updataHdfsQNAndSQNAndOwner(
			HdfsSetDirObj hdfsSetDirObj) {
		return seaBoxFeignService.updataHdfsQNAndSQNAndOwner(hdfsSetDirObj);
	}

	@Override
	public ArrayList<HdfsDirObj> getHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		ArrayList<HdfsDirObj> list = seaBoxFeignService.getHdfsSaveObjList(
				clusterId, hdfsPath);
		// 查询hdfs路径的过期时间
		String pathPre = hdfsPath;
		if (!hdfsPath.endsWith("/")) {
			pathPre += "/";
		}
		String finalPathPre = pathPre;
		List<String> pathList = list.stream()
				.map(path -> finalPathPre + path.getDirName())
				.collect(Collectors.toList());
		List<SdpsDirExpire> dirExpires = dirExpireService.selectByPathList(
				clusterId, pathList);
		// 将查询出的过期时间放入结果集中
		Map<String, Integer> dirExpiresMap = Maps.newHashMap();
		dirExpires.forEach(dir -> {
			String path = dir.getPath();
			String dirName = path.substring(path.lastIndexOf("/") + 1);
			dirExpiresMap.put(dirName, dir.getExpireDay());
		});
		list.forEach(hdfsDirObj -> {
			Integer expireDay = dirExpiresMap.get(hdfsDirObj.getDirName());
			if (expireDay != null) {
				hdfsDirObj.setExpireDay(expireDay);
			}
		});
		return list;
	}

	@Override
	public Result<List<HdfsFSObj>> selectHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		return seaBoxFeignService.selectHdfsSaveObjList(clusterId, hdfsPath);
	}

	@Override
	public HdfsDirObj selectHdfsQNAndSQN(Integer clusterId, String hdfsPath) {
		return seaBoxFeignService.selectHdfsQNAndSQN(clusterId, hdfsPath);
	}

	@Override
	public Map selectAllQueueTree(Integer clusterId) {
		Map result = seaBoxFeignService.selectAllQueueTree(clusterId);
		return result;
	}

	@Override
	public VXUsers getRangerUserByName(Integer clusterId, String userName) {
		return seaBoxFeignService.getRangerUserByName(clusterId, userName);
	}

	@Override
	public Boolean addRangerUser(Integer clusterId,
			ArrayList<VXUsers> rangerObjList) {
		return seaBoxFeignService.addRangerUser(clusterId, rangerObjList);
	}

	@Override
	public Boolean deleteRangerUserByName(Integer clusterId, String userName) {
		return seaBoxFeignService.deleteRangerUserByName(clusterId, userName);
	}

	@Override
	public Boolean updateRangerUserByName(Integer clusterId,
			VXUsers rangerUserObj) {
		return seaBoxFeignService.updateRangerUserByName(clusterId,
				rangerUserObj);
	}

	@Override
	public VXGroups getRangerGroupByName(Integer clusterId, String groupName) {
		return seaBoxFeignService.getRangerGroupByName(clusterId, groupName);
	}

	@Override
	public Boolean addRangerGroup(Integer clusterId, VXGroups rangerGroupObj) {
		return seaBoxFeignService.addRangerGroup(clusterId, rangerGroupObj);
	}

	@Override
	public Boolean deleteRangerGroupByName(Integer clusterId, String groupName) {
		return seaBoxFeignService.deleteRangerGroupByName(clusterId, groupName);
	}

	@Override
	public Boolean updateRangerGroupByName(Integer clusterId,
			VXGroups rangerGroupObj) {
		return seaBoxFeignService.updateRangerGroupByName(clusterId,
				rangerGroupObj);
	}

	@Override
	public RangerGroupUser getUsersByGroupName(Integer clusterId,
			String groupName) {
		return seaBoxFeignService.getUsersByGroupName(clusterId, groupName);
	}

	@Override
	public Result addUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		return seaBoxFeignService.addUsersToGroup(clusterId, groupName,
				rangerUsers);
	}

	@Override
	public Result deleteUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		return seaBoxFeignService.deleteUsersToGroup(clusterId, groupName,
				rangerUsers);
	}

	@Override
	public Result addRangerPolicy(RangerPolicyObj rangerPolicyObj) {
		return seaBoxFeignService.addRangerPolicy(rangerPolicyObj);
	}

	@Override
	public Result queryRangerPolicy(Integer clusterId,String serviceType,String policyName) {
		return seaBoxFeignService.queryRangerPolicy(clusterId,serviceType,policyName);
	}

	@Override
	public Result likeRangerPolicy(Integer clusterId,String serviceType,String policyName) {
		return seaBoxFeignService.likeRangerPolicy(clusterId,serviceType,policyName);
	}

	@Override
	public Result deleteRangerPolicy(Integer clusterId,String serviceType,String policyName) {
		return seaBoxFeignService.deleteRangerPolicy(clusterId,serviceType,policyName);
	}

	@Override
	public JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject) {
		return seaBoxFeignService.modifyQueue(clusterId, jsonObject);
	}

	@Override
	public JSONObject listScheduler(Integer clusterId) {
		return seaBoxFeignService.listScheduler(clusterId);
	}

	@Override
	public JSONObject listApps(ApplicationRequest request) {
		return seaBoxFeignService.listApps(request.getClusterId());
	}

	@Override
	public JSONObject listMetrics(Integer clusterId) {
		return seaBoxFeignService.listMetrics(clusterId);
	}

	@Override
	public JSONObject listNodes(Integer clusterId) {
		return seaBoxFeignService.listNodes(clusterId);
	}

	@Override
	public Integer getWarningCnt(Integer clusterId) {
		return seaBoxFeignService.getWarningCnt(clusterId);
	}

	@Override
	public JSONObject getYarnAndHdfsMetrics(Integer clusterId) {
		return seaBoxFeignService.getYarnAndHdfsMetrics(clusterId);
	}

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
	@Override
	public Result copyFromLocalFile(String username, Integer clusterId,
			MultipartFile file, String path,boolean isUserFile, boolean isCrypto) {
		return seaBoxFeignService.copyFromLocalFile(username, clusterId, file,
				path, isUserFile, isCrypto);
	}

	@Override
	public JSONObject getClusterStackAndVersions(Integer clusterId) {
		return seaBoxFeignService.getClusterStackAndVersions(clusterId);
	}

	@Override
	public JSONObject getClusterStackAndVersionsNew(Integer clusterId,
			String repositoryVersion) {
		return seaBoxFeignService.getClusterStackAndVersionsNew(clusterId,
				repositoryVersion);
	}

	@Override
	public JSONObject getServiceUsersAndGroups(Integer clusterId,
			String services) {
		return seaBoxFeignService.getServiceUsersAndGroups(clusterId, services);
	}

	@Override
	public JSONObject getClusterHostInfo(Integer clusterId, String query) {
		return seaBoxFeignService.getClusterHostInfo(clusterId, query);
	}

	@Override
	public JSONObject getClusterHostDiskInfo(Integer clusterId, String query) {
		return seaBoxFeignService.getClusterHostDiskInfo(clusterId, query);
	}

	@Override
	public JSONObject getClusterServiceAutoStart(Integer clusterId) {
		return seaBoxFeignService.getClusterServiceAutoStart(clusterId);
	}

	@Override
	public JSONObject getServiceDisplayName(Integer clusterId) {
		return seaBoxFeignService.getServiceDisplayName(clusterId);
	}

	@Override
	public JSONObject getServiceInstalled(Integer clusterId) {
		return seaBoxFeignService.getServiceInstalled(clusterId);
	}

	@Override
	public JSONObject getWarningInfo(Integer clusterId) {
		return seaBoxFeignService.getWarningInfo(clusterId);
	}

	@Override
	public JSONObject getServiceWarningInfo(Integer clusterId,
			Integer definitionId, Integer from, Integer size) {
		return seaBoxFeignService.getServiceWarningInfo(clusterId,
				definitionId, from, size);
	}

	@Override
	public Result updateServiceAutoStart(AmbariServiceAutoStartObj obj) {
		return seaBoxFeignService.updateServiceAutoStart(obj);
	}

	@Override
	public Result<JSONObject> copyFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		return seaBoxFeignService.copyFileFromHDFS(username, clusterId,
				srcPath, destPath);
	}

	@Override
	public Result<JSONObject> moveFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		return seaBoxFeignService.moveFileFromHDFS(username, clusterId,
				srcPath, destPath);
	}

	@Override
	public JSONObject getClusterIp(Integer clusterId) {
		return seaBoxFeignService.getClusterIp(clusterId);
	}

	@Override
	public JSONObject getClusterHost(Integer clusterId) {
		return seaBoxFeignService.getClusterHost(clusterId);
	}

	@Override
	public JSONObject getClusterHosts(SdpsServerInfo sdpsServerInfo) {
		return seaBoxFeignService.getClusterHosts(sdpsServerInfo);
	}

	@Override
	public JSONObject validatePlatformAccountPaaswd(Map<String, Object> hostMap) {
		return seaBoxFeignService.validatePlatformAccountPaaswd(hostMap);
	}

	@Override
	public JSONObject getClusterName(Integer clusterId) {
		return seaBoxFeignService.getClusterName(clusterId);
	}

	@Override
	public JSONObject startOrStopService(AmbariStartOrStopServiceObj obj) {
		return seaBoxFeignService.startOrStopService(obj);
	}

	@Override
	public JSONObject getComponentInfo(Integer clusterId) {
		return seaBoxFeignService.getComponentInfo(clusterId);
	}

	@Override
	public JSONObject restartAllComponent(JSONObject data) {
		return seaBoxFeignService.restartAllComponent(data);
	}

	@Override
	public JSONObject startOrStopComponent(JSONObject data) {
		return seaBoxFeignService.startOrStopComponent(data);
	}

	@Override
	public JSONObject restartComponent(JSONObject data) {
		return seaBoxFeignService.restartComponent(data);
	}

	@Override
	public boolean permission(Integer clusterId, String path, String permission) {
		return seaBoxFeignService.permission(clusterId, path, permission);
	}

	@Override
	public boolean rename(Integer clusterId, String oldPath, String newPath) {
		return seaBoxFeignService.rename(clusterId, oldPath, newPath);
	}

	@Override
	public Response download(String username, Integer clusterId, String path) {
		return seaBoxFeignService.download(username, clusterId, path);
	}

	@Override
	public PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		return seaBoxFeignService.getItemStorage(storgeRequest);
	}

	@Override
	public String getServerConfByConfName(Integer clusterId, String serverName,
			List<String> confStrs) {
		return seaBoxFeignService.getServerConfByConfName(clusterId,
				serverName, confStrs);
	}

	@Override
	public String getYarnApplicationLogUrl(Integer clusterId) {
		return seaBoxFeignService.getYarnApplicationLogUrl(clusterId);
	}

	@Override
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			StorgeRequest storgeRequest) {
		return seaBoxFeignService.getFileStorageByTenant(storgeRequest);
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		return seaBoxFeignService.subStorgeTrend(storgeRequest);
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		return seaBoxFeignService.subStorgeRank(storgeRequest);
	}

	@Override
	public List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN,
			Long startTime, Long endTime) {
		return seaBoxFeignService.usedMemoryInfo(clusterId, topN, startTime,
				endTime);
	}

	@Override
	public JSONObject listAppsByUser(Integer clusterId, String user) {
		return seaBoxFeignService.listAppsByUser(clusterId, user);
	}

	@Override
	public JSONObject performOperation(Integer id) {
		return seaBoxFeignService.performOperation(id);
	}

	@Override
	public JSONObject listAppsByStates(Integer clusterId, String[] states) {
		return seaBoxFeignService.listAppsByStates(clusterId, states);
	}

	@Override
	public JSONObject performOperationDetail(Integer id, Integer nodeId) {
		return seaBoxFeignService.performOperationDetail(id, nodeId);
	}

	@Override
	public JSONObject alarmMsg(Integer id) {
		return seaBoxFeignService.alarmMsg(id);
	}

	@Override
	public JSONObject listAppsByUserAndStates(Integer clusterId, String user,
			String[] states) {
		return seaBoxFeignService.listAppsByUserAndStates(clusterId, user,
				states);
	}

	@Override
	public JSONObject getYarnQueueConfigurate(Integer clusterId) {
		return seaBoxFeignService.getYarnQueueConfigurate(clusterId);
	}

	@Override
	public Result getStorageTopN(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<TopDTO> dirInfoDTOS = seaBoxFeignService.topN(
					dirRequest.getClusterId(), dirRequest.getPeriod(),
					dirRequest.getType(), dirRequest.getTopNum());
			log.info("getStorageTopN:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询topN报错", e);
			result.setCode(1);
			result.setMsg("查询topN报错");
		}
		return result;
	}

	@Override
	public Result getResourceStatByPage(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			IPage<DirInfoDTO> dirInfoDTOS = seaBoxFeignService
					.getResourceStatByPage(dirRequest.getClusterId(),
							dirRequest.getDurationDay(),
							dirRequest.getOrderColumn(),
							dirRequest.getPageSize(), dirRequest.getPageNo(),
							dirRequest.getDesc());
			log.info("getResourceStatByPage:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询存储情况列表报错", e);
			result.setCode(1);
			result.setMsg("查询存储情况列表报错");
		}
		return result;
	}

	@Override
	public Result getResourceByTenant(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<DirInfoDTO> dirInfoDTOS = seaBoxFeignService
					.getResourceByTenant(dirRequest.getClusterId(),
							dirRequest.getDurationDay(),
							dirRequest.getOrderColumn(),
							dirRequest.getTenant(), dirRequest.getDesc());
			log.info("getResourceStatByPage:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询存储情况列表报错", e);
			result.setCode(1);
			result.setMsg("查询存储情况列表报错");
		}
		return result;
	}

	@Override
	public Result selectStorageTrend(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<DirInfoDTO> dirInfoDTOS = seaBoxFeignService
					.selectStorageTrend(dirRequest.getClusterId(), dirRequest
							.getStartDay(), dirRequest.getEndDay(), dirRequest
							.getPath(), dirRequest.getStorageType().name(),
							dirRequest.getDbName(), dirRequest.getTable(),
							dirRequest.getCategory());
			log.info("selectStorageTrend:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("询存储资源变化趋势报错", e);
			result.setCode(1);
			result.setMsg("询存储资源变化趋势报错");
		}
		return result;
	}

	@Override
	public Result selectPathSelections(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<String> dirInfoDTOS = seaBoxFeignService.selectPathSelections(
					dirRequest.getClusterId(), dirRequest.getPath(),
					QueryEnum.PATH.name());
			log.info("selectStorageTrend:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询子路径报错", e);
			result.setCode(1);
			result.setMsg("查询子路径报错");
		}
		return result;
	}

	@Override
	public Result selectDatabaseSelections(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<String> dirInfoDTOS = seaBoxFeignService
					.selectDatabaseSelections(dirRequest.getClusterId(),
							QueryEnum.DB.name(), dirRequest.getDbName());
			log.info("selectDatabaseSelections:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询库列表报错", e);
			result.setCode(1);
			result.setMsg("查询库列表报错");
		}
		return result;
	}

	@Override
	public Result selectTableSelections(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<String> dirInfoDTOS = seaBoxFeignService
					.selectTableSelections(dirRequest.getClusterId(),
							QueryEnum.TABLE.name(), dirRequest.getDbName(),
							dirRequest.getTable(), dirRequest.getSubPath());
			log.info("selectTableSelections:{}", dirInfoDTOS);
			result.setData(dirInfoDTOS);
		} catch (Exception e) {
			log.error("查询表报错", e);
			result.setCode(1);
			result.setMsg("查询表报错");
		}
		return result;
	}

	@Override
	public Result selectDiffStorage(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			DirInfoDTO dirInfoDTO = seaBoxFeignService.selectDiffStorage(
					dirRequest.getClusterId(), dirRequest.getStartDay(),
					dirRequest.getEndDay(), dirRequest.getStorageType().name(),
					dirRequest.getPath(), dirRequest.getDbName(),
					dirRequest.getTable(), dirRequest.getCategory());
			log.info("selectDiffStorage:{}", dirInfoDTO);
			result.setData(dirInfoDTO);
		} catch (Exception e) {
			log.error("存储地图->查询存储量报错", e);
			result.setCode(1);
			result.setMsg("存储地图->查询存储量报错");
		}
		return result;
	}

	@Override
	public Result selectStorageRank(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			List<DirInfoDTO> list = seaBoxFeignService.selectStorageRank(
					dirRequest.getClusterId(), dirRequest.getStorageType()
							.name(), dirRequest.getType(),
					dirRequest.getPath(), dirRequest.getPathDepth(), dirRequest
							.getDbName(), dirRequest.getCategory(), dirRequest
							.getTopNum());
			log.info("selectStorageRank:{}", list);
			result.setData(list);
		} catch (Exception e) {
			log.error("存储地图->排行报错", e);
			result.setCode(1);
			result.setMsg("存储地图->排行报错");
		}
		return result;
	}

	@Override
	public Result getFsContent(DirRequest dirRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest
				.getClusterId()));
		try {
			HdfsFSObj fsContent = seaBoxFeignService.getFsContent(
					dirRequest.getClusterId(), dirRequest.getPath());
			log.info("getFsContent:{}", fsContent);
			result.setData(fsContent);
		} catch (Exception e) {
			log.error("获取hdfs已使用容量和总容量报错", e);
			result.setCode(1);
			result.setMsg("获取hdfs已使用容量和总容量报错");
		}
		return result;
	}

	@Override
	public Result checkTableMerge(FileMergeRequest fileMergeRequest) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(fileMergeRequest
				.getClusterId()));
		try {
			HiveTableDTO hiveTableDTO = seaBoxFeignService.checkTableMerge(
					fileMergeRequest.getClusterId(),
					fileMergeRequest.getDbName(), fileMergeRequest.getTable(),
					fileMergeRequest.getType());
			log.info("checkTableMerge:{}", hiveTableDTO);
			result.setData(hiveTableDTO);
		} catch (Exception e) {
			log.error("checkTableMerge报错", e);
			result.setCode(1);
			result.setMsg("checkTableMerge报错");
		}
		return result;
	}

	@Override
	public Result getFileMergeDetail(FileMergeDetailRequest request) {
		Result result = new Result();
		result.setCode(0);
		// 判断是否有集群可用
		clusterDevOpsService.getEnableClusterList(Lists.list(request
				.getClusterId()));
		try {
			FileMergeDTO fileMergeDetail = seaBoxFeignService
					.getFileMergeDetail(request.getClusterId(),
							request.getDbName(), request.getTable(),
							request.getType(), request.getPath(),
							request.getStartTime(), request.getEndTime());
			log.info("getFileMergeDetail:{}", fileMergeDetail);
			result.setData(fileMergeDetail);
		} catch (Exception e) {
			log.error("getFileMergeDetail报错", e);
			result.setCode(1);
			result.setMsg("getFileMergeDetail报错");
		}
		return result;
	}

	@Override
	public MergeSummaryInfo getMergeSummaryInfo(Integer clusterId) {
		return seaBoxFeignService.getMergeSummaryInfo(clusterId);
	}

	@Override
	public SmallFileRankingTopN getMergeFileTopN(Integer clusterId,
			Integer topN, Integer day) {
		return seaBoxFeignService.getMergeFileTopN(clusterId, topN, day);
	}

	@Override
	public Result mergeHdfsFileExec(DispatchJobRequest dispatchJobRequest) {
		return seaBoxFeignService.mergeHdfsFileExec(dispatchJobRequest);
	}

	@Override
	public Result mergeHdfsFileRetry(Integer clusterId, Integer submitId) {
		return seaBoxFeignService.mergeHdfsFileRetry(clusterId, submitId);
	}

	@Override
	public Result analyseHdfsMetaDataRetry(Integer clusterId, Integer taskId) {
		return seaBoxFeignService.analyseHdfsMetaDataRetry(clusterId, taskId);
	}

	@Override
	public Result updateYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return seaBoxFeignService.updateYarnQueueConfigurate(clusterId, infos);
	}

	@Override
	public List<SdpsCoresMBInfo> sdpsCoresAndMemoryRank(Integer clusterId,
			String type, Integer topN, String startTime, String endTime) {

		// 转换时间
		Long start = DateUtil.strToTimestamp(startTime,
				DateUtil.DATE_FORMAT_YYYYMMDD);
		//获取结束时间的24点
		Long end = DateUtil.strToTimestamp(endTime,
				DateUtil.DATE_FORMAT_YYYYMMDD) + 24 * 60 * 60 * 1000;

		// 获取时间范围内的nodeId
		List<String> nodeList = coresAndMBInfoMapper.getNodeIdByDuration(
				clusterId, start, end);
		if (nodeList == null) {
			nodeList = new ArrayList<>();
		}
		// 遍历集合，依次查询节点近一个小时中位数
		if (DatabaseConstants.CORE.equals(type)) {
			type = DatabaseConstants.USED_CORES;
		} else if (DatabaseConstants.MEMORY.equals(type)) {
			type = DatabaseConstants.USED_MEMORY;
		}
		String finalType = type;

		Stream<SdpsCoresMBInfo> stream = nodeList
				.stream()
				.map(node -> {
					Double median = coresAndMBInfoMapper.getMedianCurrentHour(
							clusterId, finalType, node, start, end);
					SdpsCoresMBInfo info = new SdpsCoresMBInfo();
					info.setClusterId(clusterId);
					info.setNodeId(node);
					// 根据类型传入不同的值
					if (DatabaseConstants.USED_CORES.equals(finalType)) {
						info.setUsedCores(median.intValue());
					} else if (DatabaseConstants.USED_MEMORY.equals(finalType)) {
						info.setUsedMemory(median.intValue());
					}
					return info;
				});
		if (DatabaseConstants.USED_CORES.equals(finalType)) {
			stream = stream.sorted(Comparator.comparing(
					SdpsCoresMBInfo::getUsedCores).reversed());
		} else if (DatabaseConstants.USED_MEMORY.equals(finalType)) {
			stream = stream.sorted(Comparator.comparing(
					SdpsCoresMBInfo::getUsedMemory).reversed());
		}
		List<SdpsCoresMBInfo> data = stream.limit(topN).collect(
				Collectors.toList());
		return data;
	}

	/**
	 * 保留两位小数
	 * 
	 * @param data
	 * @return
	 */
	private static Double reserveTwoDecimal(Double data) {
		if (data == null) {
			return 0.0;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.parseDouble(df.format(data));
	}

	@Override
	public Result deleteYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return seaBoxFeignService.deleteYarnQueueConfigurate(clusterId, infos);
	}

	@Override
	public Result insertYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return seaBoxFeignService.insertYarnQueueConfigurate(clusterId, infos);
	}

	@Override
	public Result stopOrRunningYarnQueue(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return seaBoxFeignService.stopOrRunningYarnQueue(clusterId, infos);
	}

	@Override
	public Result addAmbariUser(Integer clusterId, AmbariUser ambariUser) {
		return seaBoxFeignService.addAmbariUser(clusterId, ambariUser);
	}

	@Override
	public Result deleteAmbariUser(Integer clusterId, String username) {
		return seaBoxFeignService.deleteAmbariUser(clusterId, username);
	}

	@Override
	public Result updateAmbariUserPassword(Integer clusterId,
			AmbariUser ambariUser) {
		return seaBoxFeignService.updateAmbariUserPassword(clusterId,
				ambariUser);
	}

	@Override
	public Result createItemResource(String itemIden,
			HdfsSetDirObj hdfsSetDirObj) {
		return seaBoxFeignService.createItemResource(itemIden, hdfsSetDirObj);
	}

	@Override
	public Result deleteItemFile(Integer clusterId, List<String> hdfsPaths) {
		return seaBoxFeignService.deleteItemFile(clusterId, hdfsPaths);
	}

	@Override
	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(Long userId,
			String username, Integer clusterId, String hdfsPath) {
		return seaBoxFeignService.selectHdfsSaveObjListByUser(userId,username,clusterId, hdfsPath);
	}

	@Override
	public Result<JSONArray> getAmbariUsers(Integer clusterId) {
		return seaBoxFeignService.getAmbariUsers(clusterId);
	}

	@Override
	public Result<JSONObject> findServerKerberosInfo(Integer clusterId) {
		return seaBoxFeignService.findServerKerberosInfo(clusterId);
	}
}
