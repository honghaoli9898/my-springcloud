package com.seaboxdata.sdps.bigdataProxy.platform.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.bigdataProxy.feign.TbdsFeignService;
import com.seaboxdata.sdps.bigdataProxy.platform.MultiPlatformService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
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
@Service("tbds")
public class TbdsPlatform implements MultiPlatformService {

	@Autowired
	TbdsFeignService tbdsFeignService;

	@Override
	public Boolean makeHdfsPath(String username, Integer clusterId,
			String hdfsPath) {
		return null;
	}

	@Override
	public Result deleteFile(String username, Integer clusterId,
			List<String> hdfsPath, Boolean flag) {
		return null;
	}

	@Override
	public Boolean cleanHdfsDir(Integer clusterId,
			ArrayList<String> hdfsPathList) {
		return null;
	}

	@Override
	public Boolean createHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj) {
		return null;
	}

	@Override
	public Result<Boolean> updataHdfsQNAndSQNAndOwner(
			HdfsSetDirObj hdfsSetDirObj) {
		return null;
	}

	@Override
	public ArrayList<HdfsDirObj> getHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		return new ArrayList<>();
	}

	@Override
	public Result<List<HdfsFSObj>> selectHdfsSaveObjList(Integer clusterId,
			String hdfsPath) {
		return null;
	}

	@Override
	public HdfsDirObj selectHdfsQNAndSQN(Integer clusterId, String hdfsPath) {
		return null;
	}

	@Override
	public Map selectAllQueueTree(Integer clusterId) {
		return null;
	}

	@Override
	public VXUsers getRangerUserByName(Integer clusterId, String userName) {
		return null;
	}

	@Override
	public Boolean addRangerUser(Integer clusterId,
			ArrayList<VXUsers> rangerObjList) {
		return null;
	}

	@Override
	public Boolean deleteRangerUserByName(Integer clusterId, String userName) {
		return null;
	}

	@Override
	public Boolean updateRangerUserByName(Integer clusterId,
			VXUsers rangerUserObj) {
		return null;
	}

	@Override
	public VXGroups getRangerGroupByName(Integer clusterId, String groupName) {
		return null;
	}

	@Override
	public Boolean addRangerGroup(Integer clusterId, VXGroups rangerGroupObj) {
		return null;
	}

	@Override
	public Boolean deleteRangerGroupByName(Integer clusterId, String groupName) {
		return null;
	}

	@Override
	public Boolean updateRangerGroupByName(Integer clusterId,
			VXGroups rangerGroupObj) {
		return null;
	}

	@Override
	public RangerGroupUser getUsersByGroupName(Integer clusterId,
			String groupName) {
		return null;
	}

	@Override
	public Result addUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		return null;
	}

	@Override
	public Result deleteUsersToGroup(Integer clusterId, String groupName,
			List<String> rangerUsers) {
		return null;
	}

	@Override
	public Result addRangerPolicy(RangerPolicyObj rangerPolicyObj) {
		return null;
	}

	@Override
	public Result queryRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		return null;
	}

	@Override
	public Result likeRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		return null;
	}

	@Override
	public Result deleteRangerPolicy(Integer clusterId, String serviceType,
			String policyName) {
		return null;
	}

	@Override
	public JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject) {
		return null;
	}

	@Override
	public JSONObject listScheduler(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject listApps(ApplicationRequest request) {
		return null;
	}

	@Override
	public JSONObject listMetrics(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject listNodes(Integer clusterId) {
		return null;
	}

	@Override
	public Integer getWarningCnt(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getYarnAndHdfsMetrics(Integer clusterId) {
		return null;
	}

	@Override
	public Result copyFromLocalFile(String username, Integer clusterId,
			MultipartFile file, String path, boolean isUserFile,
			boolean isCrypto) {
		return null;
	}

	@Override
	public JSONObject getClusterStackAndVersions(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getClusterStackAndVersionsNew(Integer clusterId,
			String repositoryVersion) {
		return null;
	}

	@Override
	public JSONObject getServiceUsersAndGroups(Integer clusterId,
			String services) {
		return null;
	}

	@Override
	public JSONObject getClusterHostInfo(Integer clusterId, String query) {
		return null;
	}

	@Override
	public JSONObject getClusterHostDiskInfo(Integer clusterId, String query) {
		return null;
	}

	@Override
	public JSONObject getClusterServiceAutoStart(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getServiceDisplayName(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getServiceInstalled(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getWarningInfo(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getServiceWarningInfo(Integer clusterId,
			Integer definitionId, Integer from, Integer size) {
		return null;
	}

	@Override
	public Result updateServiceAutoStart(AmbariServiceAutoStartObj obj) {
		return null;
	}

	@Override
	public Result<JSONObject> copyFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		return null;
	}

	@Override
	public Result<JSONObject> moveFileFromHDFS(String username,
			Integer clusterId, String srcPath, String destPath) {
		return null;
	}

	@Override
	public JSONObject getClusterIp(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getClusterHost(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject getClusterHosts(SdpsServerInfo sdpsServerInfo) {
		return null;
	}

	@Override
	public JSONObject validatePlatformAccountPaaswd(Map<String, Object> hostMap) {
		return null;
	}

	@Override
	public JSONObject getClusterName(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject startOrStopService(AmbariStartOrStopServiceObj obj) {
		return null;
	}

	@Override
	public JSONObject getComponentInfo(Integer clusterId) {
		return null;
	}

	@Override
	public JSONObject restartAllComponent(JSONObject data) {
		return null;
	}

	@Override
	public JSONObject startOrStopComponent(JSONObject data) {
		return null;
	}

	@Override
	public JSONObject restartComponent(JSONObject data) {
		return null;
	}

	@Override
	public boolean permission(Integer clusterId, String path, String permission) {
		return false;
	}

	@Override
	public boolean rename(Integer clusterId, String oldPath, String newPath) {
		return false;
	}

	@Override
	public Response download(String username, Integer clusterId, String path) {
		return null;
	}

	@Override
	public PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		return null;
	}

	@Override
	public String getServerConfByConfName(Integer clusterId, String serverName,
			List<String> confStrs) {
		return null;
	}

	@Override
	public String getYarnApplicationLogUrl(Integer clusterId) {
		return null;
	}

	@Override
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			StorgeRequest storgeRequest) {
		return null;
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		return null;
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		return null;
	}

	@Override
	public List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN,
			Long startTime, Long endTime) {
		return null;
	}

	@Override
	public JSONObject listAppsByUser(Integer clusterId, String user) {
		return null;
	}

	@Override
	public JSONObject performOperation(Integer id) {
		return null;
	}

	@Override
	public JSONObject listAppsByStates(Integer clusterId, String[] states) {
		return null;
	}

	@Override
	public JSONObject performOperationDetail(Integer id, Integer nodeId) {
		return null;
	}

	@Override
	public JSONObject alarmMsg(Integer id) {
		return null;
	}

	@Override
	public JSONObject listAppsByUserAndStates(Integer clusterId, String user,
			String[] states) {
		return null;
	}

	@Override
	public JSONObject getYarnQueueConfigurate(Integer clusterId) {
		return null;
	}

	@Override
	public Result getStorageTopN(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result getResourceStatByPage(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result getResourceByTenant(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectStorageTrend(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectPathSelections(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectDatabaseSelections(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectTableSelections(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectDiffStorage(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result selectStorageRank(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result getFsContent(DirRequest dirRequest) {
		return null;
	}

	@Override
	public Result checkTableMerge(FileMergeRequest fileMergeRequest) {
		return null;
	}

	@Override
	public Result getFileMergeDetail(
			FileMergeDetailRequest fileMergeDetailRequest) {
		return null;
	}

	@Override
	public MergeSummaryInfo getMergeSummaryInfo(Integer clusterId) {
		return null;
	}

	@Override
	public SmallFileRankingTopN getMergeFileTopN(Integer clusterId,
			Integer topN, Integer day) {
		return null;
	}

	@Override
	public Result mergeHdfsFileExec(DispatchJobRequest dispatchJobRequest) {
		return null;
	}

	@Override
	public Result mergeHdfsFileRetry(Integer clusterId, Integer submitId) {
		return null;
	}

	@Override
	public Result analyseHdfsMetaDataRetry(Integer clusterId, Integer taskId) {
		return null;
	}

	@Override
	public Result updateYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return null;
	}

	@Override
	public List<SdpsCoresMBInfo> sdpsCoresAndMemoryRank(Integer clusterId,
			String type, Integer topN, String startTime, String endTime) {
		return null;
	}

	@Override
	public Result deleteYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return null;
	}

	@Override
	public Result insertYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return null;
	}

	@Override
	public Result stopOrRunningYarnQueue(Integer clusterId,
			List<YarnQueueConfInfo> infos) {
		return null;
	}

	@Override
	public Result addAmbariUser(Integer clusterId, AmbariUser ambariUser) {
		return null;
	}

	@Override
	public Result deleteAmbariUser(Integer clusterId, String username) {
		return null;
	}

	@Override
	public Result updateAmbariUserPassword(Integer clusterId,
			AmbariUser ambariUser) {
		return null;
	}

	@Override
	public Result createItemResource(String itemIden,
			HdfsSetDirObj hdfsSetDirObj) {
		return null;
	}

	@Override
	public Result deleteItemFile(Integer clusterId, List<String> hdfsPaths) {
		return null;
	}

	@Override
	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(Long userId,
			String username, Integer clusterId, String hdfsPath) {
		return null;
	}

	@Override
	public Result<JSONArray> getAmbariUsers(Integer clusterId) {
		return null;
	}

	@Override
	public Result<JSONObject> findServerKerberosInfo(Integer clusterId) {
		return null;
	}

}
