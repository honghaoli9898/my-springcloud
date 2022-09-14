package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;

public interface IStorageManagerService {
    /**
     * 存储资源->新增资源
     * @param request
     * @return
     */
    Result batchAddHdfsDir(StorageManageRequest request);

    /**
     * 存储资源->设置配额
     * @param request
     */
    Result batchSetQuota(StorageManageRequest request);

    /**
     * 查询hdfs目录
     * @param request
     * @return
     */
    Result getStorageList(StorageManageRequest request);
}
