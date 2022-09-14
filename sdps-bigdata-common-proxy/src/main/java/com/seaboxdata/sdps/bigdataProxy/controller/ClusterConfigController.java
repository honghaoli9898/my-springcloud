package com.seaboxdata.sdps.bigdataProxy.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterConfigService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/config")
public class ClusterConfigController {

    @Autowired
    IClusterConfigService clusterConfigService;

    /**
     * 查询服务配置历史
     * @param clusterId        集群id
     * @param page_size        查询条数
     * @param from             查询起点
     * @param sortBy           排序字段
     * @param service_name     服务名
     * @param createtime       查询时间范围
     * @return
     */
    @GetMapping("/getServiceConfigVersions")
    public Result<Object> getServiceConfigVersions(@RequestParam("clusterId") Integer clusterId, @RequestParam("page_size") Integer page_size, @RequestParam("from") Integer from, @RequestParam("sortBy") String sortBy
            , @RequestParam(value = "service_name", required = false) String service_name, @RequestParam(value = "createtime", required = false) String createtime) {
		log.info("getServiceConfigVersions clusterId:{} page_size:{}    from:{} sortBy:{}   service_name:{} createtime:{}", clusterId, page_size, from, sortBy, service_name, createtime);
        return clusterConfigService.getServiceConfigVersions(clusterId, page_size, from, sortBy, service_name, createtime);
    }

    /**
     * 根据集群和服务获取组件和节点的关系
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/getComponentAndHost")
    public Result getComponentAndHost(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName) {
        log.info("getComponentAndHost clusterId:{}  serviceName:{}", clusterId, serviceName);
        return clusterConfigService.getComponentAndHost(clusterId, serviceName);
    }

    /**
     * 根据集群和服务名查询个别配置的样式
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/configThemes")
    public Result configThemes(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName) {
        log.info("configThemes clusterId:{}  serviceName:{}", clusterId, serviceName);
        return clusterConfigService.configThemes(clusterId, serviceName);
    }

    /**
     * 根据集群和服务名查询配置信息
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/getConfigInfo")
    public Result getConfigInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName) {
        log.info("getConfigInfo clusterId:{}  serviceName:{}", clusterId, serviceName);
        return clusterConfigService.getConfigInfo(clusterId, serviceName);
    }

    /**
     * 根据集群和服务名查询配置版本
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/getConfigAllVersion")
    public Result getConfigAllVersion(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName) {
        log.info("getConfigAllVersion clusterId:{}  serviceName:{}", clusterId, serviceName);
        return clusterConfigService.getConfigAllVersion(clusterId, serviceName);
    }

    /**
     * 根据集群和服务名查询配置组
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/getConfigGroup")
    public Result getConfigGroup(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName) {
        log.info("getConfigGroup clusterId:{}  serviceName:{}", clusterId, serviceName);
        return clusterConfigService.getConfigGroup(clusterId, serviceName);
    }

    /**
     * 查询配置组显示的节点信息
     * @param clusterId 集群id
     * @return
     */
    @GetMapping("/getConfigHostInfo")
    public Result getConfigHostInfo(@RequestParam("clusterId") Integer clusterId) {
        log.info("getConfigHostInfo clusterId:{}", clusterId);
        return clusterConfigService.getConfigHostInfo(clusterId);
    }

    /**
     * 更新配置组
     * @param configGroup 配置组信息
     * @return
     */
    @PostMapping("/updateConfigGroup")
    public Result updateConfigGroup(@RequestBody ConfigGroup configGroup) {
        log.info("updateConfigGroup clusterId:{}  configGroup:{}", configGroup);
        return clusterConfigService.updateConfigGroup(configGroup);
    }

    /**
     * 删除配置组
     * @param clusterId 集群id
     * @param groupId   配置组id
     * @return
     */
    @DeleteMapping("/deleteConfigGroup")
    public Result deleteConfigGroup(@RequestParam("clusterId") Integer clusterId, @RequestParam("groupId") Integer groupId) {
        log.info("deleteConfigGroup clusterId:{} groupId:{}", clusterId, groupId);
        return clusterConfigService.deleteConfigGroup(clusterId, groupId);
    }

    /**
     * 校验配置
     * @param clusterId 集群id
     * @param settings  配置
     * @return
     */
    @PostMapping("/configValidations")
    public Result configValidations(@RequestParam("clusterId") Integer clusterId, @RequestBody JSONObject settings) {
        log.info("configValidations clusterId:{}  settings:{}", clusterId, settings);
        return clusterConfigService.configValidations(clusterId, settings);
    }

    /**
     * 校验资源库操作系统URL配置
     *
     * @param clusterId    集群id
     * @param stackName    集群名称
     * @param name         repo名称
     * @param version      版本
     * @param osType       操作系统类型
     * @param repositories 配置
     */
    @PostMapping("/resourceOsUrlValidation")
    public Result<JSONObject> resourceOsUrlValidation(@RequestParam("clusterId") Integer clusterId, @RequestParam("stackName") String stackName,
                                                      @RequestParam("name") String name, @RequestParam("version") String version,
                                                      @RequestParam("osType") String osType, @RequestBody JSONObject repositories) {
        return clusterConfigService.resourceOsUrlValidation(clusterId, stackName, name, version, osType, repositories);
    }

    /**
     * 集群版本及组件信息保存
     *
     * @param clusterId    集群id
     * @param stackName    集群名称
     * @param stackVersion 集群版本
     * @param id           id
     * @param repositories 配置
     */
    @PutMapping("/clusterVersionSave")
    public Result<JSONObject> clusterVersionSave(@RequestParam("clusterId") Integer clusterId, @RequestParam("stackName") String stackName,
                                                 @RequestParam("stackVersion") String stackVersion, @RequestParam("id") Integer id,
                                                 @RequestBody JSONObject repositories) {
        return clusterConfigService.clusterVersionSave(clusterId, stackName, stackVersion, id, repositories);
    }

    /**
     * 获取集群版本历史信息
     *
     * @param clusterId 集群id
     */
    @GetMapping("/stackHistory")
    public Result<JSONObject> stackHistory(@RequestParam("clusterId") Integer clusterId) {
        return clusterConfigService.stackHistory(clusterId);
    }

    /**
     * 获取所有集群信息
     *
     * @param clusterId 集群id
     */
    @GetMapping("/clusters")
    public Result<JSONObject> clusters(@RequestParam("clusterId") Integer clusterId) {
        return clusterConfigService.clusters(clusterId);
    }

    /**
     * 获取集群和服务信息
     *
     * @param clusterId         集群id
     * @param stackName         堆栈名称
     * @param repositoryVersion 仓库版本
     */
    @GetMapping("/clusterAndService")
    public Result<JSONObject> clusterAndService(@RequestParam("clusterId") Integer clusterId, @RequestParam("stackName") String stackName,
                                                @RequestParam("repositoryVersion") String repositoryVersion) {
        return clusterConfigService.clusterAndService(clusterId, stackName, repositoryVersion);
    }

    /**
     * 获取资源操作系统列表
     *
     * @param clusterId 集群id
     * @param stackName 堆栈名称
     * @param version   版本
     */
    @GetMapping("/resourceOSList")
    public Result<JSONObject> resourceOSList(@RequestParam("clusterId") Integer clusterId, @RequestParam("stackName") String stackName,
                                                @RequestParam("version") String version) {
        return clusterConfigService.resourceOSList(clusterId, stackName, version);
    }

    /**
     * 校验gpl的license
     *
     * @param clusterId 集群id
     */
    @GetMapping("/validateGPLLicense")
    public Result<JSONObject> validateGPLLicense(@RequestParam("clusterId") Integer clusterId) {
        return clusterConfigService.validateGPLLicense(clusterId);
    }

    /**
     * 集群版本注销
     *
     * @param clusterId 集群ID
     * @param id        数据ID
     */
    @DeleteMapping("/stackVersionDel/{id}")
    public Result<JSONObject> stackVersionDel(@RequestParam("clusterId") Integer clusterId, @PathVariable("id") Long id) {
        return clusterConfigService.stackVersionDel(clusterId, id);
    }

    /**
     * 验证配置参数
     * @param clusterId 集群id
     * @param settings  配置
     * @return
     */
    @PostMapping("/configRecommendations")
    public Result configRecommendations(@RequestParam("clusterId") Integer clusterId, @RequestBody JSONObject settings) {
        log.info("configRecommendations clusterId:{}  settings:{}", clusterId, settings);
        return clusterConfigService.configRecommendations(clusterId, settings);
    }

    /**
     * 更新配置
     * @param clusterId 集群id
     * @param settings  配置
     * @return
     */
    @PostMapping("/updateConfig")
    public Result updateConfig(@RequestParam("clusterId") Integer clusterId, @RequestBody JSONArray settings) {
        log.info("updateConfig clusterId:{}  settings:{}", clusterId, settings);
        return clusterConfigService.updateConfig(clusterId, settings);
    }
}
