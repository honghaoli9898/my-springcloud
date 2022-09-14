package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;

public interface IResourceStatService {

    /**
     * 资源管理 -> 存储资源 -> 增长趋势
     * @param dirRequest 请求参数
     * @return
     */
    Result getStorageTopN(DirRequest dirRequest);

    /**
     * 资源管理 -> 存储资源 -> 增长趋势下面的列表
     * @param dirRequest
     * @return
     */
    Result getResourceStatByPage(DirRequest dirRequest);

    /**
     * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
     * @param dirRequest
     * @return
     */
    Result getResourceByTenant(DirRequest dirRequest);

    /**
     * 查询存储资源变化趋势
     * @param dirRequest
     * @return
     */
    Result selectStorageTrend(DirRequest dirRequest);

    /**
     * 存储资源趋势->路径下拉框
     * @param dirRequest
     * @return
     */
    Result selectPathSelections(DirRequest dirRequest);

    /**
     * 存储资源趋势->库下拉框
     * @param dirRequest
     * @return
     */
    Result selectDatabaseSelections(DirRequest dirRequest);

    /**
     * 存储资源趋势->表下拉框
     * @param dirRequest
     * @return
     */
    Result selectTableSelections(DirRequest dirRequest);

    /**
     * 存储地图 -> 开始、结束时间存储量
     * @param dirRequest
     * @return
     */
    Result selectDiffStorage(DirRequest dirRequest);

    /**
     * 存储地图 -> 存储资源使用排行
     * @param dirRequest
     * @return
     */
    Result selectStorageRank(DirRequest dirRequest);

    /**
     * 获取hdfs已使用容量和总容量
     * @param dirRequest
     * @return
     */
    Result getFsContent(DirRequest dirRequest);

    /**
     * 资源管理->概览->数据热度、数据分布
     * @param clusterId 集群id
     * @param type      类型
     * @return
     */
    Result getStatsByType(Integer clusterId, String type);
}
