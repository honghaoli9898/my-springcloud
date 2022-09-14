package com.seaboxdata.sdps.bigdataProxy.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHost;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 集群管理Service
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
public interface IClusterCreateService extends ISuperService<SdpsCluster> {

    Result<JSONObject> saveCluster(SdpsCluster sdpsCluster, SysUser user, String clusterName, List<JSONObject> serviceForComponentList, List<JSONObject> componentXmlList, List<JSONObject> hostAndComponentList) throws ExecutionException, InterruptedException;

    Result<JSONObject> saveAmabriCluster() throws Exception;

    Result<SdpsCluster> saveLocalCluster(SdpsCluster sdpsCluster) throws Exception;

    Result<String> validatePlatform(String masterIp);

    // Init
    Result<JSONObject> ambariRemoteInit(String masterIp, List<SdpsClusterHost> hostList) throws InterruptedException;

    // versionDefinition
    Result<JSONObject> ambariRemoteSecond(String masterIp, String clusterName, List<String> serviceNameList, List<JSONObject> clusterServiceConfigurations);

    // serviceName -> componentList
    Result<JSONObject> serviceAndComponentsSave(String masterIp, String clusterName, List<JSONObject> serviceAndComponentList);

    Result<JSONObject> saveClusterComponentHost(String masterIp, String clusterName, JSONObject hostAndComponentInfos);

    JSONObject putOsRepositoriesData(String masterIp);

    // 3-4 ======================================================
    JSONObject saveBootstrap(String masterIp);

    JSONObject getBootStrapStatus(String masterIp, String bootstrapRequestId);

    JSONObject saveRequests(String masterIp);

    // 4-5 ======================================================
    // "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[]},\"blueprint_cluster_binding\":{\"host_groups\":[]}}}";
    JSONObject saveRecommendations(String masterIp, String reqBody);

    JSONObject saveValidations(String masterIp, String reqBody);
     //    log.info("\n***************** 5-6 结束 *****************");


    JSONObject postClusterCurrentStatus(String masterIp, String currentStatus);

    // "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
    JSONObject saveVersionDefinition(String masterIp, String versionDefinition);
    // resp: {"resources":[{"href":"http://10.1.3.11:8080/api/v1/version_definitions/1","VersionDefinition":{"id":1,"stack_name":"HDP","stack_version":"3.1"}}]}

    // "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
    JSONObject saveCustomVersionDefinition(String masterIp, String repoVersion, String customVersionDefinition);
    
    // "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
    JSONObject saveClusterWithName(String masterIp, String clusterName, String reJson);
    
    JSONObject saveClusterService(String masterIp, String clusterName, String serviceJsonArr);
    
    JSONObject saveClusterServiceXmlConfigurations(String masterIp, String clusterName, String serviceXmlJsonArray);
    
    JSONObject saveClusterComponentNodes(String masterIp, String clusterName, String serviceName, String nodeJson);

    JSONObject saveClusterComponentHost(String masterIp, String clusterName, String hostInfo);

    JSONObject saveClusterComponentNodeState(String masterIp, String clusterName, String state);

    // http://10.1.3.11:8080/api/v1/persist/CLUSTER_CURRENT_STATUS?_=1641293272654
    JSONObject getClusterCurrentStatus(String masterIp);

    JSONObject saveBootstrapFirst(String masterIp, JSONArray hostInfos);

    JSONObject saveRequestSecond(String masterIp);

    JSONObject getOneAndSecondResult(String masterIp, String oneRequestId, String secondRequestId);

    JSONObject saveThirdRequestThird(String masterIp);

    JSONObject getThirdRequestResult(String masterIp, String thirdRequestId);

    JSONObject saveVersionAndClusterFour(String masterIp, String clusterName, List<String> serviceJsonArr, String clusterServiceXmlConfigurations);

    // INIT, INSTALLED
    JSONObject getRequestStatus(String masterIp, String clusterName, String requestId);

    JSONObject updateInstalledCluster(String masterIp, String clusterName);

    JSONObject lastPostCurrentStatus(String masterIp, String currnetStatus);

    Result<SdpsCluster> updateLocalCluster(Long id, String installStatus) throws Exception;

    JSONObject getRequestTaskResult(String masterIp, String clusterName, Long requestId, Long taskId);
    // JSONObject postClusterCurrentStatus(String masterIp, String clusterCurrentStatus);
}
