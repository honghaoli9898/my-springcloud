package com.seaboxdata.sdps.seaboxProxy.service;

import com.seaboxdata.sdps.common.framework.bean.dto.StorageDTO;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;

import java.util.List;

public interface ISeaBoxStorageManagerService {
    /**
     * 存储资源->新增资源
     * @param request
     * @return
     */
    void batchAddHdfsDir(StorageManageRequest request);

    /**
     * 存储资源->设置配额
     * @param request
     */
    void batchSetQuota(StorageManageRequest request);

    /**
     * 查询hdfs目录
     * @param request
     * @return
     */
    List<StorageDTO> getStorageList(StorageManageRequest request);

}
