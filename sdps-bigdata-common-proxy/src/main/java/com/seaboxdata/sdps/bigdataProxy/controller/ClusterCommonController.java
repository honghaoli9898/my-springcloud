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
     * ??????????????????????????????
     *
     * @param clusterId ??????id
     * @return
     */
    @GetMapping("/queryInstalledService")
    public Result queryInstalledService(@RequestParam("clusterId") Integer clusterId) {
        JSONObject result = seaBoxFeignService.queryInstalledService(clusterId);
        return Result.succeed(result);
    }

    /**
     * ??????????????????
     *
     * @param requestBean ????????????
     */
    @PostMapping("/clusters")
    public PageResult<SdpsCluster> clusters(@Validated @RequestBody PageRequest<JSONObject> requestBean) {
        return clusterCommonService.clusters(requestBean);
    }

    /**
     * ??????????????????
     *
     * @param sdpsCluster ??????
     */
    @PostMapping("/remoteClusterSave")
    public Result<SdpsCluster> remoteClusterSave(@LoginUser SysUser user, @RequestBody SdpsCluster sdpsCluster) {
        try {
            return clusterCommonService.remoteClusterSave(user, sdpsCluster);
        } catch (Exception e) {
            log.info("???????????????????????? ", e);
            return Result.failed("????????????????????????");
        }
    }

    /**
     * ??????
     *
     * @param user        ?????????
     * @param id          id
     * @param sdpsCluster ??????
     */
    @PutMapping("/update/{id}")
    public Result<SdpsCluster> update(@LoginUser SysUser user, @PathVariable("id") Long id, @RequestBody SdpsCluster sdpsCluster) {
        return clusterCommonService.update(user, id, sdpsCluster);
    }

    /**
     * ??????
     *
     * @param user        ?????????
     * @param id          id
     */
    @DeleteMapping("delete/{id}")
    public Result<SdpsCluster> delete(@LoginUser SysUser user, @PathVariable("id") Long id) {
        return clusterCommonService.delete(user, id);
    }

    /**
     * ????????????
     *
     * @param user        ??????
     * @param sdpsCluster ??????
     */
    @PostMapping("/clusterSave")
    public Result<SdpsCluster> clusterSave(@LoginUser SysUser user, @RequestBody SdpsCluster sdpsCluster) {
        try {
            return clusterCommonService.clusterSave(user, sdpsCluster);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return Result.failed("??????????????????");
        }
    }

    /**
     * ????????????
     *
     * @param user ??????
     * @param ids  ids
     */
    @DeleteMapping("/batchDelete")
    public Result<SdpsCluster> batchDelete(@LoginUser SysUser user, @RequestBody List<Long> ids) {
        return clusterCommonService.batchDelete(user, ids);
    }

    /**
     * ??????
     *
     * @param user ??????
     * @param params {"id":1,"usernameAndPasswd":{"username":"","passwd":""}}
     */
    @PostMapping("/startCluster")
    public Result<SdpsCluster> startCluster(@LoginUser SysUser user, @RequestBody JSONObject params) {
        try {
            Long id = params.getLong("id");
            JSONObject usernameAndPasswd = params.getJSONObject("usernameAndPasswd");
            return clusterCommonService.startCluster(user, id, usernameAndPasswd);
        } catch (Exception e) {
            log.info("????????????", e);
            return Result.failed("???????????? " + e);
        }
    }

    /**
     * ??????
     *
     * @param user ??????
     * @param params {"id":1,"usernameAndPasswd":{"username":"","passwd":""}}
     */
    @PostMapping("/stopCluster")
    public Result<SdpsCluster> stopCluster(@LoginUser SysUser user, @RequestBody JSONObject params) {
        try {
            Long id = params.getLong("id");
            JSONObject usernameAndPasswd = params.getJSONObject("usernameAndPasswd");
            return clusterCommonService.stopCluster(user, id, usernameAndPasswd);
        } catch (Exception e) {
            return Result.failed("????????????" + e);
        }
    }

    @GetMapping("/clusterStatusList")
    public Result<List<SdpsClusterStatus>> clusterStatusList() {
        return Result.succeed(clusterStatusMapper.selectList(new QueryWrapper<>()), "????????????");
    }

    /**
     * ??????????????????
     */
    @GetMapping("/clusterAccounts")
    public Result<List<String>> clusterAccounts() {
        List<SdpsCluster> sdpsClusterStatuses = sdpsClusterMapper.selectList(new QueryWrapper<>());
        List<String> accounts = sdpsClusterStatuses.stream()
                .map(SdpsCluster::getClusterAccount).distinct().collect(Collectors.toList());
        return Result.succeed(accounts, "????????????");
    }

    /**
     * ????????????????????????
     *
     * @param id ??????ID
     */
    @GetMapping("/performOperation/{id}")
    public Result<JSONObject> performOperation(@PathVariable("id") Integer id) {
        return Result.succeed(clusterCommonService.performOperation(id), "????????????");
    }

    /**
     * ??????????????????????????????
     *
     * @param id     ??????ID
     * @param nodeId ??????ID
     */
    @GetMapping("/performOperationDetail/{id}")
    public Result<JSONObject> performOperationDetail(@PathVariable("id") Integer id, @RequestParam("nodeId") Integer nodeId) {
        return Result.succeed(clusterCommonService.performOperationDetail(id, nodeId), "????????????");
    }

    /**
     * ????????????????????????
     *
     * @param id ??????ID
     */
    @GetMapping("/alarmMsg/{id}")
    public Result<JSONObject> alarmMsg(@PathVariable("id") Integer id) {
        return Result.succeed(clusterCommonService.alarmMsg(id), "????????????");
    }


    /**
     * ??????????????????????????????
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
     * ???????????????kerberos?????????
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
