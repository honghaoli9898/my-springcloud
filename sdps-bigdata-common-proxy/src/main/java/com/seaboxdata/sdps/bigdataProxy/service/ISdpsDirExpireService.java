package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;

import java.util.List;

public interface ISdpsDirExpireService extends ISuperService<SdpsDirExpire> {
    /**
     * 根据集群id和hdfs路径查询过期时间
     * @param clusterId
     * @param path
     * @return
     */
    SdpsDirExpire selectByPath(Integer clusterId, String path);

    /**
     * 更新路径的过期时间
     * @param request
     */
    void updateExpire(StorageManageRequest request);

    /**
     * 查询配置了过期时间的目录
     * @return
     */
    List<SdpsDirExpire> getDirExpire();

    /**
     * 批量查询路径过期时间
     * @param clusterId 集群id
     * @param pathList  hdfs路径
     * @return
     */
    List<SdpsDirExpire> selectByPathList(Integer clusterId, List<String> pathList);
}
