package com.seaboxdata.sdps.bigdataProxy.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
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

public interface MultiPlatformService {

    Boolean makeHdfsPath(String username, Integer clusterId, String createHdfsPath);

    Result deleteFile(String username, Integer clusterId, List<String> hdfsPath, Boolean flag);

    Boolean cleanHdfsDir(Integer clusterId, ArrayList<String> hdfsPathList);

    Boolean createHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj);

    Result<Boolean> updataHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj);

	ArrayList<HdfsDirObj> getHdfsSaveObjList(Integer clusterId, String hdfsPath);

    Result<List<HdfsFSObj>> selectHdfsSaveObjList(Integer clusterId, String hdfsPath);

    HdfsDirObj selectHdfsQNAndSQN(Integer clusterId, String hdfsPath);

    Map selectAllQueueTree(Integer clusterId);

    VXUsers getRangerUserByName(Integer clusterId, String userName);

    Boolean addRangerUser(Integer clusterId, ArrayList<VXUsers> rangerObjList);

    Boolean deleteRangerUserByName(Integer clusterId, String userName);

    Boolean updateRangerUserByName(Integer clusterId, VXUsers rangerUserObj);

    VXGroups getRangerGroupByName(Integer clusterId, String groupName);

    Boolean addRangerGroup(Integer clusterId, VXGroups rangerGroupObj);

    Boolean deleteRangerGroupByName(Integer clusterId, String groupName);

    Boolean updateRangerGroupByName(Integer clusterId, VXGroups rangerGroupObj);

    RangerGroupUser getUsersByGroupName(Integer clusterId, String groupName);

    Result addUsersToGroup(Integer clusterId, String groupName, List<String> rangerUsers);

    Result deleteUsersToGroup(Integer clusterId, String groupName, List<String> rangerUsers);

	Result addRangerPolicy(RangerPolicyObj rangerPolicyObj);

	Result queryRangerPolicy(Integer clusterId,String serviceType,String policyName);

	Result likeRangerPolicy(Integer clusterId,String serviceType,String policyName);

	Result deleteRangerPolicy(Integer clusterId,String serviceType,String policyName);
    /**
     * 增删改队列
     * @param clusterId 集群ID
     * @param jsonObject 请求参数
     * @return 是否删除成功
     */
    JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject);

    /**
     * 查询大数据集群队列信息
     * @return 队列信息
     */
    JSONObject listScheduler(Integer clusterId);

    /**
     * 查询大数据集群任务列表
     * @param request
     * @return 任务列表
     */
    JSONObject listApps(ApplicationRequest request);

    /**
     * 查询集群资源配置信息
     * @param clusterId 集群ID
     * @return 集群资源配置信息。
     */
    JSONObject listMetrics(Integer clusterId);

    /**
     * 查询集群各个节点信息
     * @param clusterId 集群ID
     * @return 集群各个节点信息
     */
    JSONObject listNodes(Integer clusterId);
    
    /**
     * 查询节点警告数量
     * @param clusterId
     * @return
     */
    Integer getWarningCnt(Integer clusterId);

	JSONObject getYarnAndHdfsMetrics(Integer clusterId);

    /**
     * put操作
     * @param clusterId 集群ID
     * @param file 源文件
     * @param path 目标路径
     * @return 是否上传成功
     */
	Result copyFromLocalFile(String username, Integer clusterId, MultipartFile file, String path,boolean isUserFile, boolean isCrypto);
    
    /**
     * 获得集群版本信息
     * @param clusterId
     * @return
     */
	JSONObject getClusterStackAndVersions(Integer clusterId);

	/**
	 * 获得集群版本信息
	 *
	 * @param clusterId         集群ID
	 * @param repositoryVersion 仓库版本
	 */
	JSONObject getClusterStackAndVersionsNew(Integer clusterId, String repositoryVersion);

	/**
	 * 获取集群service的users和groups
	 * @param clusterId
	 * @param services 
	 * @return
	 */
	JSONObject getServiceUsersAndGroups(Integer clusterId, String services);

	/**
	 * 查询集群host信息
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterHostInfo(Integer clusterId, String query);

	/**
	 * 查询集群节点磁盘信息
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterHostDiskInfo(Integer clusterId, String query);

	/**
	 * 查询集群自启动服务
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterServiceAutoStart(Integer clusterId);

	/**
	 * 查询集群service名称和display名称
	 * @param clusterId
	 * @return
	 */
	JSONObject getServiceDisplayName(Integer clusterId);

	/**
	 * 查询已安装的服务
	 * @param clusterId
	 * @return
	 */
	JSONObject getServiceInstalled(Integer clusterId);

	/**
	 * 查询集群警告信息
	 * @param clusterId
	 * @return
	 */
	JSONObject getWarningInfo(Integer clusterId);

	/**
	 * 获取某个服务的报警
	 * @param clusterId
	 * @param definitionId
	 * @param from
	 * @param size
	 * @return
	 */
	JSONObject getServiceWarningInfo(Integer clusterId, Integer definitionId,
			Integer from, Integer size);

	/**
	 * 更新服务自启动
	 * @param obj
	 * @return
	 */
	Result updateServiceAutoStart(AmbariServiceAutoStartObj obj);

	/**
	 * 复制HDFS文件
	 * @param clusterId 集群ID
	 * @param srcPath 待复制的文件路径
	 * @param destPath 目标文件路径
	 * @return 是否复制成功
	 */
    Result<JSONObject> copyFileFromHDFS(String username, Integer clusterId, String srcPath, String destPath);

	/**
	 * 移动HDFS文件
	 * @param clusterId 集群ID
	 * @param srcPath 待移动的文件路径
	 * @param destPath 目标文件路径
	 * @return 是否移动成功
	 */
	Result<JSONObject> moveFileFromHDFS(String username, Integer clusterId, String srcPath, String destPath);

	/**
	 * 获取集群ip
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterIp(Integer clusterId);

	/**
	 * 获取集群host
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterHost(Integer clusterId);

	/**
	 * 获取集群host
	 * @return
	 */
	JSONObject getClusterHosts(SdpsServerInfo sdpsServerInfo);

	/**
	 * 校验平台账号密码
	 *
	 * @param hostMap 主机信息
	 */
	JSONObject validatePlatformAccountPaaswd(Map<String, Object> hostMap);

	/**
	 * 获取集群名称信息
	 * @param clusterId
	 * @return
	 */
	JSONObject getClusterName(Integer clusterId);

	/**
	 * 停止或者开始服务
	 * @param obj
	 * @return
	 */
	JSONObject startOrStopService(AmbariStartOrStopServiceObj obj);

	/**
	 * 获取组件信息
	 * @param clusterId
	 * @return
	 */
	JSONObject getComponentInfo(Integer clusterId);

	/**
	 * 重启所有组件
	 * @param data
	 * @return
	 */
	JSONObject restartAllComponent(JSONObject data);

	/**
	 * 启动或者停止组件
	 * @param data
	 * @return
	 */
	JSONObject startOrStopComponent(JSONObject data);

	/**
	 * 重启组件
	 * @param data
	 * @return
	 */
	JSONObject restartComponent(JSONObject data);

	/**
	 * 修改文件权限
	 * @param clusterId 集群ID
	 * @param path 待修改的文件路径
	 * @param permission 新权限
	 * @return 是否修改成功
	 */
    boolean permission(Integer clusterId, String path, String permission);

	/**
	 * 文件重命名
	 * @param clusterId 集群ID
	 * @param oldPath 旧文件路径
	 * @param newPath 新文件路径
	 * @return 是否重命名成功
	 */
    boolean rename(Integer clusterId, String oldPath, String newPath);

	/**
	 * hdfs文件下载
	 * @param clusterId 集群ID
	 * @param path 待下载文件路径
	 * @return 是否下载成功
	 */
    Response download(String username, Integer clusterId, String path);

    /**
     * 得到项目存储信息
     * @return
     */
	PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest);

	String getServerConfByConfName(Integer clusterId, String serverName,
			List<String> confStrs);

	String getYarnApplicationLogUrl(Integer clusterId);

	PageResult<StorgeDirInfo> getFileStorageByTenant(StorgeRequest storgeRequest);

	PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest);

	PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest);

	List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN, Long startTime, Long endTime);

    JSONObject listAppsByUser(Integer clusterId, String user);

	JSONObject performOperation(Integer id);

    JSONObject listAppsByStates(Integer clusterId, String[] states);

	/**
	 * 获取集群执行操作内容
	 *
	 * @param id     集群ID
	 * @param nodeId 节点ID
	 */
	JSONObject performOperationDetail(Integer id, Integer nodeId);

	/**
	 * 告警消息
	 *
	 * @param id 集群ID
	 */
    JSONObject alarmMsg(Integer id);

    JSONObject listAppsByUserAndStates(Integer clusterId, String user, String[] states);

	JSONObject getYarnQueueConfigurate(Integer clusterId);
	/**
	 * 资源管理 -> 存储资源 -> 增长趋势
	 * @param dirRequest 请求参数
	 * @return
	 */
	Result getStorageTopN(DirRequest dirRequest);

	/**
	 * 资源管理 -> 存储资源 -> 增长趋势下面的列表
	 * @param dirRequest
	 * @return
	 */
	Result getResourceStatByPage(DirRequest dirRequest);

	/**
	 * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
	 * @param dirRequest
	 * @return
	 */
	Result getResourceByTenant(DirRequest dirRequest);

	/**
	 * 查询存储资源变化趋势
	 * @param dirRequest
	 * @return
	 */
	Result selectStorageTrend(DirRequest dirRequest);

	/**
	 * 存储资源趋势->路径下拉框
	 * @param dirRequest
	 * @return
	 */
	Result selectPathSelections(DirRequest dirRequest);

	/**
	 * 存储资源趋势->库下拉框
	 * @param dirRequest
	 * @return
	 */
	Result selectDatabaseSelections(DirRequest dirRequest);

	/**
	 * 存储资源趋势->表下拉框
	 * @param dirRequest
	 * @return
	 */
	Result selectTableSelections(DirRequest dirRequest);

	/**
	 * 存储地图 -> 开始、结束时间存储量
	 * @param dirRequest
	 * @return
	 */
	Result selectDiffStorage(DirRequest dirRequest);

	/**
	 * 存储地图 -> 存储资源使用排行
	 * @param dirRequest
	 * @return
	 */
	Result selectStorageRank(DirRequest dirRequest);

	/**
	 * 获取hdfs已使用容量和总容量
	 * @param dirRequest
	 * @return
	 */
	Result getFsContent(DirRequest dirRequest);

	/**
	 * 校验表是否能合并
	 * @param fileMergeRequest 请求参数
	 * @return
	 */
	Result checkTableMerge(FileMergeRequest fileMergeRequest);

	/**
	 * 回显要合并的路径信息
	 * @param fileMergeDetailRequest
	 * @return
	 */
	Result getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest);

	/**
	 * 获取合并小文件信息(小文件总数，以合并的小文件数，合并的小文件块大小)
	 * @param clusterId
	 * @return
	 */
	MergeSummaryInfo getMergeSummaryInfo(Integer clusterId);

	/**
	 * 获取小文件合并TopN
	 * @param clusterId
	 * @param topN
	 * @return
	 */
	SmallFileRankingTopN getMergeFileTopN(Integer clusterId, Integer topN,Integer day);

	/**
	 * 启动合并小文件
	 * @param dispatchJobRequest
	 * @return
	 */
	Result mergeHdfsFileExec(DispatchJobRequest dispatchJobRequest);

	/**
	 * 重试合并小文件
	 * @param clusterId
	 * @param submitId
	 * @return
	 */
	Result mergeHdfsFileRetry(Integer clusterId,Integer submitId);

	/**
	 * 重试分析HDFS元数据文件
	 * @param clusterId
	 * @param taskId
	 * @return
	 */
	Result analyseHdfsMetaDataRetry(Integer clusterId,Integer taskId);
	
	Result updateYarnQueueConfigurate(Integer clusterId, List<YarnQueueConfInfo> infos);

	/**
	 * 计算资源->资源使用率排行
	 * @param clusterId 集群id
	 * @param type      core或memory
	 * @param topN      取前几
	 * @return
	 */
	List<SdpsCoresMBInfo> sdpsCoresAndMemoryRank(Integer clusterId, String type, Integer topN, String startTime, String endTime);

	Result deleteYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos);

	Result insertYarnQueueConfigurate(Integer clusterId,
			List<YarnQueueConfInfo> infos);

	Result stopOrRunningYarnQueue(Integer clusterId,
			List<YarnQueueConfInfo> infos);

	Result addAmbariUser(Integer clusterId, AmbariUser ambariUser);

	Result deleteAmbariUser(Integer clusterId, String username);

	Result updateAmbariUserPassword(Integer clusterId, AmbariUser ambariUser);

	Result createItemResource(String itemIden, HdfsSetDirObj hdfsSetDirObj);

	Result deleteItemFile(Integer clusterId, List<String> hdfsPaths);

	Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(Long userId,
			String username, Integer clusterId, String hdfsPath);

	Result<JSONArray> getAmbariUsers(Integer clusterId);

	Result<JSONObject> findServerKerberosInfo(Integer clusterId);

}
