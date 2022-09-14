package com.seaboxdata.sdps.seaboxProxy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.TbDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;

import java.util.List;

public interface ISeaBoxStatService extends ISuperService<TbDirInfo>  {
    /**
     * 资源管理 -> 存储资源 -> 增长趋势
     * @param dirRequest 请求参数
     * @return
     */
    List<TopDTO> topN(DirRequest dirRequest);

    /**
     * 资源管理 -> 存储资源 -> 增长趋势下面的列表
     * @param dirRequest
     * @return
     */
    IPage<DirInfoDTO> getResourceStatByPage(DirRequest dirRequest);

    /**
     * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
     * @param dirRequest
     * @return
     */
    List<DirInfoDTO> getResourceByTenant(DirRequest dirRequest);

    /**
     * 查询存储资源变化趋势
     * @param dirRequest
     * @return
     */
    List<DirInfoDTO> selectStorageTrend(DirRequest dirRequest);

    /**
     * 存储资源趋势->1.路径下拉框 2.库下拉框 3.表下拉框
     * @param dirRequest
     * @return
     */
    List<String> selectStorageSelections(DirRequest dirRequest);

    /**
     * 存储地图 -> 开始、结束时间存储量
     * @param dirRequest
     * @return
     */
    DirInfoDTO selectDiffStorage(DirRequest dirRequest);

    /**
     * 存储地图->存储资源使用排行
     * @param dirRequest
     * @return
     */
    List<DirInfoDTO> selectStorageRank(DirRequest dirRequest);

    /**
     * 获取hdfs的空间大小和文件数
     * @param dirRequest
     * @return
     */
    HdfsFSObj getFsContent(DirRequest dirRequest);
}
