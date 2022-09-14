package com.seaboxdata.sdps.bigdataProxy.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfoDTO;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsOverviewInfo;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsCoresAndMBInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsOverviewInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;

/**
 * @author: Denny
 * @date: 2021/10/25 18:24
 * @desc:
 */
@Slf4j
@RestController
@RequestMapping("/yarnCommon")
public class YarnCommonController {


    @Autowired
    private SdpsOverviewInfoMapper sdpsOverviewInfoMapper;


    @Autowired
    CommonBigData commonBigData;

    @Autowired
    private SdpsServerInfoMapper sdpsServerInfoMapper;




    /**
     * 根据cluster id 查询yarn主节点&端口信息
     * @param clusterId cluster ID
     * @return yarn 主节点&端口信息
     */
    @GetMapping("/selectYarnInfo")
    public SdpsServerInfo selectYarnInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam(name = "port",  defaultValue = "8088",required = false) Integer port) {
        if(clusterId == null) {
            return null;
        }
        log.info("clusterId:{},port:{}",clusterId, port);
        SdpsServerInfo sdpsServerInfo = sdpsServerInfoMapper.selectYarnInfoById(clusterId, port);
        log.info("sdpsServerInfo:{}", sdpsServerInfo.toString());
        return sdpsServerInfo;
    }


    /**
     * 根据clusterId查询overview 列表数据
     * @param clusterId 集群ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 返回的结果
     */
    @GetMapping("/listSdpsOverviewInfo")
    public Result listSdpsOverviewInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime)  {
        try {
            log.info("clusterId:{}  startTime:{}  endTime:{}", clusterId, startTime, endTime);
            Long start = DateUtil.strToTimestamp(startTime, DateUtil.DATE_FORMAT_YYYYMMDD);
            Long end = DateUtil.strToTimestamp(endTime, DateUtil.DATE_FORMAT_YYYYMMDD);
            //获取结束时间的24点
            end +=  24 * 60 * 60 * 1000;

            List<SdpsOverviewInfo> sdpsOverviewInfos = sdpsOverviewInfoMapper.listSdpsOverviewInfo(clusterId,start,end);
            //根据时间范围创建有序map集合，过滤重复时间
            HashMap<Date, SdpsOverviewInfo> map = generalDateList(clusterId, start, end);
            sdpsOverviewInfos.forEach(info -> {
                Date date = DateUtil.getHalfMinutes(info.getSaveTime());
                info.setSaveTime(date.getTime());
                map.put(date, info);
            });
            sdpsOverviewInfos = map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(x -> x.getValue())
                    .collect(Collectors.toList());
            return Result.succeed(sdpsOverviewInfos,"请求成功.");
        }catch (Exception e) {
            return Result.failed(e,"请求失败.");
        }
    }

    /**
     * 根据时间范围生成列表
     * @param clusterId 集群id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private static HashMap<Date, SdpsOverviewInfo> generalDateList(Integer clusterId, Long startTime, Long endTime) {
        Calendar start = generalDate(startTime, true);
        Calendar end = generalDate(endTime, false);
        HashMap<Date, SdpsOverviewInfo> map = new HashMap<>();
        for (Calendar i = start; i.compareTo(end) < 1; i.add(Calendar.MINUTE, 30)) {
            SdpsOverviewInfo sdpsOverviewInfo = new SdpsOverviewInfo(clusterId, 0, 0, 0, 0, i.getTimeInMillis());
            map.put(i.getTime(), sdpsOverviewInfo);
        }
        return map;
    }

    /**
     * 获取当前时间戳最靠近的0分或30分
     * @param timestamp 时间戳
     * @param isStart   是否是开始时间
     * @return
     */
    private static Calendar generalDate(Long timestamp, Boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        // 秒
        calendar.set(Calendar.SECOND, 0);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        if (isStart) {
            if (calendar.get(Calendar.MINUTE) > 0 && calendar.get(Calendar.MINUTE) < 30) {
                calendar.set(Calendar.MINUTE, 30);
            } else if (calendar.get(Calendar.MINUTE) > 30) {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            }
        } else {
            if (calendar.get(Calendar.MINUTE) > 0 && calendar.get(Calendar.MINUTE) < 30) {
                calendar.set(Calendar.MINUTE, 0);
            } else if (calendar.get(Calendar.MINUTE) > 30) {
                calendar.set(Calendar.MINUTE, 30);
            }
        }

        return calendar;
    }

    /**
     * 根据clusterId查询cores&Memory 列表数据
     * @param clusterId 集群ID
     * @param topN      topN
     * @param type      core或者memory
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回的结果
     */
    @GetMapping("/listSdpsCoresAndMBInfo")
    public Result listSdpsCoresAndMBInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam("type") String type, @RequestParam("topN") Integer topN, @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime)  {
        try {
            List<SdpsCoresMBInfo> sdpsCoresMBInfos = commonBigData.sdpsCoresAndMemoryRank(clusterId, type, topN, startTime, endTime);
            return Result.succeed(sdpsCoresMBInfos,"请求成功.");
        }catch (Exception e) {
            return Result.failed(e,"请求失败.");
        }
    }


}
