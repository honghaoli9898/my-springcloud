package com.seaboxdata.sdps.bigdataProxy.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateMapper;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterCreateService;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHost;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cluster/create")
public class ClusterCreateController {

    @Autowired
    private IClusterCreateService clusterCreateService;
    @Autowired
    private SdpsClusterServiceTemplateMapper clusterServiceTemplateMapper;

    /**
     * 提供服务列表
     *
     * @param clusterVersion 集群版本
     */
    @GetMapping("/clusterServiceTemplates")
    public Result<List<SdpsClusterServiceTemplate>> clusterServiceTemplates(@RequestParam(required = false) String clusterVersion) {
        QueryWrapper<SdpsClusterServiceTemplate> queryWrapper = new QueryWrapper<>();
        // 默认查询在使用的服务
        queryWrapper.eq("is_used", true);
        if (StringUtils.isNotBlank(clusterVersion)) {
            queryWrapper.eq("cluster_version", clusterVersion);
        }
        return Result.succeed(clusterServiceTemplateMapper.selectList(queryWrapper), "查询成功");
    }

    /**
     * 保存本地集群信息
     *
     * @param params 集群名称，主机信息
     */
    @PostMapping("/saveLocalCluster")
    public Result<SdpsCluster> saveLocalCluster(@RequestBody JSONObject params) {
        try {
            // 安装 ambari 服务
            // 主机、集群保存
            String clusterName = params.getString("clusterName");
            JSONArray hostInfoArray = params.getJSONArray("hostInfos");
            SdpsCluster sdpsCluster = new SdpsCluster();
            sdpsCluster.setClusterShowName(clusterName);
            sdpsCluster.setClusterHostConf(hostInfoArray.toJSONString());
            return clusterCreateService.saveLocalCluster(sdpsCluster);
        } catch (Exception e) {
            return Result.failed("保存失败");
        }
    }

    /**
     * 更新本地集群信息
     *
     * @param id 集群ID
     * @param installStatus 安装状态
     */
    @PutMapping("/updateLocalCluster/{id}")
    public Result<SdpsCluster> updateLocalCluster(@PathVariable("id") Long id, @RequestParam("installStatus") String installStatus) {
        try {
            return clusterCreateService.updateLocalCluster(id, installStatus);
        } catch (Exception e) {
            return Result.failed("更新失败");
        }
    }

    /**
     * 校验 Ambari 是否安装成功
     *
     * @param masterIp 主节点IP
     */
    @GetMapping("/validatePlatform")
    public Result<String> validatePlatform(@RequestParam(value = "masterIp", required = false) String masterIp) {
        try {
            return clusterCreateService.validatePlatform(masterIp);
        } catch (Exception e) {
            return Result.failed("校验失败");
        }
    }

    @PostMapping("/putOsRepositoriesData")
    Result<JSONObject> putOsRepositoriesData(@RequestParam(value = "masterIp", required = false) String masterIp) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.putOsRepositoriesData(masterIp), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveBootstrap")
    Result<JSONObject> saveBootstrap(@RequestParam(value = "masterIp", required = false) String masterIp) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveBootstrap(masterIp), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @GetMapping("/getBootStrapStatus")
    Result<JSONObject> getBootStrapStatus(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("bootstrapRequestId") String bootstrapRequestId) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.getBootStrapStatus(masterIp, bootstrapRequestId), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }


    @PostMapping("/saveRequests")
    Result<JSONObject> saveRequests(@RequestParam(value = "masterIp", required = false) String masterIp) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveRequests(masterIp), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveValidations")
        // Result<JSONObject> saveValidations(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestBody String reqBody) {
    Result<JSONObject> saveValidations(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String reqBody = paramsJson.getJSONObject("reqBody").toJSONString();
            if (!JSONUtil.isJson(reqBody)) {
                return Result.failed("数据格式有误");
            }

            return Result.succeed(clusterCreateService.saveValidations(masterIp, reqBody), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveRecommendations")
        // Result<JSONObject> saveRecommendations(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestBody String reqBody) {
    Result<JSONObject> saveRecommendations(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String reqBody = paramsJson.getJSONObject("reqBody").toJSONString();
            if (!JSONUtil.isJson(reqBody)) {
                return Result.failed("数据格式错误");
            }

            return Result.succeed(clusterCreateService.saveRecommendations(masterIp, reqBody), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/postClusterCurrentStatus")
        // Result<JSONObject> postClusterCurrentStatus(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestBody String currentStatus) {
    Result<JSONObject> postClusterCurrentStatus(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            JSONObject currentStatus = paramsJson.getJSONObject("currentStatus");

            return Result.succeed(clusterCreateService.postClusterCurrentStatus(masterIp, currentStatus.toJSONString()), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveVersionDefinition")
    public JSONObject saveVersionDefinition(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam(value = "versionDefinition", required = false) String versionDefinition) {
        // String versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        versionDefinition = StringUtils.isBlank(versionDefinition) ? "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}" : versionDefinition;
        return clusterCreateService.saveVersionDefinition(masterIp, versionDefinition);
    }

    @PostMapping("/saveCustomVersionDefinition")
    Result<JSONObject> saveCustomVersionDefinition(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("repoVersion") String repoVersion,
                                                   @RequestParam(value = "customVersionDefinition", required = false) String customVersionDefinition) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveCustomVersionDefinition(masterIp, repoVersion, customVersionDefinition), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveClusterWithName")
    Result<JSONObject> saveClusterWithName(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName, @RequestParam(value = "params", required = false) String params) {
        try {
            // masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterWithName(masterIp, clusterName, params), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveClusterService")
        // Result<JSONObject> saveClusterService(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName, @RequestBody String serviceJsonArr) {
    Result<JSONObject> saveClusterService(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            String serviceJsonArr = paramsJson.getJSONArray("serviceJsonArr").toJSONString();
            // masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterService(masterIp, clusterName, serviceJsonArr), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveClusterServiceXmlConfigurations")
        // Result<JSONObject> saveClusterServiceXmlConfigurations(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName, @RequestBody String serviceXmlJsonArray) {
    Result<JSONObject> saveClusterServiceXmlConfigurations(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            JSONArray serviceXmlJsonArray = paramsJson.getJSONArray("serviceXmlJsonArray");
            String reqBody;
            if (serviceXmlJsonArray == null || serviceXmlJsonArray.isEmpty()) {
                JSONObject serviceXmlJsonObject = paramsJson.getJSONObject("serviceXmlJsonObject");
                reqBody = serviceXmlJsonObject.toJSONString();
            } else {
                reqBody = serviceXmlJsonArray.toJSONString();
            }
            // masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterServiceXmlConfigurations(masterIp, clusterName, reqBody), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/saveClusterComponentNodes")
    /*Result<JSONObject> saveClusterComponentNodes(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName,
                                                 @RequestParam("serviceName") String serviceName, @RequestBody String nodeJson) {*/
    Result<JSONObject> saveClusterComponentNodes(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            String serviceName = paramsJson.getString("serviceName");
            String nodeJson = paramsJson.getString("params");
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    /*@PostMapping("/saveClusterComponentHost")
    Result<JSONObject> saveClusterComponentHost(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName, @RequestBody String hostInfo) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterComponentHost(masterIp, clusterName, hostInfo), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }*/

    @PostMapping("/saveClusterComponentNodeState")
    Result<JSONObject> saveClusterComponentNodeState(@RequestParam(value = "masterIp", required = false) String masterIp, @RequestParam("clusterName") String clusterName, @RequestParam("state") String state) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.saveClusterComponentNodeState(masterIp, clusterName, state), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }

    @PostMapping("/getClusterCurrentStatus")
    Result<JSONObject> getClusterCurrentStatus(@RequestParam(value = "masterIp", required = false) String masterIp) {
        try {
            masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
            return Result.succeed(clusterCreateService.getClusterCurrentStatus(masterIp), "处理成功");
        } catch (Exception e) {
            log.error("请求失败 ", e);
            return Result.failed("请求失败，请查看日志");
        }
    }


    @PostMapping("/saveClusterAndParams")
    /*public Result<JSONObject> saveCluster(@LoginUser SysUser user, @RequestBody SdpsCluster sdpsCluster,
                                          @RequestParam("serviceForCompos") String serviceForCompos, @RequestParam("serviceXmls") String serviceXmls,
                                          @RequestParam("paramData") String paramData) {*/
    public Result<JSONObject> saveCluster(@LoginUser SysUser user, @RequestBody JSONObject paramData) {
        try {
            // 基础组件 hdfs + (yarn + mapreduce) + zookeeper + ambari metrics
            SdpsCluster sdpsCluster = paramData.getObject("sdpsCluster", SdpsCluster.class);
            List<JSONObject> serviceForComponentList = paramData.getJSONArray("serviceForComponents").toJavaList(JSONObject.class);
            List<JSONObject> componentXmlList = paramData.getJSONArray("componentXmls").toJavaList(JSONObject.class);
            List<JSONObject> hostAndComponentList = paramData.getJSONArray("hostAndServiceList").toJavaList(JSONObject.class);

            String clusterName = sdpsCluster.getClusterShowName();
            if (StringUtils.isBlank(clusterName)) {
                return Result.failed("集群名称为空");
            }

            Result<JSONObject> saveClusterResult = clusterCreateService.saveCluster(sdpsCluster, user, clusterName, serviceForComponentList, componentXmlList, hostAndComponentList);
            if (saveClusterResult.isFailed()) {
                log.error("安装失败 {}", saveClusterResult);
                return saveClusterResult;
            }
            return Result.succeed("安装成功");
        } catch (Exception e) {
            log.error("安装失败 ", e);
            return Result.failed("安装失败，请查看日志");
        }
    }

    @PostMapping("/saveAmbariCluster")
    public Result<JSONObject> saveAmbariCluster(@LoginUser SysUser user) {
        try {
            // 基础组件 hdfs + (yarn + mapreduce) + zookeeper + ambari metrics

            Result<JSONObject> saveClusterResult = clusterCreateService.saveAmabriCluster();
            return Result.succeed("安装成功");
        } catch (Exception e) {
            log.error("安装失败 ", e);
            return Result.failed("安装失败，请查看日志");
        }
    }

    // addHost
    // 第一步 Init
    // 第二步 调用currentStatus "currentStep": 7，"status": "PENDING" requestId：null
    // 第三步 调用currentStatus "currentStep": 8，"status": "PENDING" requestId：null
    // 第四步 ambariRemoteSecond
    // 第五步：serviceAndComponentsSave
    // saveClusterComponentHost
    // services state=状态 PUT INIT
    // persist
    // services state=状态 PUT INSTALLED
    // persist (start or start failed)
    // 还有一次persist？ 昨天没有 copy 该数据

    // Init
    @PostMapping("/ambariRemoteInit")
    // Result<JSONObject> ambariRemoteInit(@RequestParam("masterIp") String masterIp, @RequestBody List<SdpsClusterHost> hostList) {
    Result<JSONObject> ambariRemoteInit(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            List<SdpsClusterHost> hostList = paramsJson.getJSONArray("hostList").toJavaList(SdpsClusterHost.class);
            return clusterCreateService.ambariRemoteInit(masterIp, hostList);
        } catch (Exception e) {
            log.error("调用失败", e);
            return Result.failed("调用失败");
        }
    }

    // versionDefinition
    @PostMapping("/ambariRemoteSecond")
    /*Result<JSONObject> ambariRemoteSecond(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName,
                                          @RequestParam("serviceNameList") List<String> serviceNameList, @RequestBody String clusterServiceConfigurations) {*/
    Result<JSONObject> ambariRemoteSecond(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            List<String> serviceNameList = paramsJson.getJSONArray("serviceNameList").toJavaList(String.class);
            List<JSONObject> clusterServiceConfigurations = paramsJson.getJSONArray("clusterServiceConfigurations").toJavaList(JSONObject.class);
            return clusterCreateService.ambariRemoteSecond(masterIp, clusterName, serviceNameList, clusterServiceConfigurations);
        } catch (Exception e) {
            log.error("调用失败", e);
            return Result.failed("调用失败");
        }
    }

    // serviceName -> componentList
    @PostMapping("/serviceAndComponentsSave")
    /*Result<JSONObject> serviceAndComponentsSave(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName,
                                                @RequestBody List<JSONObject> serviceAndComponentList) {*/
    Result<JSONObject> serviceAndComponentsSave(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            List<JSONObject> serviceAndComponentList = paramsJson.getJSONArray("serviceAndComponentList").toJavaList(JSONObject.class);
            return clusterCreateService.serviceAndComponentsSave(masterIp, clusterName, serviceAndComponentList);
        } catch (Exception e) {
            log.error("调用失败", e);
            return Result.failed("调用失败");
        }
    }

    @PostMapping("/saveClusterComponentHost")
    /*Result<JSONObject> saveClusterComponentHost(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName,
                                                @RequestBody JSONObject hostAndComponentInfos) {*/
    Result<JSONObject> saveClusterComponentHost(@RequestBody JSONObject paramsJson) {
        try {
            String masterIp = paramsJson.getString("masterIp");
            String clusterName = paramsJson.getString("clusterName");
            JSONObject hostAndComponentInfos = paramsJson.getJSONObject("hostAndComponentInfos");
            return clusterCreateService.saveClusterComponentHost(masterIp, clusterName, hostAndComponentInfos);
        } catch (Exception e) {
            log.error("调用失败", e);
            return Result.failed("调用失败");
        }
    }


    // 保存主机互信
    @PostMapping("saveBootstrapFirst")
    Result<JSONObject> saveBootstrapFirst(@RequestBody JSONObject params) {
        String masterIp = params.getString("masterIp");
        JSONArray hostInfos = params.getJSONArray("hostInfos");
        return Result.succeed(clusterCreateService.saveBootstrapFirst(masterIp, hostInfos));
    }

    // 保存第一次检查，保存第二次检查
    @PostMapping("saveRequestSecond")
    Result<JSONObject> saveRequestSecond(@RequestParam("masterIp") String masterIp) {
        return Result.succeed(clusterCreateService.saveRequestSecond(masterIp));
    }

    // 查询第一步第二步检查保存状态  循环等待
    @GetMapping("getOneAndSecondResult")
    Result<JSONObject> getOneAndSecondResult(@RequestParam("masterIp") String masterIp,
                                             @RequestParam("oneRequestId") String oneRequestId, @RequestParam("oneRequestId") String secondRequestId) {
        return Result.succeed(clusterCreateService.getOneAndSecondResult(masterIp, oneRequestId, secondRequestId));
    }

    // 保存第三次检查
    @PostMapping("saveThirdRequestThird")
    Result<JSONObject> saveThirdRequestThird(@RequestParam("masterIp") String masterIp) {
        return Result.succeed(clusterCreateService.saveThirdRequestThird(masterIp));
    }

    // 查询第三步  循环等待
    @GetMapping("getThirdRequestResult")
    Result<JSONObject> getThirdRequestResult(@RequestParam("masterIp") String masterIp, @RequestParam("thirdRequestId") String thirdRequestId) {
        return Result.succeed(clusterCreateService.getThirdRequestResult(masterIp, thirdRequestId));
    }


    // 保存版本、自定义版本 集群和集群服务的配置
    @PostMapping("saveVersionAndClusterFour")
    Result<JSONObject> saveVersionAndClusterFour(@RequestBody JSONObject paramsJson) {
        String masterIp = paramsJson.getString("masterIp");
        String clusterName = paramsJson.getString("clusterName");
        List<String> serviceJsonArr = paramsJson.getJSONArray("serviceJsonArr").toJavaList(String.class);
        String clusterServiceXmlConfigurations = paramsJson.getJSONArray("clusterServiceXmlConfigurations").toJSONString();
        /*if (!JSONUtil.isJson(clusterServiceXmlConfigurations)) {
            return Result.failed("数据格式有误");
        }*/

        return Result.succeed(clusterCreateService.saveVersionAndClusterFour(masterIp, clusterName, serviceJsonArr, clusterServiceXmlConfigurations));
    }

    // 获取初始化状态  循环等待 使用 init 的 id
    @GetMapping("getRequestStatus")
    Result<JSONObject> getRequestStatus(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName,
                                        @RequestParam("requestId") String requestId) {
        return Result.succeed(clusterCreateService.getRequestStatus(masterIp, clusterName, requestId));
    }

    // (postClusterCurrentStatus "{\"admin-settings-show-bg-admin\":\"true\"}") & 集群已安装状态更新
    @PostMapping("updateInstalledCluster")
    Result<JSONObject> updateInstalledCluster(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName) {
        return Result.succeed(clusterCreateService.updateInstalledCluster(masterIp, clusterName));
    }

    // currentStatus 集合请求
    @PostMapping("lastPostCurrentStatus")
    Result<JSONObject> lastPostCurrentStatus(@RequestBody JSONObject paramsJson) {
        String masterIp = paramsJson.getString("masterIp");
        JSONObject currentStatus = paramsJson.getJSONObject("currentStatus");

        return Result.succeed(clusterCreateService.lastPostCurrentStatus(masterIp, currentStatus.toJSONString()));
    }

    /**
     * 获取 Task 的子任务信息
     *
     * @param masterIp    主节点IP
     * @param clusterName 集群名称
     * @param requestId   主任务RequestID
     * @param taskId      子任务ID
     */
    @GetMapping("/getRequestTaskResult")
    Result<JSONObject> getRequestTaskResult(@RequestParam("masterIp") String masterIp, @RequestParam("clusterName") String clusterName,
                                            @RequestParam("requestId") Long requestId, @RequestParam("taskId") Long taskId) {
        return Result.succeed(clusterCreateService.getRequestTaskResult(masterIp, clusterName, requestId, taskId));
    }
}
