package com.seaboxdata.sdps.bigdataProxy.scheduler;

import java.util.List;

import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsOverviewInfo;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsCoresAndMBInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsOverviewInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;


/**
 * @author: Denny
 * @date: 2021/11/12 17:43
 * @desc: 集群定时任务，获取集群数据
 */
@Component
@Slf4j
@Configuration
@EnableScheduling
public class ClusterMetricsScheduler {

    @Autowired
    CommonBigData commonBigData;

    @Autowired
    SdpsOverviewInfoMapper sdpsOverviewInfoMapper;

    @Autowired
    SdpsServerInfoMapper sdpsServerInfoMapper;


    @Autowired
    SdpsCoresAndMBInfoMapper sdpsCoresAndMBInfoMapper;

    /**
     * 资源使用趋势一览表
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void schedulerClusterMetrics() {
        //1.执行获取集群ID的逻辑
        List<SdpsServerInfo> sdpsServerInfos = sdpsServerInfoMapper.selectClusterId();
        for (SdpsServerInfo sdpsServerInfo : sdpsServerInfos) {
            if (!StrUtil.isBlankIfStr(sdpsServerInfo.getHost())) {
                Integer clusterId = sdpsServerInfo.getServerId();
                log.info("clusterId:{}", clusterId);
                //2.执行调用集群的逻辑
                JSONObject jsonObject = commonBigData.listMetrics(clusterId);
                //log.info("jsonObject:{}",jsonObject);
                //进行json数据处理，获取memory和cores
                if(!StrUtil.isBlankIfStr(jsonObject)) {
                    JSONObject clusterMetrics = jsonObject.getJSONObject("clusterMetrics");
                    String totalMB = clusterMetrics.getString("totalMB");
                    String totalVirtualCores = clusterMetrics.getString("totalVirtualCores");
                    String availableMB = clusterMetrics.getString("availableMB");
                    String availableVirtualCores = clusterMetrics.getString("availableVirtualCores");
                    //3.执行落库的逻辑
                    SdpsOverviewInfo sdpsOverviewInfo = new SdpsOverviewInfo();
                    sdpsOverviewInfo.setClusterId(clusterId);
                    sdpsOverviewInfo.setTotalCores(Integer.parseInt(totalVirtualCores));
                    sdpsOverviewInfo.setTotalMemory(Integer.parseInt(totalMB));
                    sdpsOverviewInfo.setSaveTime(System.currentTimeMillis());
                    sdpsOverviewInfo.setUsedMemory(Integer.parseInt(totalMB) - Integer.parseInt(availableMB));
                    sdpsOverviewInfo.setUsedCores(Integer.parseInt(totalVirtualCores) - Integer.parseInt(availableVirtualCores));
                    int flag = sdpsOverviewInfoMapper.insert(sdpsOverviewInfo);//插入正常返回1，异常返回0
                    if (flag == 0)
                        log.error("集群资源使用趋势信息插入失败.");
                } else {
                    log.error("获取集群指标信息异常.");
                }
            }
        }
    }

    /**
     * 节点资源使用率排行
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void schedulerNodesMetrics() {
        List<SdpsServerInfo> sdpsServerInfos = sdpsServerInfoMapper.selectClusterId();
        for (SdpsServerInfo sdpsServerInfo : sdpsServerInfos) {
            if (!StrUtil.isBlankIfStr(sdpsServerInfo.getHost())) {
                //1.获取clusterId
                Integer clusterId = sdpsServerInfo.getServerId();
                //2.根据clusterId，调用集群节点接口，获取节点数据。
                JSONObject jsonObject = commonBigData.listNodes(clusterId);
                log.info("jsonObject的返回内容:{}",jsonObject);
                if(!StrUtil.isBlankIfStr(jsonObject)) {
                    //3.进行json数据处理，获取使用的cores和memory
                    JSONObject nodesJSONObject = jsonObject.getJSONObject("nodes");
                    JSONArray node = nodesJSONObject.getJSONArray("node");
                    for (int i = 0; i < node.size(); i++) {
                        JSONObject nodeJSONObject = node.getJSONObject(i);
                        Integer usedMemoryMB = nodeJSONObject.getInteger("usedMemoryMB");
                        Integer usedVirtualCores = nodeJSONObject.getInteger("usedVirtualCores");
                        Integer availMemoryMB = nodeJSONObject.getInteger("availMemoryMB");
                        Integer availableVirtualCores = nodeJSONObject.getInteger("availableVirtualCores");
                        String id = nodeJSONObject.getString("id");
                        //4.封装数据，准备入库
                        SdpsCoresMBInfo sdpsCoresMBInfo = new SdpsCoresMBInfo();
                        sdpsCoresMBInfo.setClusterId(clusterId);
                        sdpsCoresMBInfo.setUsedMemory(usedMemoryMB);
                        sdpsCoresMBInfo.setUsedCores(usedVirtualCores);
                        sdpsCoresMBInfo.setTotalMemory(availMemoryMB + usedMemoryMB);
                        sdpsCoresMBInfo.setTotalCores(availableVirtualCores + usedVirtualCores);
                        sdpsCoresMBInfo.setSaveTime(System.currentTimeMillis());
                        sdpsCoresMBInfo.setNodeId(id);
                        //5.数据入库
                        int flag = sdpsCoresAndMBInfoMapper.insert(sdpsCoresMBInfo);
                        if (flag == 0)
                            log.error("集群资源使用率信息插入失败.");
                    }
                } else {
                    log.error("获取集群节点信息异常.");
                }
            }
        }
    }
}
