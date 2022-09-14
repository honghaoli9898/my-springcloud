package com.seaboxdata.sdps.bigdataProxy.controller;

import com.seaboxdata.sdps.bigdataProxy.service.ISdpsDirExpireService;
import com.seaboxdata.sdps.bigdataProxy.service.IStorageManagerService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storageManager")
@Slf4j
public class StorageManagerController {
    @Autowired
    private ISdpsDirExpireService dirExpireService;
    @Autowired
    private IStorageManagerService storageManagerService;

    /**
     * 根据路径查询过期时间
     * @param clusterId 集群id
     * @param path      hdfs路径
     * @return
     */
    @GetMapping("/selectByPath")
    public SdpsDirExpire selectByPath(@RequestParam("clusterId") Integer clusterId, @RequestParam("path") String path) {
        return dirExpireService.selectByPath(clusterId, path);
    }

    /**
     * 更新过期时间
     * @param request
     * @return
     */
    @PostMapping("/updateExpire")
    public Result updateExpire(@RequestBody StorageManageRequest request) {
        try {
            dirExpireService.updateExpire(request);
            return Result.succeed("");
        } catch (Exception e) {
            log.error("updateExpire error", e);
            return Result.failed("updateExpire error");
        }
    }

    /**
     * 存储资源->新增资源
     * @param request
     * @return
     */
    @PostMapping("/batchAddHdfsDir")
    public Result batchAddHdfsDir(@RequestBody StorageManageRequest request) {
        return storageManagerService.batchAddHdfsDir(request);
    }

    /**
     * 存储资源->设置配额
     * @param request
     */
    @PostMapping("/batchSetQuota")
    public Result batchSetQuota(@RequestBody StorageManageRequest request) {
        return storageManagerService.batchSetQuota(request);
    }

    /**
     * 存储资源->查询hdfs目录
     * @param request
     * @return
     */
    @GetMapping("/getStorageList")
    public Result getStorageList(StorageManageRequest request) {
        return storageManagerService.getStorageList(request);
    }

    /**
     * 查询配置了过期时间的目录
     * @return
     */
    @GetMapping("/getDirExpire")
    public List<SdpsDirExpire> getDirExpire() {
        return dirExpireService.getDirExpire();
    }
}
