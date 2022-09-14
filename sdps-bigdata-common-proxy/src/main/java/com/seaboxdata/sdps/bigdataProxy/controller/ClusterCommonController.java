package com.seaboxdata.sdps.bigdataProxy.controller;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterStatusMapper;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterClusterService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterCommonService;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterStatus;

@Slf4j
@RestController
@RequestMapping("/clusterCommon")
public class ClusterCommonController {

    @Autowired
    private SdpsClusterMapper sdpsClusterMapper;

    @Autowired
    private SeaBoxFeignService seaBoxFeignService;

    @Autowired
    private IClusterCommonService clusterCommonService;
    @Autowired
    private SdpsClusterStatusMapper clusterStatusMapper;
    @Autowired
    private SdpsClusterServiceTemplateMapper clusterServiceTemplateMapper;

    @Autowired
    private IClusterClusterService clusterClusterService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/queryClusterHost")
    @Cacheable(cacheNames = "clusterHost")
    public String queryClusterHost(@RequestParam("clusterId") Integer clusterId) {
        String clusterHostConf = sdpsClusterMapper.selectById(clusterId.intValue()).getClusterHostConf();
        return clusterHostConf;
    }

    @GetMapping("/queryClusterConfPath")
    @Cacheable(cacheNames = "clusterConfPath")
    public String queryClusterConfPath(@RequestParam("clusterId") Integer clusterId) {
        String clusterConfSavePath = sdpsClusterMapper.selectById(clusterId.intValue()).getClusterConfSavePath();
        return clusterConfSavePath;
    }

    @GetMapping("/queryClusterServerInfo")
    @Cacheable(cacheNames = "clusterServerInfo", unless = "#result == null ")
    public SdpsServerInfo queryClusterRangerInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam("type") String type) {
        SdpsServerInfo sdpsServerInfo = sdpsClusterMapper.queryServerInfoByClusterId(clusterId, type);
        return sdpsServerInfo;
    }

    @GetMapping("/querySdpsClusterById")
    public SdpsCluster querySdpsClusterById(@RequestParam("clusterId") Integer clusterId) {
        return sdpsClusterMapper.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id", clusterId));
    }

    @GetMapping("/selectClusterNameById")
//    @Cacheable(cacheNames = "clusterName",unless = "#result == null ")
    public String queryClusterName(@RequestParam("clusterId") Integer clusterId) {
        SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);
        return sdpsCluster.getClusterName();
    }

    /**
     * 查询已安装组件及状态
     *
     * @param clusterId 集群id
     * @return
     */
    @GetMapping("/queryInstalledService")
    public Result queryInstalledService(@RequestParam("clusterId") Integer clusterId) {
        JSONObject result = seaBoxFeignService.queryInstalledService(clusterId);
        return Result.succeed(result);
    }

    /**
     * 集群管理列表
     *
     * @param requestBean 请求对象
     */
    @PostMapping("/clusters")
    public PageResult<SdpsCluster> clusters(@Validated @RequestBody PageRequest<JSONObject> requestBean) {
        return clusterCommonService.clusters(requestBean);
    }

    /**
     * 远程集群保存
     *
     * @param sdpsCluster 集群
     */
    @PostMapping("/remoteClusterSave")
    public Result<SdpsCluster> remoteClusterSave(@LoginUser SysUser user, @RequestBody SdpsCluster sdpsCluster) {
        try {
            return clusterCommonService.remoteClusterSave(user, sdpsCluster);
        } catch (Exception e) {
            log.info("远程集群创建失败 ", e);
            return Result.failed("远程集群创建失败");
        }
    }

    /**
     * 更新
     *
     * @param user        操作人
     * @param id          id
     * @param sdpsCluster 实体
     */
    @PutMapping("/update/{id}")
    public Result<SdpsCluster> update(@LoginUser SysUser user, @PathVariable("id") Long id, @RequestBody SdpsCluster sdpsCluster) {
        return clusterCommonService.update(user, id, sdpsCluster);
    }

    /**
     * 删除
     *
     * @param user        操作人
     * @param id          id
     */
    @DeleteMapping("delete/{id}")
    public Result<SdpsCluster> delete(@LoginUser SysUser user, @PathVariable("id") Long id) {
        return clusterCommonService.delete(user, id);
    }

    /**
     * 新建集群
     *
     * @param user        用户
     * @param sdpsCluster 集群
     */
    @PostMapping("/clusterSave")
    public Result<SdpsCluster> clusterSave(@LoginUser SysUser user, @RequestBody SdpsCluster sdpsCluster) {
        try {
            return clusterCommonService.clusterSave(user, sdpsCluster);
        } catch (Exception e) {
            log.error("新建集群失败", e);
            return Result.failed("新建集群失败");
        }
    }

    /**
     * 批量删除
     *
     * @param user 用户
     * @param ids  ids
     */
    @DeleteMapping("/batchDelete")
    public Result<SdpsCluster> batchDelete(@LoginUser SysUser user, @RequestBody List<Long> ids) {
        return clusterCommonService.batchDelete(user, ids);
    }

    /**
     * 启用
     *
     * @param user 用户
     * @param params {"id":1,"usernameAndPasswd":{"username":"","passwd":""}}
     */
    @PostMapping("/startCluster")
    public Result<SdpsCluster> startCluster(@LoginUser SysUser user, @RequestBody JSONObject params) {
        try {
            Long id = params.getLong("id");
            JSONObject usernameAndPasswd = params.getJSONObject("usernameAndPasswd");
            return clusterCommonService.startCluster(user, id, usernameAndPasswd);
        } catch (Exception e) {
            log.info("启用失败", e);
            return Result.failed("启用失败 " + e);
        }
    }

    /**
     * 停用
     *
     * @param user 用户
     * @param params {"id":1,"usernameAndPasswd":{"username":"","passwd":""}}
     */
    @PostMapping("/stopCluster")
    public Result<SdpsCluster> stopCluster(@LoginUser SysUser user, @RequestBody JSONObject params) {
        try {
            Long id = params.getLong("id");
            JSONObject usernameAndPasswd = params.getJSONObject("usernameAndPasswd");
            return clusterCommonService.stopCluster(user, id, usernameAndPasswd);
        } catch (Exception e) {
            return Result.failed("停用失败" + e);
        }
    }

    @GetMapping("/clusterStatusList")
    public Result<List<SdpsClusterStatus>> clusterStatusList() {
        return Result.succeed(clusterStatusMapper.selectList(new QueryWrapper<>()), "查询成功");
    }

    /**
     * 获取集群账户
     */
    @GetMapping("/clusterAccounts")
    public Result<List<String>> clusterAccounts() {
        List<SdpsCluster> sdpsClusterStatuses = sdpsClusterMapper.selectList(new QueryWrapper<>());
        List<String> accounts = sdpsClusterStatuses.stream()
                .map(SdpsCluster::getClusterAccount).distinct().collect(Collectors.toList());
        return Result.succeed(accounts, "查询成功");
    }

    /**
     * 集群执行操作记录
     *
     * @param id 集群ID
     */
    @GetMapping("/performOperation/{id}")
    public Result<JSONObject> performOperation(@PathVariable("id") Integer id) {
        return Result.succeed(clusterCommonService.performOperation(id), "查询成功");
    }

    /**
     * 集群执行操作记录详情
     *
     * @param id     集群ID
     * @param nodeId 节点ID
     */
    @GetMapping("/performOperationDetail/{id}")
    public Result<JSONObject> performOperationDetail(@PathVariable("id") Integer id, @RequestParam("nodeId") Integer nodeId) {
        return Result.succeed(clusterCommonService.performOperationDetail(id, nodeId), "查询成功");
    }

    /**
     * 集群告警消息记录
     *
     * @param id 集群ID
     */
    @GetMapping("/alarmMsg/{id}")
    public Result<JSONObject> alarmMsg(@PathVariable("id") Integer id) {
        return Result.succeed(clusterCommonService.alarmMsg(id), "查询成功");
    }


    /**
     * 根据用户查询集群信息
     * @param user
     * @return
     */
    @GetMapping("/queryClusterHosts")
    public Result queryClusterHosts(@RequestParam("user") String user) {
        log.info("queryClusterHosts,params:{}",user);
        return Result.succeed(clusterClusterService.queryClusterHosts(user));
    }

    @GetMapping("/getUserToken")
    public Result getUserToken(@RequestParam("user") String user) {
        log.info("getUserToken,params:{}",user);
        return Result.succeed(clusterClusterService.generateCode(user));
    }

    @PostMapping("/compareUserToken")
    public Result compareUserToken(@RequestParam("auth") String code, @RequestParam("user") String user) {
        log.info("getUserToken,params:{}",code,user);
        return Result.succeed(clusterClusterService.compareUserToken(code, user));
    }


    @GetMapping("/get")
    public String get() {
        Object llll = redisTemplate.opsForValue().get("llll");
        return llll.toString();
    }

    /**
     * 获取已开启kerberos的集群
     * @return
     */
    @GetMapping("/getEnableKerberosClusters")
    public Result getEnableKerberosClusters() {
        try {
            return Result.succeed(clusterCommonService.getEnableKerberosClusters());
        } catch (Exception e) {
            log.error("getEnableKerberosClusters error", e);
            return Result.failed(e.getMessage());
        }
    }
}
