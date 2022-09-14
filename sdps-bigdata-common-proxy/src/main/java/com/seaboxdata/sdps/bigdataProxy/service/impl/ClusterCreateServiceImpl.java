package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.mapper.DictionaryMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterCreateService;
import com.seaboxdata.sdps.bigdataProxy.task.ClusterAmbariTask;
import com.seaboxdata.sdps.bigdataProxy.util.ListUtil;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 集群管理Service实现
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
@Service
@Slf4j
public class ClusterCreateServiceImpl extends SuperServiceImpl<SdpsClusterMapper, SdpsCluster> implements IClusterCreateService {

    /*@Autowired
    private ThreadPoolTaskExecutor taskExecutor;*/

    /*@Qualifier("applicationTaskExecutor")
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;*/

    @Autowired
    SeaBoxFeignService seaBoxFeignService;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Override
    public Result<JSONObject> saveCluster(SdpsCluster sdpsCluster, SysUser user, String clusterName, List<JSONObject> serviceForComponentList, List<JSONObject> componentXmlList, List<JSONObject> hostAndComponentList) throws ExecutionException, InterruptedException {
        return Result.succeed("安装成功");
    }

    @Override
    public Result<JSONObject> saveAmabriCluster() throws Exception {
        // String hostInfoJsonArr = JSONArray.toJSONString(hostInfos);
        // SdpsCluster sdpsCluster = new SdpsCluster();
        // sdpsCluster.setClusterShowName(clusterName);
        // sdpsCluster.setClusterHostConf(hostInfoJsonArr);
        // clusterCreateService.saveCluster(sdpsCluster);
        ambariRemoteSave("10.1.3.11", "testCluster");
        return Result.succeed("安装成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SdpsCluster> saveLocalCluster(SdpsCluster sdpsCluster) throws Exception {
        //
        List<SdpsClusterHost> sdpsClusterHosts = JSONArray.parseArray(sdpsCluster.getClusterHostConf(), SdpsClusterHost.class);
        Date createDate = new Date();
        String masterIp = "";
        String masterName = "";
        String masterPasswd = "";
        for (SdpsClusterHost sdpsClusterHost : sdpsClusterHosts) {
            if ("master".equals(sdpsClusterHost.getDomainName())) {
                masterIp = sdpsClusterHost.getIp();
                masterName = sdpsClusterHost.getName();
                masterPasswd = sdpsClusterHost.getPasswd();

                sdpsCluster.setClusterIp(sdpsClusterHost.getIp());
                sdpsCluster.setClusterPort(8080);
                sdpsCluster.setClusterName(sdpsCluster.getClusterShowName() + "_real");
                sdpsCluster.setClusterAccount("admin");
                sdpsCluster.setClusterPasswd("admin");
                sdpsCluster.setClusterStatusId(SdpsCluster.Status.DEPLOYING.getId());
                sdpsCluster.setClusterSource(ClusterConstants.PLATFORM_NEW);
                sdpsCluster.setIsUse(true);
                sdpsCluster.setClusterTypeId(SdpsCluster.Type.SEABOX.getId());
                sdpsCluster.setCreateTime(createDate);
                sdpsCluster.setRunning(SdpsCluster.RunStatus.STOP.getStatus());
                break;
            }
        }

        int insertResult = baseMapper.insert(sdpsCluster);
        if (insertResult <= 0) {
            return Result.failed("保存失败");
        }

        // 执行 Ambari 安装脚本
        //taskExecutor.submit(new ClusterAmbariTask(masterIp, masterName, masterPasswd, sdpsCluster.getClusterId()));
        // threadPoolTaskExecutor.submit(new ClusterAmbariTask(masterIp, masterName, masterPasswd, sdpsCluster.getClusterId()));

        return Result.succeed(sdpsCluster,"Ambari安装中...");
    }

    @Override
    public Result<String> validatePlatform(String masterIp) {
        masterIp = StringUtils.isBlank(masterIp) ? "10.1.3.11" : masterIp;
        // String ip = "10.1.3.11";
        // 默认 端口 账号密码
        String port = "8080";
        String username = "admin";
        String passwd = "admin";

        // 使用登录来校验 远程集群是否已经存在
        Result<String> validateLoginResult = validateLogin(username, passwd, masterIp, port);
        if (validateLoginResult.isFailed()) {
            return Result.failed(validateLoginResult.getMsg());
        }

        return validateLoginResult;
    }

    /**
     * 使用页面登录接口校验是否可以登录成功
     *
     * @param username 用户名
     * @param passwd   密码
     * @param ip       IP
     * @param port     端口
     */
    private Result<String> validateLogin(String username, String passwd, String ip, String port) {
        // 使用登录来校验 远程集群是否已经存在
        Map<String, Object> hostMap = new HashedMap<>();
        hostMap.put("username", username);
        hostMap.put("passwd", passwd);
        hostMap.put("ip", ip);
        hostMap.put("port", port);
        JSONObject validateResult = seaBoxFeignService.validatePlatformAccountPaaswd(hostMap);
        if (validateResult.isEmpty()) {
            return Result.failed("校验登录失败，ambari安装失败，或者请检查账号密码是否正确");
        }

        return Result.succeed("校验登录成功");
    }

    // Init
    // 调用currentStatus "currentStep": 7，"status": "PENDING" requestId：null
    // 调用currentStatus "currentStep": 8，"status": "PENDING" requestId：null
    // saveVersionDefinition
    // saveSeviceAndComponents
    // saveClusterComponentHost
    // 第五步 services?ServiceInfo/state=state状态 PUT
    // persist init
    // services
    // persist installed
    // persist start or start failed
    // 还有一次persist？ 昨天没有 copy 该数据

    //

    // Init
    @Override
    public Result<JSONObject> ambariRemoteInit(String masterIp, List<SdpsClusterHost> hostList) throws InterruptedException {
        // String masterIp = sdpsClusterResult.getData().getClusterIp();
        // masterIp = "10.1.3.11";

        // 2-3 ======================================================
        JSONObject putOsRepositoriesData = seaBoxFeignService.putOsRepositoriesData(masterIp, "");
        log.info("\nputOsRepositoriesData: {}", putOsRepositoriesData);
        log.info("\n***************** 2-3 结束 *****************");

        // 3-4 ======================================================
        JSONObject saveBootstrap = seaBoxFeignService.saveBootstrap(masterIp, hostList);
        log.info("\nsaveBootstrap: {}", saveBootstrap);
        String bootstrapRequestId = saveBootstrap.getString("requestId");
        String bootStatus = "RUNNING";
        // 设置循环最大时间为 2 分钟
        long maxTime = 2 * 60 * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        while (!"SUCCESS".equals(bootStatus)) {
            TimeUnit.SECONDS.sleep(2);
            JSONObject bootStrapStatus = seaBoxFeignService.getBootStrapStatus(masterIp, bootstrapRequestId);
            bootStatus = bootStrapStatus.getString("status");

            // 如果循环时间超过 2 分钟，则不继续进行
            if (System.currentTimeMillis() - currentTimeMillis > maxTime) {
                bootStatus = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(bootStatus)) {
            return Result.failed("到达获取最大 Bootstrap 时间");
        }
        log.info("bootstrap构建使用了 {} 秒", System.currentTimeMillis() - currentTimeMillis);


        JSONObject saveRequests = seaBoxFeignService.saveRequests(masterIp);
        log.info("\nsaveRequests: {}", saveRequests);
        log.info("\n***************** 3-4 结束 *****************");
        if (!saveRequests.getBoolean("result")) {
            return Result.failed("请求失败");
        }

        // 一个阶段 =================

        // 4-5 ======================================================
        /*String reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[]},\"blueprint_cluster_binding\":{\"host_groups\":[]}}}";
        JSONObject saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[{\"name\":\"host-group-1\",\"components\":[{\"name\":\"NAMENODE\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_SERVER\"}]},{\"name\":\"host-group-2\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HISTORYSERVER\"}]},{\"name\":\"host-group-3\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_COLLECTOR\"}]}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"name\":\"host-group-1\",\"hosts\":[{\"fqdn\":\"master\"}]},{\"name\":\"host-group-2\",\"hosts\":[{\"fqdn\":\"node1\"}]},{\"name\":\"host-group-3\",\"hosts\":[{\"fqdn\":\"node2\"}]}]}}}";
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);


        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"validate\":\"host_groups\",\"recommendations\":{\"config-groups\":null,\"blueprint\":{\"configurations\":null,\"host_groups\":[{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"DATANODE\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"ZOOKEEPER_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"NODEMANAGER\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"HST_AGENT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"ZKFC\"}],\"name\":\"host-group-2\"},{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NAMENODE\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"HST_SERVER\"}],\"name\":\"host-group-3\"},{\"components\":[{\"name\":\"HISTORYSERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"}],\"name\":\"host-group-1\"}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"hosts\":[{\"fqdn\":\"node2\"}],\"name\":\"host-group-2\"},{\"hosts\":[{\"fqdn\":\"master\"}],\"name\":\"host-group-3\"},{\"hosts\":[{\"fqdn\":\"node1\"}],\"name\":\"host-group-1\"}]}}}";
        JSONObject saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);
        log.info("\n***************** 4-5 结束 *****************");

        // 5-6 ======================================================
        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[{\"name\":\"host-group-1\",\"components\":[{\"name\":\"NAMENODE\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_SERVER\"}]},{\"name\":\"host-group-2\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HISTORYSERVER\"}]},{\"name\":\"host-group-3\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_COLLECTOR\"}]}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"name\":\"host-group-1\",\"hosts\":[{\"fqdn\":\"master\"}]},{\"name\":\"host-group-2\",\"hosts\":[{\"fqdn\":\"node1\"}]},{\"name\":\"host-group-3\",\"hosts\":[{\"fqdn\":\"node2\"}]}]}}}";
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        // 会有两次，先做一次
        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"validate\":\"host_groups\",\"recommendations\":{\"config-groups\":null,\"blueprint\":{\"configurations\":null,\"host_groups\":[{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"DATANODE\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"ZOOKEEPER_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"NODEMANAGER\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"HST_AGENT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"ZKFC\"}],\"name\":\"host-group-2\"},{\"components\":[{\"name\":\"HISTORYSERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"}],\"name\":\"host-group-1\"},{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NAMENODE\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"HST_SERVER\"}],\"name\":\"host-group-3\"}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"hosts\":[{\"fqdn\":\"node1\"}],\"name\":\"host-group-1\"},{\"hosts\":[{\"fqdn\":\"master\"}],\"name\":\"host-group-3\"},{\"hosts\":[{\"fqdn\":\"node2\"}],\"name\":\"host-group-2\"}]}}}";
        saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);
        log.info("\n***************** 5-6 结束 *****************");

        // 6-7 ======================================================
        Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "RECOMMENDATIONS_CONFIG_REQ"));
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, dictionary.getValue());
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "VALIDATIONS_CONFIG_REQ"));
        saveValidations = seaBoxFeignService.saveValidations(masterIp, dictionary.getValue());
        log.info("\nsaveValidations: {}", saveValidations);*/

        JSONObject resultData = new JSONObject();
        // 第一次未返回信息 requestId
        // resultData.put("requestId", );
        resultData.put("sshRsa", saveBootstrap.getString("sshRsa"));
        resultData.put("bootRequestId", bootstrapRequestId);
        return Result.succeed(resultData, "初始化成功");

    }

    // 调用currentStatus "currentStep": 7，"status": "PENDING" requestId：null
    // 调用currentStatus "currentStep": 8，"status": "PENDING" requestId：null

    // versionDefinition
    @Override
    public Result<JSONObject> ambariRemoteSecond(String masterIp, String clusterName, List<String> serviceNameList, List<JSONObject> clusterServiceConfigurations) {
        String versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        JSONObject saveVersionDefinitionResult = seaBoxFeignService.saveVersionDefinition(masterIp, versionDefinition);
        log.info("\nsaveVersionDefinitionResult: {}", saveVersionDefinitionResult);
        String repoVersion = saveVersionDefinitionResult.getJSONArray("resources").getJSONObject(0).getJSONObject("VersionDefinition").getString("id");
        // resp: {"resources":[{"href":"http://10.1.3.11:8080/api/v1/version_definitions/1","VersionDefinition":{"id":1,"stack_name":"HDP","stack_version":"3.1"}}]}

        String customVersionDefinition = "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
        JSONObject saveCustomVersionDefinition = seaBoxFeignService.saveCustomVersionDefinition(masterIp, repoVersion, customVersionDefinition);
        log.info("\nsaveCustomVersionDefinition: {}", saveCustomVersionDefinition);

        // String clusterName = "PPPP";
        String reJson = "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
        JSONObject saveClusterWithName = seaBoxFeignService.saveClusterWithName(masterIp, clusterName, JSONObject.parseObject(reJson));
        log.info("\nsaveClusterWithName: {}", saveClusterWithName);

        List<JSONObject> serviceInfoArray = new ArrayList<>();
        for (String serviceName : serviceNameList) {
            JSONObject serviceInfoJson = new JSONObject();
            // TODO 获取 repoVersion？
            JSONObject serviceInfo = new JSONObject().fluentPut("desired_repository_version_id", 1);
            serviceInfoJson.put("ServiceInfo", serviceInfo.fluentPut("service_name", serviceName.toUpperCase()));
            serviceInfoArray.add(serviceInfoJson);
        }

        // String serviceJsonArr = "[{\"ServiceInfo\":{\"service_name\":\"HDFS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"YARN\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"MAPREDUCE2\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"ZOOKEEPER\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"AMBARI_METRICS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"SMARTSENSE\",\"desired_repository_version_id\":1}}]";
        // JSONObject saveClusterService = seaBoxFeignService.saveClusterService(masterIp, clusterName, JSONArray.parseArray(serviceJsonArr, JSONObject.class));
        JSONObject saveClusterService = seaBoxFeignService.saveClusterService(masterIp, clusterName, serviceInfoArray);
        log.info("\nsaveClusterService: {}", saveClusterService);

        // CLUSTER_SERVICE_CONFIGURATIONS
        // dictionary = dictionary.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_SERVICE_CONFIGURATIONS"));

        JSONObject saveClusterServiceXmlConfigurations = seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, JSONArray.toJSONString(clusterServiceConfigurations));
        log.info("\nsaveClusterServiceXmlConfigurations: {}", saveClusterServiceXmlConfigurations);

        return Result.succeed("第二步_执行成功");
    }

    // serviceName -> componentList
    @Override
    public Result<JSONObject> serviceAndComponentsSave(String masterIp, String clusterName, List<JSONObject> serviceAndComponentList) {
        for (JSONObject serviceAndComponent : serviceAndComponentList) {
            String service = serviceAndComponent.getString("service");
            List<String> components = serviceAndComponent.getJSONArray("components").toJavaList(String.class);
            JSONArray serviceComponentInfoArray = new JSONArray();
            for (String component : components) {
                JSONObject serviceComponentInfo = new JSONObject().fluentPut("component_name", component.toUpperCase());
                serviceComponentInfoArray.add(new JSONObject().fluentPut("ServiceComponentInfo", serviceComponentInfo));
            }
            JSONObject reqServiceAndComponents = new JSONObject().fluentPut("components", serviceComponentInfoArray);
            seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, service.toUpperCase(), reqServiceAndComponents.toJSONString());
        }
        /*serviceName = "SMARTSENSE";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_ANALYZER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_EXPLORER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_AGENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_SERVER\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);*/

       return Result.succeed("第三步_执行成功");
    }

    @Override
    public Result<JSONObject> saveClusterComponentHost(String masterIp, String clusterName, JSONObject hostAndComponentInfos) {
        String hostnameList = hostAndComponentInfos.getString("hostnameList");
        JSONArray hostArray = new JSONArray();
        for (String hostname : hostnameList.split(",")) {
            hostArray.add(new JSONObject().fluentPut("Hosts", new JSONObject().fluentPut("host_name", hostname.trim())));
        }
        JSONObject saveClusterComponentHost = seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostArray.toJSONString());

        /*String hostInfo = "[{\"Hosts\":{\"host_name\":\"master\"}},{\"Hosts\":{\"host_name\":\"node1\"}},{\"Hosts\":{\"host_name\":\"node2\"}}]";
        JSONObject saveClusterComponentHost = seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        log.info("saveClusterComponentHost: {}", saveClusterComponentHost);*/

        List<JSONObject> hostAndComponentList = hostAndComponentInfos.getJSONArray("hostAndComponents").toJavaList(JSONObject.class);
        int hostAndComponentListCnt = 0;
        for (JSONObject hostAndComponent : hostAndComponentList) {
            // Hosts/host_name=
            StringBuilder queryHostname = new StringBuilder();
            for (String hostname : hostAndComponent.getString("hostname").split(",")) {
                queryHostname.append("Hosts/host_name=").append(hostname).append("|");
            }
            queryHostname.deleteCharAt(queryHostname.lastIndexOf("|"));
            if (hostAndComponentListCnt == 0) {
                hostAndComponentListCnt++;
                log.info("queryHostname: {}", queryHostname);
            }

            JSONObject reqHostInfo = new JSONObject();
            reqHostInfo.put("RequestInfo", new JSONObject().fluentPut("query", queryHostname.toString()));

            JSONObject hostComponents = new JSONObject().fluentPut("HostRoles", new JSONObject().fluentPut("component_name", hostAndComponent.getString("componentName")));
            reqHostInfo.put("Body", new JSONObject().fluentPut("host_components", new JSONArray().fluentAdd(hostComponents)));
            seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, reqHostInfo.toJSONString());
            /*hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node1\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"SECONDARY_NAMENODE\"}}]}}";
            seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);*/
        }

        return Result.succeed("第四步_执行成功");
    }

    // 第五步 services?ServiceInfo/state=INIT PUT

    void ambariRemoteSave(String masterIp, String clusterName) throws InterruptedException {
        // String masterIp = sdpsClusterResult.getData().getClusterIp();
        // masterIp = "10.1.3.11";

        // http://10.1.3.11:8080/api/v1/stacks?_=1641980652149
        seaBoxFeignService.stackOne("");
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980652150
        seaBoxFeignService.AMBARI_SERVER("");
        // http://10.1.3.11:8080/api/v1/version_definitions?fields=VersionDefinition/stack_default,VersionDefinition/stack_repo_update_link_exists,VersionDefinition/max_jdk,VersionDefinition/min_jdk,operating_systems/repositories/Repositories/*,operating_systems/OperatingSystems/*,VersionDefinition/stack_services,VersionDefinition/repository_version&VersionDefinition/show_available=true&VersionDefinition/stack_name=HDP&_=1641980652151
        seaBoxFeignService.version_definitionsfieldsVersionDefinition("");
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1?fields=operating_systems/repositories/Repositories&_=1641980652152
        seaBoxFeignService.stacksHDPversions31("");
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.0?fields=operating_systems/repositories/Repositories&_=1641980652152
        seaBoxFeignService.stacksHDPversions30("");
        // http://10.1.3.11:8080/api/v1/clusters?fields=Clusters/provisioning_state,Clusters/security_type,Clusters/version,Clusters/cluster_id&_=1641980652154
        seaBoxFeignService.clustersfieldsClustersprovisioning_state("");


        // first
        // 2-3 ======================================================
        JSONObject putOsRepositoriesData = seaBoxFeignService.putOsRepositoriesData(masterIp, "");
        log.info("\nputOsRepositoriesData: {}", putOsRepositoriesData);
        log.info("\n***************** 2-3 结束 *****************");

        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980652155
        seaBoxFeignService.AMBARI_SERVER("");


        // 3-4 ======================================================
        String hostJson = "[{\"name\": \"root\", \"passwd\": \"1qaz2wsx3edc!QAZ@WSX\", \"domainName\": \"master\", \"ip\": \"10.1.3.11\"}," +
                "{\"name\": \"root\", \"passwd\": \"1qaz2wsx3edc!QAZ@WSX\", \"domainName\": \"node1\", \"ip\": \"10.1.3.12\"}" +
                "{\"name\": \"root\", \"passwd\": \"1qaz2wsx3edc!QAZ@WSX\", \"domainName\": \"node2\", \"ip\": \"10.1.3.13\"}]";
        List<SdpsClusterHost> hostList = JSONArray.parseArray(hostJson, SdpsClusterHost.class);

        JSONObject saveBootstrap = seaBoxFeignService.saveBootstrap(masterIp, hostList);
        log.info("\nsaveBootstrap: {}", saveBootstrap);
        String bootstrapRequestId = saveBootstrap.getString("requestId");
        String bootStatus = "RUNNING";
        // 设置循环最大时间为 2 分钟
        long maxTime = 2 * 60 * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        while (!"SUCCESS".equals(bootStatus)) {
            TimeUnit.SECONDS.sleep(3);
            JSONObject bootStrapStatus = seaBoxFeignService.getBootStrapStatus(masterIp, bootstrapRequestId);
            bootStatus = bootStrapStatus.getString("status");

            // 如果循环时间超过 2 分钟，则不继续进行
            if (System.currentTimeMillis() - currentTimeMillis > maxTime) {
                bootStatus = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(bootStatus)) {
            return;
        }
        log.info("bootstrap构建使用了 {} 秒", System.currentTimeMillis() - currentTimeMillis);

        // http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/host_status&_=1641966844074
        seaBoxFeignService.hostsfieldsHostshost_status("");


        JSONObject saveRequests = seaBoxFeignService.saveRequests(masterIp);
        log.info("\nsaveRequests: {}", saveRequests);

        JSONObject oneRequestResult = saveRequests.getJSONObject("resultOne");
        JSONObject secondRequestResult = saveRequests.getJSONObject("resultSecond");

        String oneStatus = "InProgress";
        String secondStatus = "InProgress";

        currentTimeMillis = System.currentTimeMillis();
        maxTime = 10* 60 * 1000;
        while (!"COMPLETED".equals(oneStatus) || !"COMPLETED".equals(secondStatus)) {
            TimeUnit.SECONDS.sleep(3);
            JSONObject oneRequest = seaBoxFeignService.getOneRequest(masterIp, oneRequestResult.getJSONObject("Requests").getString("id"));
            oneStatus = oneRequest.getJSONObject("Requests").getString("request_status");
            JSONObject secondRequest = seaBoxFeignService.getSecondRequest(masterIp, secondRequestResult.getJSONObject("Requests").getString("id"));
            secondStatus = secondRequest.getJSONObject("Requests").getString("request_status");

            // 如果循环时间超过 2 分钟，则不继续进行
            if (System.currentTimeMillis() - currentTimeMillis > maxTime) {
                oneStatus = "MAXRUNNING";
                secondStatus = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(oneStatus)) {
            return;
        }
        JSONObject thirdRequestResult = seaBoxFeignService.saveRequestsThird(masterIp);

        String thirdStatus = "InProgress";
        currentTimeMillis = System.currentTimeMillis();
        maxTime = 10* 60 * 1000;
        while (!"COMPLETED".equals(thirdStatus)) {
            TimeUnit.SECONDS.sleep(3);
            JSONObject thirdRequest = seaBoxFeignService.getOneRequest(masterIp, thirdRequestResult.getJSONObject("resultThird").getJSONObject("Requests").getString("id"));
            thirdStatus = thirdRequest.getJSONObject("Requests").getString("request_status");

            // 如果循环时间超过 2 分钟，则不继续进行
            if (System.currentTimeMillis() - currentTimeMillis > maxTime) {
                thirdStatus = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(thirdStatus)) {
            return;
        }

        // http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/total_mem,Hosts/cpu_count,Hosts/disk_info,Hosts/last_agent_env,Hosts/host_name,Hosts/os_type,Hosts/os_arch,Hosts/os_family,Hosts/ip&_=1641966844657
        seaBoxFeignService.hostsfieldsHoststotal_mem("");
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653495
        seaBoxFeignService.AMBARI_SERVER("");
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?fields=StackServices/*,components/*,components/dependencies/Dependencies/scope,components/dependencies/Dependencies/service_name,artifacts/Artifacts/artifact_name&_=1641966844660
        seaBoxFeignService.servicesfieldsStackServices("");
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653497
        seaBoxFeignService.AMBARI_SERVER("");
        // http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
        seaBoxFeignService.hostsHostshost_name("");



        /*if (!saveRequests.getBoolean("result")) {
            return;
        }*/
        log.info("\n***************** 3-4 结束 *****************");

        // 4-5 ======================================================
        // TODO 这里需要 第三步的Request 完成后 再进行调用
        // http://10.1.3.11:8080/api/v1/requests/3?fields=Requests/inputs,Requests/request_status,tasks/Tasks/host_name,tasks/Tasks/structured_out/host_resolution_check/hosts_with_failures,tasks/Tasks/structured_out/host_resolution_check/failed_count,tasks/Tasks/structured_out/installed_packages,tasks/Tasks/structured_out/last_agent_env_check,tasks/Tasks/structured_out/transparentHugePage,tasks/Tasks/stdout,tasks/Tasks/stderr,tasks/Tasks/error_log,tasks/Tasks/command_detail,tasks/Tasks/status&minimal_response=true&_=1641958787704
        String reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[]},\"blueprint_cluster_binding\":{\"host_groups\":[]}}}";
        JSONObject saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[{\"name\":\"host-group-1\",\"components\":[{\"name\":\"NAMENODE\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_SERVER\"}]},{\"name\":\"host-group-2\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HISTORYSERVER\"}]},{\"name\":\"host-group-3\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_COLLECTOR\"}]}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"name\":\"host-group-1\",\"hosts\":[{\"fqdn\":\"master\"}]},{\"name\":\"host-group-2\",\"hosts\":[{\"fqdn\":\"node1\"}]},{\"name\":\"host-group-3\",\"hosts\":[{\"fqdn\":\"node2\"}]}]}}}";
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);


        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"validate\":\"host_groups\",\"recommendations\":{\"config-groups\":null,\"blueprint\":{\"configurations\":null,\"host_groups\":[{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"DATANODE\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"ZOOKEEPER_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"NODEMANAGER\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"HST_AGENT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"ZKFC\"}],\"name\":\"host-group-2\"},{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NAMENODE\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"HST_SERVER\"}],\"name\":\"host-group-3\"},{\"components\":[{\"name\":\"HISTORYSERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"}],\"name\":\"host-group-1\"}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"hosts\":[{\"fqdn\":\"node2\"}],\"name\":\"host-group-2\"},{\"hosts\":[{\"fqdn\":\"master\"}],\"name\":\"host-group-3\"},{\"hosts\":[{\"fqdn\":\"node1\"}],\"name\":\"host-group-1\"}]}}}";
        JSONObject saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);
        log.info("\n***************** 4-5 结束 *****************");

        // 5-6 ======================================================
        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"recommend\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[{\"name\":\"host-group-1\",\"components\":[{\"name\":\"NAMENODE\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_SERVER\"}]},{\"name\":\"host-group-2\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HISTORYSERVER\"}]},{\"name\":\"host-group-3\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_COLLECTOR\"}]}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"name\":\"host-group-1\",\"hosts\":[{\"fqdn\":\"master\"}]},{\"name\":\"host-group-2\",\"hosts\":[{\"fqdn\":\"node1\"}]},{\"name\":\"host-group-3\",\"hosts\":[{\"fqdn\":\"node2\"}]}]}}}";
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        // 会有两次，先做一次
        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"validate\":\"host_groups\",\"recommendations\":{\"config-groups\":null,\"blueprint\":{\"configurations\":null,\"host_groups\":[{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"DATANODE\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"ZOOKEEPER_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"NODEMANAGER\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"HST_AGENT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"ZKFC\"}],\"name\":\"host-group-2\"},{\"components\":[{\"name\":\"HISTORYSERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"}],\"name\":\"host-group-1\"},{\"components\":[{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NAMENODE\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"HST_SERVER\"}],\"name\":\"host-group-3\"}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"hosts\":[{\"fqdn\":\"node1\"}],\"name\":\"host-group-1\"},{\"hosts\":[{\"fqdn\":\"master\"}],\"name\":\"host-group-3\"},{\"hosts\":[{\"fqdn\":\"node2\"}],\"name\":\"host-group-2\"}]}}}";
        saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);

        // 会有两次，先做一次
        reqBody = "{\"hosts\":[\"master\",\"node1\",\"node2\"],\"services\":[\"HDFS\",\"YARN\",\"MAPREDUCE2\",\"ZOOKEEPER\",\"AMBARI_METRICS\",\"SMARTSENSE\"],\"validate\":\"host_groups\",\"recommendations\":{\"blueprint\":{\"host_groups\":[{\"name\":\"host-group-1\",\"components\":[{\"name\":\"SECONDARY_NAMENODE\"},{\"name\":\"APP_TIMELINE_SERVER\"},{\"name\":\"HISTORYSERVER\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"HST_AGENT\"}]},{\"name\":\"host-group-2\",\"components\":[{\"name\":\"NAMENODE\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"TIMELINE_READER\"},{\"name\":\"YARN_REGISTRY_DNS\"},{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"ACTIVITY_ANALYZER\"},{\"name\":\"ACTIVITY_EXPLORER\"},{\"name\":\"HST_SERVER\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"HST_AGENT\"}]},{\"name\":\"host-group-3\",\"components\":[{\"name\":\"ZOOKEEPER_SERVER\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"HST_AGENT\"},{\"name\":\"ZKFC\"},{\"name\":\"DATANODE\"},{\"name\":\"NODEMANAGER\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"MAPREDUCE2_CLIENT\"},{\"name\":\"ZOOKEEPER_CLIENT\"}]}]},\"blueprint_cluster_binding\":{\"host_groups\":[{\"name\":\"host-group-1\",\"hosts\":[{\"fqdn\":\"node1\"}]},{\"name\":\"host-group-2\",\"hosts\":[{\"fqdn\":\"master\"}]},{\"name\":\"host-group-3\",\"hosts\":[{\"fqdn\":\"node2\"}]}]}}}";
        saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);
        log.info("\n***************** 5-6 结束 *****************");

        // 未添加到接口中，目测没啥用，先不做处理
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653501
        seaBoxFeignService.AMBARI_SERVER(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1?fields=configurations/*,Versions/config_types/*&_=1641966844665
        seaBoxFeignService.fieldsconfigurations31(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&fields=configurations/*,configurations/dependencies/*,StackServices/config_types/*&_=1641966844666
        seaBoxFeignService.servicesStackServices(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&themes/ThemeInfo/default=true&fields=themes/*&_=1641966844667
        seaBoxFeignService.servicesthemes(masterIp);



        // 6-7 ======================================================
        Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "RECOMMENDATIONS_CONFIG_REQ"));
        saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, dictionary.getValue());
        log.info("\nsaveRecommendations: {}", saveRecommendations);

        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "VALIDATIONS_CONFIG_REQ"));
        saveValidations = seaBoxFeignService.saveValidations(masterIp, dictionary.getValue());
        log.info("\nsaveValidations: {}", saveValidations);

        // 已有
        // 这两部没有携带requestId, 即service请求后的requestId·
        // Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_NO_1"));
        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_NO_1"));
        JSONObject postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, dictionary.getValue());
        log.info("\npostClusterCurrentStatus: {}", postClusterCurrentStatus);
        log.info("\n***************** 6-7 结束 *****************");

        // 未添加到接口中
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653505
        seaBoxFeignService.AMBARI_SERVER(masterIp);

        // 7-8 ======================================================
        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_NO_2"));
        postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, dictionary.getValue());
        log.info("\npostClusterCurrentStatus: {}", postClusterCurrentStatus);


        // TODO TODO
        // second
        String versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        JSONObject saveVersionDefinitionResult = seaBoxFeignService.saveVersionDefinition(masterIp, versionDefinition);
        log.info("\nsaveVersionDefinitionResult: {}", saveVersionDefinitionResult);
        String repoVersion = saveVersionDefinitionResult.getJSONArray("resources").getJSONObject(0).getJSONObject("VersionDefinition").getString("id");
        // resp: {"resources":[{"href":"http://10.1.3.11:8080/api/v1/version_definitions/1","VersionDefinition":{"id":1,"stack_name":"HDP","stack_version":"3.1"}}]}

        String customVersionDefinition = "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
        JSONObject saveCustomVersionDefinition = seaBoxFeignService.saveCustomVersionDefinition(masterIp, repoVersion, customVersionDefinition);
        log.info("\nsaveCustomVersionDefinition: {}", saveCustomVersionDefinition);

        // String clusterName = "PPPP";
        String reJson = "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
        JSONObject saveClusterWithName = seaBoxFeignService.saveClusterWithName(masterIp, clusterName, JSONObject.parseObject(reJson));
        log.info("\nsaveClusterWithName: {}", saveClusterWithName);

        String serviceJsonArr = "[{\"ServiceInfo\":{\"service_name\":\"HDFS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"YARN\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"MAPREDUCE2\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"ZOOKEEPER\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"AMBARI_METRICS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"SMARTSENSE\",\"desired_repository_version_id\":1}}]";
        JSONObject saveClusterService = seaBoxFeignService.saveClusterService(masterIp, clusterName, JSONArray.parseArray(serviceJsonArr, JSONObject.class));
        log.info("\nsaveClusterService: {}", saveClusterService);


        // CLUSTER_SERVICE_CONFIGURATIONS

        dictionary = dictionary.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_SERVICE_CONFIGURATIONS"));
        JSONObject saveClusterServiceXmlConfigurations = seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, dictionary.getValue());
        log.info("\nsaveClusterServiceXmlConfigurations: {}", saveClusterServiceXmlConfigurations);

        // three serviceAndComponentsSave
        String serviceName = "HDFS";
        String nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"DATANODE\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HDFS_CLIENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"JOURNALNODE\"}},{\"ServiceComponentInfo\":{\"component_name\":\"NAMENODE\"}},{\"ServiceComponentInfo\":{\"component_name\":\"NFS_GATEWAY\"}},{\"ServiceComponentInfo\":{\"component_name\":\"SECONDARY_NAMENODE\"}},{\"ServiceComponentInfo\":{\"component_name\":\"ZKFC\"}}]}";
        JSONObject saveClusterComponentNodes = seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);
        log.info("\nsaveClusterComponentNodes: {}", saveClusterComponentNodes);
        serviceName = "YARN";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"APP_TIMELINE_SERVER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"NODEMANAGER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"RESOURCEMANAGER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"TIMELINE_READER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"YARN_CLIENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"YARN_REGISTRY_DNS\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);

        serviceName = "MAPREDUCE2";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"HISTORYSERVER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"MAPREDUCE2_CLIENT\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);

        serviceName = "ZOOKEEPER";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"ZOOKEEPER_CLIENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"ZOOKEEPER_SERVER\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);

        serviceName = "AMBARI_METRICS";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"METRICS_COLLECTOR\"}},{\"ServiceComponentInfo\":{\"component_name\":\"METRICS_GRAFANA\"}},{\"ServiceComponentInfo\":{\"component_name\":\"METRICS_MONITOR\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);

        serviceName = "SMARTSENSE";
        nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_ANALYZER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_EXPLORER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_AGENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_SERVER\"}}]}";
        seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);

        // four saveClusterComponentHost
        String hostInfo = "[{\"Hosts\":{\"host_name\":\"master\"}},{\"Hosts\":{\"host_name\":\"node1\"}},{\"Hosts\":{\"host_name\":\"node2\"}}]";
        JSONObject saveClusterComponentHost = seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        log.info("saveClusterComponentHost: {}", saveClusterComponentHost);

        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node1\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"SECONDARY_NAMENODE\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"NAMENODE\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);

        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node1\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"APP_TIMELINE_SERVER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"RESOURCEMANAGER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"TIMELINE_READER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"YARN_REGISTRY_DNS\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node1\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"HISTORYSERVER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node1|Hosts/host_name=node2|Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"ZOOKEEPER_SERVER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"METRICS_COLLECTOR\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"METRICS_GRAFANA\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"ACTIVITY_ANALYZER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"ACTIVITY_EXPLORER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"HST_SERVER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"DATANODE\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"NODEMANAGER\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2|Hosts/host_name=master|Hosts/host_name=node1\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"HDFS_CLIENT\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2|Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"YARN_CLIENT\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2|Hosts/host_name=master\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"MAPREDUCE2_CLIENT\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"ZOOKEEPER_CLIENT\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master|Hosts/host_name=node1|Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"METRICS_MONITOR\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master|Hosts/host_name=node1|Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"HST_AGENT\"}}]}}";
        seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);

        /*JSONObject saveClusterComponentNodeInit = seaBoxFeignService.saveClusterComponentNodeStatusNewZk(masterIp, clusterName, "INIT");
        log.info("\nsaveClusterComponentNodeInit: {}", saveClusterComponentNodeInit);*/
        JSONObject saveClusterComponentNodeInit = seaBoxFeignService.saveClusterComponentNodeState(masterIp, clusterName, "INIT");
        String initId = saveClusterComponentNodeInit.getJSONObject("Requests").getString("id");
        log.info("\nsaveClusterComponentNodeInit: {}", saveClusterComponentNodeInit);

        /*// http://10.1.3.11:8080/api/v1/persist/CLUSTER_CURRENT_STATUS?_=1641293272654
        JSONObject clusterCurrentStatusResult = seaBoxFeignService.getClusterCurrentStatus(masterIp);
        log.info("\nclusterCurrentStatusResult: {}", clusterCurrentStatusResult);

        if (!clusterCurrentStatusResult.getBoolean("result")) {
            return;
        }
        String clusterCurrentStatus = clusterCurrentStatusResult.getString("CLUSTER_CURRENT_STATUS");
        if (StringUtils.isBlank(clusterCurrentStatus)) {
            clusterCurrentStatus = clusterCurrentStatusResult.getString("ambariData");
        }*/

        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_HAS_1"));
        postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, dictionary.getValue());
        // postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, clusterCurrentStatus);
        log.info("\npostClusterCurrentStatus: {}", postClusterCurrentStatus);
        log.info("\n***************** 7-8 结束 *****************");

        // 等待查询初始化结果后再进行安装后的启动
        // http://10.1.3.11:8080/api/v1/clusters/seabox4/requests/1?
        // fields=tasks/Tasks/command,tasks/Tasks/command_detail,tasks/Tasks/ops_display_name,tasks/Tasks/exit_code,tasks/Tasks/start_time,
        // tasks/Tasks/end_time,tasks/Tasks/host_name,tasks/Tasks/id,tasks/Tasks/role,tasks/Tasks/status&minimal_response=true&_=1641879941759

        // id为 service_init 返回 id
        String taskComplate = "RUNNING";

        // 设置循环最大时间为 1 小时
        long taskMaxTime = 60 * 60 * 1000;
        long taskCurrentTimeMillis = System.currentTimeMillis();
        while (!"SUCCESS".equals(taskComplate)) {
            TimeUnit.SECONDS.sleep(3);
            JSONObject requestStatus = seaBoxFeignService.getRequestStatus(masterIp, clusterName, initId);
            log.info("requestStatus: {}", requestStatus.toJSONString());
            JSONArray tasks = requestStatus.getJSONArray("tasks");
            List<JSONObject> collect = tasks.toJavaList(JSONObject.class).stream().
                    filter(json -> "COMPLETED".equals(json.getJSONObject("Tasks").getString("status"))).collect(Collectors.toList());
            if (collect.size() == tasks.size()) {
                taskComplate = "SUCCESS";
            }

            // 如果循环时间超过 1 小时，则不继续进行
            if (System.currentTimeMillis() - taskCurrentTimeMillis > taskMaxTime) {
                taskComplate = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(taskComplate)) {
            log.info("超过集群安装最大时长, 请检查集群服务安装状态，并在所有服务安装结束后，手动启动所有服务");
            return;
        }


        JSONObject saveClusterComponentNodeInstalled = seaBoxFeignService.saveClusterComponentNodeState(masterIp, clusterName, "INSTALLED");
        String installedId = saveClusterComponentNodeInstalled.getJSONObject("Requests").getString("id");
        log.info("\nsaveClusterComponentNodeInstalled: {}", saveClusterComponentNodeInstalled);
        dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_HAS_2"));
        postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, dictionary.getValue());
        // postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, clusterCurrentStatus);
        log.info("\npostClusterCurrentStatus: {}", postClusterCurrentStatus);


        // id为 service_init 返回 id
        taskComplate = "RUNNING";

        // 设置循环最大时间为 1 小时
        taskMaxTime = 60 * 60 * 1000;
        taskCurrentTimeMillis = System.currentTimeMillis();
        while (!"SUCCESS".equals(taskComplate)) {
            TimeUnit.SECONDS.sleep(3);
            JSONObject requestStatus = seaBoxFeignService.getRequestStatus(masterIp, clusterName, installedId);
            log.info("requestStatus: {}", requestStatus.toJSONString());
            JSONArray tasks = requestStatus.getJSONArray("tasks");
            List<JSONObject> collect = tasks.toJavaList(JSONObject.class).stream().
                    filter(json -> "COMPLETED".equals(json.getJSONObject("Tasks").getString("status"))).collect(Collectors.toList());
            if (collect.size() == tasks.size()) {
                taskComplate = "SUCCESS";
            }

            // 如果循环时间超过 1 小时，则不继续进行
            if (System.currentTimeMillis() - taskCurrentTimeMillis > taskMaxTime) {
                taskComplate = "MAXRUNNING";
                break;
            }
        }
        if ("MAXRUNNING".equals(taskComplate)) {
            log.info("超过集群服务启动最大时长, 请检查集群服务启动状态并手动启动所有服务");
            return;
        }

        // 检查所有任务是否启动成功，成功后安装启动结束
        log.info("安装启动结束");

        //persist 进入摘要 加密
        String currnetStatus = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_INSTALLED_1")).getValue();
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //persist 不加密 {"admin-settings-show-bg-admin":"true"}
        currnetStatus = "{\"admin-settings-show-bg-admin\":\"true\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //http://10.1.3.11:8080/api/v1/clusters/testCluster PUT
        //{"Clusters":{"provisioning_state":"INSTALLED"}} 无返回
        String clusterStr = "{\"Clusters\":{\"provisioning_state\":\"INSTALLED\"}}";
        seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, clusterStr);
        //persist 加密
        currnetStatus = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_INSTALLED_2")).getValue();
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //
        //persist {"admin-settings-timezone-admin":"\"-480-480|Antarctica\""}
        currnetStatus = "{\"admin-settings-timezone-admin\":\"\\\"-480-480|Antarctica\\\"\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //persist {"wizard-data":"null"}
        currnetStatus = "{\"wizard-data\":\"null\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //
        //persist
        //{"user-pref-admin-dashboard":"{\"visible\":[1,2,3,4,5,6,7,8,9,10,17,19,26],\"hidden\":[11,18,20,23],\"threshold\":{\"1\":[80,90],\"2\":[85,95],\"3\":[90,95],\"4\":[80,90],\"5\":[1000,3000],\"6\":[],\"7\":[],\"8\":[],\"9\":[],\"10\":[],\"11\":[],\"12\":[],\"13\":[70,90],\"14\":[150,250],\"15\":[3,10],\"16\":[],\"17\":[70,90],\"18\":[],\"19\":[50,75],\"20\":[50,75],\"21\":[85,95],\"22\":[85,95],\"23\":[],\"24\":[75,90],\"25\":[],\"26\":[]},\"groups\":{}}"}
        currnetStatus = "{\"user-pref-admin-dashboard\":\"{\\\"visible\\\":[1,2,3,4,5,6,7,8,9,10,17,19,26],\\\"hidden\\\":[11,18,20,23],\\\"threshold\\\":{\\\"1\\\":[80,90],\\\"2\\\":[85,95],\\\"3\\\":[90,95],\\\"4\\\":[80,90],\\\"5\\\":[1000,3000],\\\"6\\\":[],\\\"7\\\":[],\\\"8\\\":[],\\\"9\\\":[],\\\"10\\\":[],\\\"11\\\":[],\\\"12\\\":[],\\\"13\\\":[70,90],\\\"14\\\":[150,250],\\\"15\\\":[3,10],\\\"16\\\":[],\\\"17\\\":[70,90],\\\"18\\\":[],\\\"19\\\":[50,75],\\\"20\\\":[50,75],\\\"21\\\":[85,95],\\\"22\\\":[85,95],\\\"23\\\":[],\\\"24\\\":[75,90],\\\"25\\\":[],\\\"26\\\":[]},\\\"groups\\\":{}}\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);

        log.info("\n执行结束");
    }


    @Override
    public JSONObject putOsRepositoriesData(String masterIp) {
        JSONObject putOsRepositoriesData = seaBoxFeignService.putOsRepositoriesData(masterIp, "");
        log.info("\nputOsRepositoriesData: {}", putOsRepositoriesData);
        log.info("\n***************** 2-3 结束 *****************");
        return putOsRepositoriesData;
    }

    @Override
    public JSONObject saveBootstrap(String masterIp) {
        JSONObject saveBootstrap = seaBoxFeignService.saveBootstrap(masterIp, new ArrayList<>());
        log.info("\nsaveBootstrap: {}", saveBootstrap);
        return saveBootstrap;
    }

    @Override
    public JSONObject getBootStrapStatus(String masterIp, String bootstrapRequestId) {
        JSONObject bootStrapStatus = seaBoxFeignService.getBootStrapStatus(masterIp, bootstrapRequestId);
        log.info(bootStrapStatus.getString("status"));
        return bootStrapStatus;
    }

    @Override
    public JSONObject saveRequests(String masterIp) {
        JSONObject saveRequests = seaBoxFeignService.saveRequests(masterIp);
        log.info("\nsaveRequests: {}", saveRequests);
        log.info("\n***************** 3-4 结束 *****************");
        /*if (!saveRequests.getBoolean("result")) {
            return;
        }*/
        return saveRequests;
    }

    @Override
    public JSONObject saveRecommendations(String masterIp, String reqBody) {
        JSONObject saveRecommendations = seaBoxFeignService.saveRecommendations(masterIp, reqBody);
        log.info("\nsaveRecommendations: {}", saveRecommendations);
        return saveRecommendations;
    }

    @Override
    public JSONObject saveValidations(String masterIp, String reqBody) {
        JSONObject saveValidations = seaBoxFeignService.saveValidations(masterIp, reqBody);
        log.info("\nsaveValidations: {}", saveValidations);
        return saveValidations;
    }

    @Override
    public JSONObject postClusterCurrentStatus(String masterIp, String currentStatus) {
        JSONObject postClusterCurrentStatus = seaBoxFeignService.postClusterCurrentStatus(masterIp, currentStatus);
        log.info("\npostClusterCurrentStatus: {}", postClusterCurrentStatus);
        log.info("\n***************** 7-8 结束 *****************");
        return postClusterCurrentStatus;
    }

    @Override
    public JSONObject saveVersionDefinition(String masterIp, String versionDefinition) {
        // String versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        JSONObject saveVersionDefinitionResult = seaBoxFeignService.saveVersionDefinition(masterIp, versionDefinition);
        log.info("\nsaveVersionDefinitionResult: {}", saveVersionDefinitionResult);
        return saveVersionDefinitionResult;
    }

    @Override
    public JSONObject saveCustomVersionDefinition(String masterIp, String repoVersion, String customVersionDefinition) {
        // String customVersionDefinition = "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
        customVersionDefinition = "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
        JSONObject saveCustomVersionDefinition = seaBoxFeignService.saveCustomVersionDefinition(masterIp, repoVersion, customVersionDefinition);
        log.info("\nsaveCustomVersionDefinition: {}", saveCustomVersionDefinition);
        return saveCustomVersionDefinition;
    }

    @Override
    public JSONObject saveClusterWithName(String masterIp, String clusterName, String reJson) {
        // String clusterName = "PPPP";
        // String reJson = "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
        // clusterName = "PPPP";
        reJson = "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
        JSONObject saveClusterWithName = seaBoxFeignService.saveClusterWithName(masterIp, clusterName, JSONObject.parseObject(reJson));
        log.info("\nsaveClusterWithName: {}", saveClusterWithName);
        return null;
    }

    @Override
    public JSONObject saveClusterService(String masterIp, String clusterName, String serviceJsonArr) {
        // String serviceJsonArr = "[{\"ServiceInfo\":{\"service_name\":\"HDFS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"YARN\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"MAPREDUCE2\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"ZOOKEEPER\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"AMBARI_METRICS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"SMARTSENSE\",\"desired_repository_version_id\":1}}]";
        // serviceJsonArr = "[{\"ServiceInfo\":{\"service_name\":\"HDFS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"YARN\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"MAPREDUCE2\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"ZOOKEEPER\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"AMBARI_METRICS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"SMARTSENSE\",\"desired_repository_version_id\":1}}]";
        JSONObject saveClusterService = seaBoxFeignService.saveClusterService(masterIp, clusterName, JSONArray.parseArray(serviceJsonArr, JSONObject.class));
        log.info("\nsaveClusterService: {}", saveClusterService);
        return saveClusterService;
    }

    @Override
    public JSONObject saveClusterServiceXmlConfigurations(String masterIp, String clusterName, String serviceXmlJsonArray) {
        JSONObject saveClusterServiceXmlConfigurations = seaBoxFeignService.saveClusterServiceXmlConfigurations(
                masterIp, clusterName, serviceXmlJsonArray);
        log.info("\nsaveClusterServiceXmlConfigurations: {}", saveClusterServiceXmlConfigurations);
        return saveClusterServiceXmlConfigurations;
    }

    @Override
    public JSONObject saveClusterComponentNodes(String masterIp, String clusterName, String serviceName, String nodeJson) {
        // serviceName = "SMARTSENSE";
        // nodeJson = "{\"components\":[{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_ANALYZER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"ACTIVITY_EXPLORER\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_AGENT\"}},{\"ServiceComponentInfo\":{\"component_name\":\"HST_SERVER\"}}]}";
        JSONObject saveClusterComponentNodes = seaBoxFeignService.saveClusterComponentNodes(masterIp, clusterName, serviceName, nodeJson);
        log.info("\nsaveClusterComponentNodes: {}", saveClusterComponentNodes);
        return saveClusterComponentNodes;
    }

    @Override
    public JSONObject saveClusterComponentHost(String masterIp, String clusterName, String hostInfo) {
        // hostInfo = "{\"RequestInfo\":{\"query\":\"Hosts/host_name=master|Hosts/host_name=node1|Hosts/host_name=node2\"},\"Body\":{\"host_components\":[{\"HostRoles\":{\"component_name\":\"HST_AGENT\"}}]}}";
        JSONObject saveClusterComponentHost = seaBoxFeignService.saveClusterComponentHost(masterIp, clusterName, hostInfo);
        log.info("\nsaveClusterComponentHost: {}", saveClusterComponentHost);
        return saveClusterComponentHost;
    }

    @Override
    public JSONObject saveClusterComponentNodeState(String masterIp, String clusterName, String state) {
        JSONObject saveClusterComponentNodeStatus = seaBoxFeignService.saveClusterComponentNodeState(masterIp, clusterName, state);
        log.info("\nsaveClusterComponentNodeStatus: {}", saveClusterComponentNodeStatus);
        return saveClusterComponentNodeStatus;
    }

    @Override
    public JSONObject getClusterCurrentStatus(String masterIp) {
        // http://10.1.3.11:8080/api/v1/persist/CLUSTER_CURRENT_STATUS?_=1641293272654
        JSONObject clusterCurrentStatusResult = seaBoxFeignService.getClusterCurrentStatus(masterIp);
        log.info("\nclusterCurrentStatusResult: {}", clusterCurrentStatusResult);
        return clusterCurrentStatusResult;
    }


    @Override
    public JSONObject saveBootstrapFirst(String masterIp, JSONArray hostInfos) {
        // String masterIp = sdpsClusterResult.getData().getClusterIp();
        // masterIp = "10.1.3.11";

        // http://10.1.3.11:8080/api/v1/stacks?_=1641980652149
        seaBoxFeignService.stackOne(masterIp);
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980652150
        seaBoxFeignService.AMBARI_SERVER(masterIp);
        // http://10.1.3.11:8080/api/v1/version_definitions?fields=VersionDefinition/stack_default,VersionDefinition/stack_repo_update_link_exists,VersionDefinition/max_jdk,VersionDefinition/min_jdk,operating_systems/repositories/Repositories/*,operating_systems/OperatingSystems/*,VersionDefinition/stack_services,VersionDefinition/repository_version&VersionDefinition/show_available=true&VersionDefinition/stack_name=HDP&_=1641980652151
        seaBoxFeignService.version_definitionsfieldsVersionDefinition(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1?fields=operating_systems/repositories/Repositories&_=1641980652152
        seaBoxFeignService.stacksHDPversions31(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.0?fields=operating_systems/repositories/Repositories&_=1641980652152
        seaBoxFeignService.stacksHDPversions30(masterIp);
        // http://10.1.3.11:8080/api/v1/clusters?fields=Clusters/provisioning_state,Clusters/security_type,Clusters/version,Clusters/cluster_id&_=1641980652154
        seaBoxFeignService.clustersfieldsClustersprovisioning_state(masterIp);


        // first
        // 2-3 ======================================================
        Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>()
                .eq("type", "CLUSTER_CREATE").eq("name", "CLUSTER_CREATE_REPO"));
        JSONObject putOsRepositoriesData = seaBoxFeignService.putOsRepositoriesData(masterIp, dictionary.getValue());
        log.info("\nputOsRepositoriesData: {}", putOsRepositoriesData);
        log.info("\n***************** 2-3 结束 *****************");

        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980652155
        seaBoxFeignService.AMBARI_SERVER(masterIp);


        // 3-4 ======================================================
        JSONObject saveBootstrap = seaBoxFeignService.saveBootstrap(masterIp, hostInfos.toJavaList(SdpsClusterHost.class));
        log.info("\nsaveBootstrap: {}", saveBootstrap);

        return saveBootstrap;
    }

    @Override
    public JSONObject saveRequestSecond(String masterIp) {
        // http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/host_status&_=1641966844074
        seaBoxFeignService.hostsfieldsHostshost_status(masterIp);

        JSONObject saveRequests = seaBoxFeignService.saveRequests(masterIp);
        log.info("\nsaveRequests: {}", saveRequests);
        return saveRequests;
    }

    @Override
    public JSONObject getOneAndSecondResult(String masterIp, String oneRequestId, String secondRequestId) {
        // JSONObject oneRequest = seaBoxFeignService.getOneRequest(masterIp, oneRequestResult.getJSONObject("Requests").getString("id"));
        JSONObject oneRequest = seaBoxFeignService.getOneRequest(masterIp, oneRequestId);
        String oneStatus = oneRequest.getJSONObject("Requests").getString("request_status");
        JSONObject secondRequest = seaBoxFeignService.getSecondRequest(masterIp, secondRequestId);
        String secondStatus = secondRequest.getJSONObject("Requests").getString("request_status");
        return new JSONObject().fluentPut("oneStatus", oneStatus).fluentPut("secondStatus", secondStatus);
    }

    @Override
    public JSONObject saveThirdRequestThird(String masterIp) {
        JSONObject thirdRequestResult = seaBoxFeignService.saveRequestsThird(masterIp);
        log.info("thirdRequestResult: {}", thirdRequestResult);
        return thirdRequestResult;
    }

    @Override
    public JSONObject getThirdRequestResult(String masterIp, String thirdRequestId) {
        // JSONObject thirdRequest = seaBoxFeignService.getOneRequest(masterIp, thirdRequestResult.getJSONObject("resultThird").getJSONObject("Requests").getString("id"));
        JSONObject thirdRequest = seaBoxFeignService.getOneRequest(masterIp, thirdRequestId);
        String thirdStatus = thirdRequest.getJSONObject("Requests").getString("request_status");

        // http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/total_mem,Hosts/cpu_count,Hosts/disk_info,Hosts/last_agent_env,Hosts/host_name,Hosts/os_type,Hosts/os_arch,Hosts/os_family,Hosts/ip&_=1641966844657
        seaBoxFeignService.hostsfieldsHoststotal_mem(masterIp);
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653495
        seaBoxFeignService.AMBARI_SERVER(masterIp);
        // http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?fields=StackServices/*,components/*,components/dependencies/Dependencies/scope,components/dependencies/Dependencies/service_name,artifacts/Artifacts/artifact_name&_=1641966844660
        seaBoxFeignService.servicesfieldsStackServices(masterIp);
        // http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980653497
        seaBoxFeignService.AMBARI_SERVER(masterIp);
        // http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
        seaBoxFeignService.hostsHostshost_name(masterIp);

        log.info("\n***************** 3-4 结束 *****************");

        return new JSONObject().fluentPut("thirdStatus", thirdStatus);
    }

    @Override
    public JSONObject saveVersionAndClusterFour(String masterIp, String clusterName, List<String> serviceJsonList, String clusterServiceXmlConfigurations) {
        String versionDefinition = "{\"VersionDefinition\":{\"available\":\"HDP-3.1\"}}";
        JSONObject saveVersionDefinitionResult = seaBoxFeignService.saveVersionDefinition(masterIp, versionDefinition);
        log.info("\nsaveVersionDefinitionResult: {}", saveVersionDefinitionResult);
        String repoVersion = saveVersionDefinitionResult.getJSONArray("resources").getJSONObject(0).getJSONObject("VersionDefinition").getString("id");
        // resp: {"resources":[{"href":"http://10.1.3.11:8080/api/v1/version_definitions/1","VersionDefinition":{"id":1,"stack_name":"HDP","stack_version":"3.1"}}]}

        String customVersionDefinition = "{\"operating_systems\":[{\"OperatingSystems\":{\"os_type\":\"redhat7\",\"ambari_managed_repositories\":true},\"repositories\":[{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_id\":\"HDP-3.1\",\"repo_name\":\"HDP\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-3.1-GPL\",\"repo_name\":\"HDP-GPL\",\"components\":null,\"tags\":[\"GPL\"],\"distribution\":null,\"applicable_services\":[]}},{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_id\":\"HDP-UTILS-1.1.0.22\",\"repo_name\":\"HDP-UTILS\",\"components\":null,\"tags\":[],\"distribution\":null,\"applicable_services\":[]}}]}]}";
        JSONObject saveCustomVersionDefinition = seaBoxFeignService.saveCustomVersionDefinition(masterIp, repoVersion, customVersionDefinition);
        log.info("\nsaveCustomVersionDefinition: {}", saveCustomVersionDefinition);

        // String clusterName = "PPPP";
        String reJson = "{\"Clusters\":{\"version\":\"HDP-3.1\"}}";
        JSONObject saveClusterWithName = seaBoxFeignService.saveClusterWithName(masterIp, clusterName, JSONObject.parseObject(reJson));
        log.info("\nsaveClusterWithName: {}", saveClusterWithName);

        JSONArray serviceArr = new JSONArray();
        for (String serviceJson : serviceJsonList) {
            JSONObject serviceInfoJsonObj = new JSONObject().fluentPut("service_name", serviceJson).fluentPut("desired_repository_version_id", repoVersion);
            serviceArr.add(new JSONObject().fluentPut("ServiceInfo", serviceInfoJsonObj));
        }

        // String serviceJsonArr = "[{\"ServiceInfo\":{\"service_name\":\"HDFS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"YARN\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"MAPREDUCE2\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"ZOOKEEPER\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"AMBARI_METRICS\",\"desired_repository_version_id\":1}},{\"ServiceInfo\":{\"service_name\":\"SMARTSENSE\",\"desired_repository_version_id\":1}}]";
        JSONObject saveClusterService = seaBoxFeignService.saveClusterService(masterIp, clusterName, JSONArray.parseArray(serviceArr.toJSONString(), JSONObject.class));
        log.info("\nsaveClusterService: {}", saveClusterService);

        // CLUSTER_SERVICE_CONFIGURATIONS
        // dictionary = dictionary.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_SERVICE_CONFIGURATIONS"));
        // JSONObject saveClusterServiceXmlConfigurations = seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, dictionary.getValue());
        JSONObject saveClusterServiceXmlConfigurations = seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, clusterServiceXmlConfigurations);
        log.info("\nsaveClusterServiceXmlConfigurations: {}", saveClusterServiceXmlConfigurations);

        return new JSONObject().fluentPut("result", true).fluentPut("data", saveClusterServiceXmlConfigurations);
    }

    // INIT, INSTALLED
    @Override
    public JSONObject getRequestStatus(String masterIp, String clusterName, String requestId) {
        // JSONObject requestStatus = seaBoxFeignService.getRequestStatus(masterIp, clusterName, initId);
        JSONObject requestStatusData = seaBoxFeignService.getRequestStatus(masterIp, clusterName, requestId);
        log.info("requestStatus: {}", requestStatusData.toJSONString());

        // 调用接口获取数据错误时，暂查询两次，避免前端等待接口超时
        int count = 0;
        while (!requestStatusData.getBoolean("result")) {
            requestStatusData = seaBoxFeignService.getRequestStatus(masterIp, clusterName, requestId);
            if (count == 1) {
                break;
            }
            count++;
        }
        // 两次都获取失败后，直接返回结果，通知前端获取超时，前端继续进行循环获取
        if (count == 1 && !requestStatusData.getBoolean("result")) {
            return requestStatusData;
        }

        JSONArray tasks = requestStatusData.getJSONArray("tasks");
        List<JSONObject> collect = tasks.toJavaList(JSONObject.class).stream().
                filter(json -> "COMPLETED".equals(json.getJSONObject("Tasks").getString("status"))).collect(Collectors.toList());
        boolean isComplate = tasks.size() == collect.size();
        return new JSONObject().fluentPut("result", true).fluentPut("originData", requestStatusData).fluentPut("complate", isComplate);
    }

    @Override
    public JSONObject updateInstalledCluster(String masterIp, String clusterName) {
        //persist 不加密 {"admin-settings-show-bg-admin":"true"}
        String currnetStatus = "{\"admin-settings-show-bg-admin\":\"true\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //http://10.1.3.11:8080/api/v1/clusters/testCluster PUT
        //{"Clusters":{"provisioning_state":"INSTALLED"}} 无返回
        String clusterStr = "{\"Clusters\":{\"provisioning_state\":\"INSTALLED\"}}";
        seaBoxFeignService.saveClusterServiceXmlConfigurations(masterIp, clusterName, clusterStr);
        return new JSONObject().fluentPut("result", true).fluentPut("msg", "更新成功");
    }

    @Override
    public JSONObject lastPostCurrentStatus(String masterIp, String currnetStatus) {
        //persist 加密
        // currnetStatus = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("name", "CLUSTER_CURRENT_STATUS_INSTALLED_2")).getValue();
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //
        //persist {"admin-settings-timezone-admin":"\"-480-480|Antarctica\""}
        currnetStatus = "{\"admin-settings-timezone-admin\":\"\\\"-480-480|Antarctica\\\"\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //persist {"wizard-data":"null"}
        currnetStatus = "{\"wizard-data\":\"null\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);
        //
        //persist
        //{"user-pref-admin-dashboard":"{\"visible\":[1,2,3,4,5,6,7,8,9,10,17,19,26],\"hidden\":[11,18,20,23],\"threshold\":{\"1\":[80,90],\"2\":[85,95],\"3\":[90,95],\"4\":[80,90],\"5\":[1000,3000],\"6\":[],\"7\":[],\"8\":[],\"9\":[],\"10\":[],\"11\":[],\"12\":[],\"13\":[70,90],\"14\":[150,250],\"15\":[3,10],\"16\":[],\"17\":[70,90],\"18\":[],\"19\":[50,75],\"20\":[50,75],\"21\":[85,95],\"22\":[85,95],\"23\":[],\"24\":[75,90],\"25\":[],\"26\":[]},\"groups\":{}}"}
        currnetStatus = "{\"user-pref-admin-dashboard\":\"{\\\"visible\\\":[1,2,3,4,5,6,7,8,9,10,17,19,26],\\\"hidden\\\":[11,18,20,23],\\\"threshold\\\":{\\\"1\\\":[80,90],\\\"2\\\":[85,95],\\\"3\\\":[90,95],\\\"4\\\":[80,90],\\\"5\\\":[1000,3000],\\\"6\\\":[],\\\"7\\\":[],\\\"8\\\":[],\\\"9\\\":[],\\\"10\\\":[],\\\"11\\\":[],\\\"12\\\":[],\\\"13\\\":[70,90],\\\"14\\\":[150,250],\\\"15\\\":[3,10],\\\"16\\\":[],\\\"17\\\":[70,90],\\\"18\\\":[],\\\"19\\\":[50,75],\\\"20\\\":[50,75],\\\"21\\\":[85,95],\\\"22\\\":[85,95],\\\"23\\\":[],\\\"24\\\":[75,90],\\\"25\\\":[],\\\"26\\\":[]},\\\"groups\\\":{}}\"}";
        seaBoxFeignService.postClusterCurrentStatus(masterIp, currnetStatus);

        return new JSONObject().fluentPut("result", true).fluentPut("msg", "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SdpsCluster> updateLocalCluster(Long id, String installStatus) throws Exception {
        SdpsCluster queryCluster = baseMapper.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id", id));
        if (queryCluster == null) {
            return Result.failed("集群不存在");
        }

        if (SdpsCluster.Status.AMBARI_INSTALLED.name().equals(installStatus)) {
            queryCluster.setClusterStatusId(SdpsCluster.Status.OK.getId());
            queryCluster.setRunning(true);
        } else if (SdpsCluster.Status.AMBARI_FAILED.name().equals(installStatus)) {
            queryCluster.setClusterStatusId(SdpsCluster.Status.FAILED.getId());
            queryCluster.setRunning(false);
        }
        queryCluster.setUpdateTime(new Date());
        baseMapper.updateById(queryCluster);
        return Result.succeed(queryCluster);
    }

    @Override
    public JSONObject getRequestTaskResult(String masterIp, String clusterName, Long requestId, Long taskId) {
        return seaBoxFeignService.getRequestTaskResult(masterIp, clusterName, requestId, taskId);
    }
}
