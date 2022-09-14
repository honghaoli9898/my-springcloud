package com.seaboxdata.sdps.bigdataProxy.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateComponentMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateConfigMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateMapper;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterServiceTemplate;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterServiceTemplateComponent;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterServiceTemplateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 集群服务模板Controller
 *
 * @author jiaohongtao
 */
@Slf4j
@RestController
@RequestMapping("/clusterServiceTemplate")
public class ClusterServiceController {

    @Autowired
    private SdpsClusterServiceTemplateMapper serviceTemplateMapper;
    @Autowired
    private SdpsClusterServiceTemplateConfigMapper serviceTemplateConfigMapper;
    @Autowired
    private SdpsClusterServiceTemplateComponentMapper serviceTemplateComponentMapper;

    @GetMapping("/components")
    public Result<List<SdpsClusterServiceTemplateComponent>> getComponents(Long serviceTemplateId) {
        List<SdpsClusterServiceTemplateComponent> serviceTemplateComponents =
                serviceTemplateComponentMapper.selectList(
                        new QueryWrapper<SdpsClusterServiceTemplateComponent>().eq("service_template_id", serviceTemplateId));
        return Result.succeed(serviceTemplateComponents);
    }

    @GetMapping("/componentsByServiceTemplateIds")
    public Result<List<SdpsClusterServiceTemplateComponent>> getComponentsByServiceTemplateIds(@RequestParam("serviceTemplateIds") String serviceTemplateIds) {
        List<String> serviceTemplateIdList = Arrays.stream(serviceTemplateIds.split(",")).collect(Collectors.toList());
        List<SdpsClusterServiceTemplateComponent> serviceTemplateComponents = serviceTemplateComponentMapper.selectList(
                        new QueryWrapper<SdpsClusterServiceTemplateComponent>().in("service_template_id", serviceTemplateIdList));
        return Result.succeed(serviceTemplateComponents);
    }

    @GetMapping("/getServiceTemplateAndComponents")
    public Result<List<SdpsClusterServiceTemplate>> getServiceTemplateAndComponents(@RequestParam("serviceTemplateIds") String serviceTemplateIds) {

        List<SdpsClusterServiceTemplate> serviceTemplates = serviceTemplateMapper.selectList(new QueryWrapper<SdpsClusterServiceTemplate>().in("id", Arrays.stream(serviceTemplateIds.split(",")).toArray()));
        List<SdpsClusterServiceTemplateComponent> serviceTemplateComponents =
                serviceTemplateComponentMapper.selectList(
                        new QueryWrapper<SdpsClusterServiceTemplateComponent>().in("service_template_id", Arrays.stream(serviceTemplateIds.split(",")).toArray()));

        serviceTemplateComponents.forEach(component -> component.setHosts(new ArrayList<>()));

        Map<Long, List<SdpsClusterServiceTemplateComponent>> componentGroupByServiceId = serviceTemplateComponents
                .stream().collect(Collectors.groupingBy(SdpsClusterServiceTemplateComponent::getServiceTemplateId));
        componentGroupByServiceId.forEach((serviceId, componentGroup) -> {
            serviceTemplates.forEach(serviceTemplate -> {
                if (serviceTemplate.getId().equals(serviceId)) {
                    serviceTemplate.setComponents(componentGroup);
                }
            });
        });

        return Result.succeed(serviceTemplates);
    }

    @GetMapping("/getServiceTemplateAndConfigs")
    public JSONObject getServiceTemplateAndConfigs(@RequestParam("serviceTemplateIds") String serviceTemplateIds) {

        List<SdpsClusterServiceTemplate> serviceTemplates = serviceTemplateMapper.selectList(new QueryWrapper<SdpsClusterServiceTemplate>().in("id", Arrays.stream(serviceTemplateIds.split(",")).toArray()));

        List<SdpsClusterServiceTemplateConfig> serviceTemplateConfigs = serviceTemplateConfigMapper.selectList(
                new QueryWrapper<SdpsClusterServiceTemplateConfig>().in("service_template_id", Arrays.stream(serviceTemplateIds.split(",")).toArray()));

        Map<Long, List<SdpsClusterServiceTemplateConfig>> serviceIdForConfigs = serviceTemplateConfigs.stream().
                collect(Collectors.groupingBy(SdpsClusterServiceTemplateConfig::getServiceTemplateId));
        serviceTemplates.forEach(serviceTemplate -> {
            List<Map<String, List<SdpsClusterServiceTemplateConfig>>> configMap = new ArrayList<>();
            serviceIdForConfigs.forEach((serviceId, configs) -> {
                if (serviceTemplate.getId().equals(serviceId)) {
                    configMap.add(configs.stream().collect(Collectors.groupingBy(SdpsClusterServiceTemplateConfig::getConfigName)));
                }
            });
            serviceTemplate.setConfigs(configMap);
        });

        List<SdpsClusterServiceTemplateConfig> configList = serviceTemplateConfigMapper.selectList(
                new QueryWrapper<SdpsClusterServiceTemplateConfig>().eq("config_name", "cluster-env"));
        JSONObject resultJson = JSONUtil.parseObj(Result.succeed(serviceTemplates));
        resultJson.set("cluster_env", configList);
        return resultJson;
    }

    @GetMapping("/components/all")
    public Result<List<SdpsClusterServiceTemplateComponent>> components() {
        /* {"service": "SMARTSENSE", "components": ["ZOOKEEPER SERVER",
        "ZOOKEEPER CLIENT"]} */
        List<SdpsClusterServiceTemplateComponent> serviceTemplateComponents = serviceTemplateComponentMapper.selectList(new QueryWrapper<>());
        return Result.succeed(serviceTemplateComponents);
    }

    /**
     * 获取服务配置
     * xmlName and properites
     *
     * @param serviceTemplateId 服务模板ID
     */
    @GetMapping("/configs")
    public Result<List<SdpsClusterServiceTemplateConfig>> getConfigs(Long serviceTemplateId) {
        List<SdpsClusterServiceTemplateConfig> serviceTemplateComponents = serviceTemplateConfigMapper.selectList(
                new QueryWrapper<SdpsClusterServiceTemplateConfig>().eq("service_template_id", serviceTemplateId));
        return Result.succeed(serviceTemplateComponents);
    }

    /**
     * 获取服务配置
     * xmlName and properites
     *
     * @param serviceTemplateIds 服务模板IDS
     */
    @GetMapping("/configsByServiceTemplateIds")
    public Result<List<SdpsClusterServiceTemplateConfig>> getConfigsByServiceTemplateIds(@RequestParam("serviceTemplateIds") String serviceTemplateIds) {
        List<String> serviceTemplateIdList = Arrays.stream(serviceTemplateIds.split(",")).collect(Collectors.toList());
        List<SdpsClusterServiceTemplateConfig> serviceTemplateComponents = serviceTemplateConfigMapper.selectList(
                new QueryWrapper<SdpsClusterServiceTemplateConfig>().in("service_template_id", serviceTemplateIdList));
        return Result.succeed(serviceTemplateComponents);
    }

    /**
     * 获取服务配置 以服务配置名称分组（eg：zoo.xml）
     * xmlName -> properties({})
     *
     * @param serviceTemplateId 服务模板ID
     */
    @GetMapping("/groupConfigs")
    public Result<Map<String, List<SdpsClusterServiceTemplateConfig>>> getGroupConfigs(Long serviceTemplateId) {
        List<SdpsClusterServiceTemplateConfig> serviceTemplateComponents = serviceTemplateConfigMapper.selectList(
                new QueryWrapper<SdpsClusterServiceTemplateConfig>().eq("service_template_id", serviceTemplateId));
        return Result.succeed(serviceTemplateComponents.stream().collect(Collectors.groupingBy(SdpsClusterServiceTemplateConfig::getConfigName)));
    }
}
