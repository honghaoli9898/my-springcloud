package com.seaboxdata.sdps.seaboxProxy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.feign.UserCenterFegin;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxStatService;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seabox/stat")
public class SeaBoxStatController {

    @Autowired
    UserCenterFegin userCenterFegin;
    @Autowired
    BigdataCommonFegin bigdataCommonFegin;
    @Autowired
    BigdataVirtualHost bigdataVirtualHost;
    @Autowired
    ISeaBoxStatService seaBoxStatService;

    /**
     * 资源管理 -> 存储资源 -> 增长趋势
     * @param dirRequest 请求参数
     * @return
     */
    @GetMapping("/topN")
    public List<TopDTO> topN(DirRequest dirRequest) {
        return seaBoxStatService.topN(dirRequest);
    }

    /**
     * 资源管理 -> 存储资源 -> 增长趋势下面的列表
     * @param dirRequest
     * @return
     */
    @GetMapping("/getResourceStatByPage")
    public IPage<DirInfoDTO> getResourceStatByPage(DirRequest dirRequest) {
        return seaBoxStatService.getResourceStatByPage(dirRequest);
    }

    /**
     * 具体某个tenant（项目）所属目录的存储量、文件数和小文件数
     * @param dirRequest
     * @return
     */
    @GetMapping("/getResourceByTenant")
    public List<DirInfoDTO> getResourceByTenant(DirRequest dirRequest) {
        return seaBoxStatService.getResourceByTenant(dirRequest);
    }

    /**
     * 查询存储资源变化趋势
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectStorageTrend")
    public List<DirInfoDTO> selectStorageTrend(DirRequest dirRequest) {
        return seaBoxStatService.selectStorageTrend(dirRequest);
    }

    /**
     * 存储资源趋势->1.路径下拉框 2.库下拉框 3.表下拉框
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectStorageSelections")
    public List<String> selectStorageSelections(DirRequest dirRequest) {
        return seaBoxStatService.selectStorageSelections(dirRequest);
    }

    /**
     * 存储地图 -> 开始、结束时间存储量
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectDiffStorage")
    public DirInfoDTO selectDiffStorage(DirRequest dirRequest) {
        return seaBoxStatService.selectDiffStorage(dirRequest);
    }

    /**
     * 存储地图->存储资源使用排行
     * @param dirRequest
     * @return
     */
    @GetMapping("/selectStorageRank")
    public List<DirInfoDTO> selectStorageRank(DirRequest dirRequest) {
        return seaBoxStatService.selectStorageRank(dirRequest);
    }

    /**
     * 获取hdfs的空间大小和文件数
     * @param dirRequest
     * @return
     */
    @GetMapping("/getFsContent")
    public HdfsFSObj getFsContent(DirRequest dirRequest) {
        return seaBoxStatService.getFsContent(dirRequest);
    }
}
