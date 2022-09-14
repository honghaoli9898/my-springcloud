package com.seaboxdata.sdps.seaboxProxy.controller;

import com.seaboxdata.sdps.common.framework.bean.dto.StorageDTO;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxStorageManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seabox/storageManager")
public class SeaBoxStorageManagerController {

    @Autowired
    private ISeaBoxStorageManagerService storageManagerService;

    /**
     * 存储资源->新增资源
     * @param request
     * @return
     */
    @PostMapping("/batchAddHdfsDir")
    public void batchAddHdfsDir(@RequestBody StorageManageRequest request) {
        storageManagerService.batchAddHdfsDir(request);
    }

    /**
     * 存储资源->设置配额
     * @param request
     */
    @PostMapping("/batchSetQuota")
    public void batchSetQuota(@RequestBody StorageManageRequest request) {
        storageManagerService.batchSetQuota(request);
    }

    /**
     * 存储资源->查询hdfs目录
     * @param request
     * @return
     */
    @GetMapping("/getStorageList")
    public List<StorageDTO> getStorageList(StorageManageRequest request) {
        return storageManagerService.getStorageList(request);
    }
}
