package com.seaboxdata.sdps.bigdataProxy.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsClusterType;
import com.seaboxdata.sdps.bigdataProxy.vo.DevOpsRequest;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterStatus;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;

public interface IClusterDevOpsService extends ISuperService<SdpsCluster> {

    List<SdpsCluster> getEnableClusterList(List<Integer> clusterIds);

    JSONObject warningCnt(List<Integer> clusterIds);

    SysGlobalArgs selectGlobalArgs(String type, String key);

    List<SysGlobalArgs> getGlobalParams(String type);

    JSONObject getHdfsAndYarnMetrics(Integer page, Integer size,
                                     List<Integer> clusterIds);

    JSONObject getClusterStackAndVersions(Integer clusterId);

    /**
     * 获取替换后的集群版本及组件新版本及名称
     *
     * @param clusterId         集群ID
     * @param repositoryVersion 仓库版本
     */
    JSONObject getClusterStackAndVersionsNew(Integer clusterId, String repositoryVersion);

    JSONObject getServiceUsersAndGroups(Integer clusterId, String services);

    JSONObject getClusterHostInfo(Integer clusterId, String query);

    JSONObject getClusterHostDiskInfo(Integer clusterId, String query);

    JSONObject getClusterServiceAutoStart(Integer clusterId);

    JSONObject getServiceDisplayName(Integer clusterId);

    JSONObject getServiceInstalled(Integer clusterId);

    JSONObject getWarningInfo(Integer clusterId);

    JSONObject getServiceWarningInfo(Integer clusterId, Integer definitionId, Integer from, Integer from2);

    Result updateServiceAutoStart(AmbariServiceAutoStartObj obj);

    JSONObject getClusterIp(Integer clusterId);

    JSONObject getClusterHost(Integer clusterId);

    JSONObject getClusterName(Integer clusterId);

    JSONObject startOrStopService(AmbariStartOrStopServiceObj obj);

    JSONObject getComponentInfo(Integer clusterId);

    JSONObject restartAllComponent(JSONObject data);

    JSONObject startOrStopComponent(JSONObject data);

    JSONObject restartComponent(JSONObject data);

	void saveServerInfo(SdpsServerInfo serverInfo);

	PageResult getServerInfoPage(PageRequest<DevOpsRequest> request);

    void updateServerInfo(SdpsServerInfo serverInfo);

	void deleteServerInfoById(Long id);

	void addClusterInfo(SdpsCluster sdpsCluster);

	List<SdpsClusterType> getClusterTypeList();

	List<SdpsClusterStatus> getClusterStatusList();

	void updateClusterInfo(SdpsCluster sdpsCluster);

	void removeCluster(Integer clusterId);
}
