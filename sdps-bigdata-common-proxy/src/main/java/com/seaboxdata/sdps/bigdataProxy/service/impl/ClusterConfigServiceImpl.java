package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterConfigService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.util.DictionaryUtil;
import com.seaboxdata.sdps.bigdataProxy.util.ListUtil;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClusterConfigServiceImpl implements IClusterConfigService {

    @Autowired
    SeaBoxFeignService seaBoxFeignService;

    @Autowired
    IClusterDevOpsService clusterDevOpsService;

    @Autowired
    DictionaryUtil dictionaryUtil;

    @Override
    public Result getServiceConfigVersions(Integer clusterId, Integer page_size, Integer from, String sortBy, String service_name, String createtime) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getServiceConfigVersions(clusterId, page_size, from, sortBy, service_name, createtime);
            log.info("config history:{}", jo);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询集群配置版本历史记录报错", e);
            result.setCode(1);
            result.setMsg("查询集群配置版本历史记录报错");
        }
        return result;
    }

    @Override
    public Result getComponentAndHost(Integer clusterId, String serviceName) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getComponentAndHost(clusterId, serviceName);
            log.info("getComponentAndHost result:{}", result);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询服务组件所在节点信息报错", e);
            result.setCode(1);
            result.setMsg("查询服务组件所在节点信息报错");
        }
        return result;
    }

    @Override
    public Result configThemes(Integer clusterId, String serviceName) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.configThemes(clusterId, serviceName);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询服务配置themes报错", e);
            result.setCode(1);
            result.setMsg("查询服务配置themes报错");
        }
        return result;
    }

    @Override
    public Result getConfigInfo(Integer clusterId, String serviceName) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getConfigInfo(clusterId, serviceName);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询服务配置信息报错", e);
            result.setCode(1);
            result.setMsg("查询服务配置信息报错");
        }

        return result;
    }

    @Override
    public Result getConfigAllVersion(Integer clusterId, String serviceName) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getConfigAllVersion(clusterId, serviceName);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询服务配置版本报错", e);
            result.setCode(1);
            result.setMsg("查询服务配置版本报错");
        }

        return result;
    }

    @Override
    public Result getConfigGroup(Integer clusterId, String serviceName) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getConfigGroup(clusterId, serviceName);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询服务配置版本报错", e);
            result.setCode(1);
            result.setMsg("查询服务配置版本报错");
        }

        return result;
    }

    @Override
    public Result getConfigHostInfo(Integer clusterId) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.getConfigHostInfo(clusterId);
            result.setData(jo);
        } catch (Exception e) {
            log.error("查询节点信息报错", e);
            result.setCode(1);
            result.setMsg("查询节点信息报错");
        }

        return result;
    }

    @Override
    public Result updateConfigGroup(ConfigGroup configGroup) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(configGroup.getClusterId()));
        try {
            JSONObject jo = seaBoxFeignService.updateConfigGroup(configGroup);
            result.setData(jo);
        } catch (Exception e) {
            log.error("更新配置组报错", e);
            result.setCode(1);
            result.setMsg("更新配置组报错");
        }

        return result;
    }

    @Override
    public Result deleteConfigGroup(Integer clusterId, Integer groupId) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.deleteConfigGroup(clusterId, groupId);
            result.setData(jo);
        } catch (Exception e) {
            log.error("更新配置组报错", e);
            result.setCode(1);
            result.setMsg("更新配置组报错");
        }

        return result;
    }

    @Override
    public Result configValidations(Integer clusterId, JSONObject settings) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.configValidations(clusterId, settings);
            result.setData(jo);
        } catch (Exception e) {
            log.error("校验服务配置报错", e);
            result.setCode(1);
            result.setMsg("校验服务配置报错");
        }

        return result;
    }

    @Override
    public Result<JSONObject> resourceOsUrlValidation(Integer clusterId, String stackName, String name, String version, String operatingSystem, JSONObject repositories) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.resourceOsUrlValidation(clusterId, stackName, name, version, operatingSystem, repositories);
            result.setData(json);
        } catch (Exception e) {
            log.error("校验服务配置报错", e);
            result.setCode(1);
            result.setMsg("校验服务配置报错");
        }
        return result;
    }

    @Override
    public Result<JSONObject> clusterVersionSave(Integer clusterId, String stackName, String stackVersion, Integer id, JSONObject repositories) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.clusterVersionSave(clusterId, stackName, stackVersion, id, repositories);
            result.setData(json);
        } catch (Exception e) {
            log.error("集群版本信息保存失败", e);
            result.setCode(1);
            result.setMsg("集群版本信息保存失败");
        }
        return result;
    }

    @Override
    public Result<JSONObject> stackHistory(Integer clusterId) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.stackHistory(clusterId);
            result.setData(json);
        } catch (Exception e) {
            log.error("集群历史信息查询失败", e);
            result.setCode(1);
            result.setMsg("集群历史信息查询失败");
        }
        return result;
    }

    @Override
    public Result<JSONObject> clusters(Integer clusterId) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.clusters(clusterId);
            result.setData(json);
        } catch (Exception e) {
            log.error("集群环境查询失败", e);
            result.setCode(1);
            result.setMsg("集群环境查询失败");
        }
        return result;
    }

    @Override
    public Result configRecommendations(Integer clusterId, JSONObject settings) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.configRecommendations(clusterId, settings);
            result.setData(jo);
        } catch (Exception e) {
            log.error("更新服务配置报错", e);
            result.setCode(1);
            result.setMsg("更新服务配置报错");
        }

        return result;
    }

    @Override
    public Result updateConfig(Integer clusterId, JSONArray settings) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject jo = seaBoxFeignService.updateConfig(clusterId, settings);
            result.setData(jo);
        } catch (Exception e) {
            log.error("更新服务配置报错", e);
            result.setCode(1);
            result.setMsg("更新服务配置报错");
        }

        return result;
    }

    @Override
    public Result<JSONObject> clusterAndService(Integer clusterId, String stackName, String repositoryVersion) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.clusterAndService(clusterId, stackName, repositoryVersion);
            result.setData(json);
        } catch (Exception e) {
            log.error("集群和服务信息查询失败", e);
            result.setCode(1);
            result.setMsg("集群和服务信息查询失败");
        }
        return result;
    }

    /**
     * 根据 json 对象的 key 获取 dicMap 的 value 并替换
     *
     * @param json    json对象
     * @param jsonKey json对象的 key
     */
    private void jsonEqDicMapAndSwap(JSONObject json, String jsonKey) {
        String repoName = json.getString(jsonKey);
        List<Dictionary> dicRepoNames = dictionaryUtil.dictionaryCacheMap.get(repoName);
        if (ListUtil.isNotEmpty(dicRepoNames)) {
            json.put(jsonKey, dicRepoNames.get(0).getValue());
        }
    }

    @Override
    public Result<JSONObject> resourceOSList(Integer clusterId, String stackName, String version) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.resourceOSList(clusterId, stackName, version);
            result.setData(json);
        } catch (Exception e) {
            log.error("资源操作系统列表查询失败", e);
            result.setCode(1);
            result.setMsg("资源操作系统列表查询失败");
        }
        return result;
    }

    @Override
    public Result<JSONObject> validateGPLLicense(Integer clusterId) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.validateGPLLicense(clusterId);
            result.setData(json);
        } catch (Exception e) {
            log.error("校验gpl的license失败", e);
            result.setCode(1);
            result.setMsg("校验gpl的license失败");
        }
        return result;
    }

    @Override
    public Result<JSONObject> stackVersionDel(Integer clusterId, Long id) {
        Result<JSONObject> result = new Result<>();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            JSONObject json = seaBoxFeignService.stackVersionDel(clusterId, id);
            result.setData(json);
        } catch (Exception e) {
            log.error("注销版本失败", e);
            result.setCode(1);
            result.setMsg("注销版本失败");
        }
        return result;
    }
}
