package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.service.IStorageManagerService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.dto.StorageDTO;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StorageManagerServiceImpl implements IStorageManagerService {
    @Autowired
    private SeaBoxFeignService seaBoxFeignService;

    @Override
    public Result batchAddHdfsDir(StorageManageRequest request) {
        Result result = new Result();
        result.setCode(0);
        try {
            seaBoxFeignService.batchAddHdfsDir(request);
        } catch (Exception e) {
            log.error("新增资源报错", e);
            result.setCode(1);
            result.setMsg("新增资源报错");
        }
        return result;
    }

    @Override
    public Result batchSetQuota(StorageManageRequest request) {
        Result result = new Result();
        result.setCode(0);
        try {
            seaBoxFeignService.batchSetQuota(request);
        } catch (Exception e) {
            log.error("更新配额报错", e);
            result.setCode(1);
            result.setMsg("更新配额报错");
        }
        return result;
    }

    @Override
    public Result getStorageList(StorageManageRequest request) {
        Result result = new Result();
        result.setCode(0);
        try {
            List<StorageDTO> storageList = seaBoxFeignService.getStorageList(request.getClusterId(), request.getParentPath());
            log.info("查询hdfs目录:{}", storageList);
            result.setData(storageList);
        } catch (Exception e) {
            log.error("查询hdfs目录报错", e);
            result.setCode(1);
            result.setMsg("查询hdfs目录报错");
        }
        return result;
    }
}
