package com.seaboxdata.sdps.bigdataProxy.controller;

import java.util.List;

import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;

@RestController
@Slf4j
@RequestMapping("/stat")
public class ResourceStatController {

    @Autowired
    CommonBigData commonBigData;

    /**
     * 资源管理 -> 存储资源 -> 增长趋势
     * @param dirRequest 请求参数
     * @return
     */
    @GetMapping("/topN")
    public Result topN(DirRequest dirRequest) {
        return commonBigData.getStorageTopN(dirRequest);
    }

    /**
     * 资源管理 -> 存储资源 -> 增长趋势下面的列表
     * @param dirRequest
     * @return
     */
    @GetMapping("/getResourceStatByPage")
    public Result getResourceStatByPage(DirRequest dirRequest) {
        return commonBigData.getResourceStatByPage(dirRequest);
    }

    /**
     * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
     * @param dirRequest
     * @return
     */
    @GetMapping("/getResourceByTenant")
    public Result getResourceByTenant(DirRequest dirRequest) {
        return commonBigData.getResourceByTenant(dirRequest);
    }

    /**
     * 查询存储资源变化趋势
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectStorageTrend")
    public Result selectStorageTrend(DirRequest dirRequest) {
        return commonBigData.selectStorageTrend(dirRequest);
    }

    /**
     * 存储资源趋势->路径下拉框
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectPathSelections")
    public Result selectPathSelections(DirRequest dirRequest) {
        return commonBigData.selectPathSelections(dirRequest);
    }

    /**
     * 存储资源趋势->库下拉框
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectDatabaseSelections")
    public Result selectDatabaseSelections(DirRequest dirRequest) {
        return commonBigData.selectDatabaseSelections(dirRequest);
    }

    /**
     * 存储资源趋势->表下拉框
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectTableSelections")
    public Result selectTableSelections(DirRequest dirRequest) {
        return commonBigData.selectTableSelections(dirRequest);
    }

    /**
     * 存储地图 -> 开始、结束时间存储量
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectDiffStorage")
    public Result selectDiffStorage(DirRequest dirRequest) {
        return commonBigData.selectDiffStorage(dirRequest);
    }

    /**
     * 存储地图 -> 存储资源使用排行
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectStorageRank")
    public Result selectStorageRank(DirRequest dirRequest) {
        return commonBigData.selectStorageRank(dirRequest);
    }

    /**
     * 获取hdfs已使用容量和总容量
     * @param dirRequest
     * @return
     */
    @GetMapping("/getFsContent")
    public Result getFsContent(DirRequest dirRequest) {
        return commonBigData.getFsContent(dirRequest);
    }

    /**
     * 根据类型查询数据热度或数据大小
     * @param clusterId 集群id
     * @param type      热度或大小
     * @return
     */
    @GetMapping("/getStatsByType")
    public Result<List<FileStatsDTO>> getStatsByType(@RequestParam("clusterId") Integer clusterId, @RequestParam("type") String type) {
        return commonBigData.getStatsByType(clusterId, type);
    }
}
