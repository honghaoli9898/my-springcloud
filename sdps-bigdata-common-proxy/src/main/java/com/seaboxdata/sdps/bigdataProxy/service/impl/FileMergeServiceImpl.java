package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.service.IFileMergeService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileMergeServiceImpl implements IFileMergeService {
    @Autowired
    SeaBoxFeignService seaBoxFeignService;

    @Autowired
    IClusterDevOpsService clusterDevOpsService;
    @Override
    public Result checkTableMerge(FileMergeRequest fileMergeRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(fileMergeRequest.getClusterId()));
        try {
            HiveTableDTO hiveTableDTO = seaBoxFeignService.checkTableMerge(fileMergeRequest.getClusterId(), fileMergeRequest.getDbName(), fileMergeRequest.getTable(), fileMergeRequest.getType());
            log.info("checkTableMerge:{}", hiveTableDTO);
            result.setData(hiveTableDTO);
        } catch (Exception e) {
            log.error("checkTableMerge报错", e);
            result.setCode(1);
            result.setMsg("checkTableMerge报错");
        }
        return result;
    }

    @Override
    public Result getFileMergeDetail(FileMergeDetailRequest request) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(request.getClusterId()));
        try {
            FileMergeDTO fileMergeDetail = seaBoxFeignService.getFileMergeDetail(request.getClusterId(), request.getDbName(), request.getTable(), request.getType(), request.getPath(), request.getStartTime(), request.getEndTime());
            log.info("getFileMergeDetail:{}", fileMergeDetail);
            result.setData(fileMergeDetail);
        } catch (Exception e) {
            log.error("getFileMergeDetail报错", e);
            result.setCode(1);
            result.setMsg("getFileMergeDetail报错");
        }
        return result;
    }
}
