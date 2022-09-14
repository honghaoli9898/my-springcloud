package com.seaboxdata.sdps.bigdataProxy.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;

public interface IClusterConfigService {

    Result getServiceConfigVersions(Integer clusterId, Integer page_size, Integer from, String sortBy
            , String service_name, String createtime);

    Result getComponentAndHost(Integer clusterId, String serviceName);

    Result configThemes(Integer clusterId, String serviceName);

    Result getConfigInfo(Integer clusterId, String serviceName);

    Result getConfigAllVersion(Integer clusterId, String serviceName);

    Result getConfigGroup(Integer clusterId, String serviceName);

    Result getConfigHostInfo(Integer clusterId);

    Result updateConfigGroup(ConfigGroup configGroup);

    Result deleteConfigGroup(Integer clusterId, Integer groupId);

    Result configValidations(Integer clusterId, JSONObject settings);

    /**
     * 校验资源库操作系统URL配置
     *
     * @param clusterId    集群id
     * @param stackName    集群名称
     * @param name         配置
     * @param version      版本
     * @param osType       操作系统类型
     * @param repositories 配置
     */
    Result<JSONObject> resourceOsUrlValidation(Integer clusterId, String stackName, String name, String version, String osType, JSONObject repositories);

    /**
     * 集群版本及组件信息保存
     *
     * @param clusterId    集群id
     * @param stackName    集群名称
     * @param stackVersion 集群版本
     * @param id           id
     * @param repositories 配置
     */
    Result<JSONObject> clusterVersionSave(Integer clusterId, String stackName, String stackVersion, Integer id, JSONObject repositories);

    /**
     * 获取集群版本历史信息
     *
     * @param clusterId 集群id
     */
    Result<JSONObject> stackHistory(Integer clusterId);

    /**
     * 获取所有集群信息
     *
     * @param clusterId 集群id
     */
    Result<JSONObject> clusters(Integer clusterId);

    Result configRecommendations(Integer clusterId, JSONObject settings);

    Result updateConfig(Integer clusterId, JSONArray settings);

    /**
     * 查询集群和服务信息
     *
     * @param clusterId         集群ID
     * @param stackName         集群名称
     * @param repositoryVersion 仓库版本
     */
    Result<JSONObject> clusterAndService(Integer clusterId, String stackName, String repositoryVersion);

    /**
     * 获取资源操作系统列表
     *
     * @param clusterId 集群id
     * @param stackName 堆栈名称
     * @param version   版本
     */
    Result<JSONObject> resourceOSList(Integer clusterId, String stackName, String version);

    /**
     * 校验gpl的license
     *
     * @param clusterId 集群id
     */
    Result<JSONObject> validateGPLLicense(Integer clusterId);

    /**
     * 集群版本注销
     *
     * @param clusterId 集群ID
     * @param id        数据ID
     */
    Result<JSONObject> stackVersionDel(Integer clusterId, Long id);
}
