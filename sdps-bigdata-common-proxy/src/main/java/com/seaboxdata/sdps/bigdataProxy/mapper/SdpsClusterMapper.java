package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

public interface SdpsClusterMapper extends SuperMapper<SdpsCluster> {

    @Cacheable(cacheNames = "clusterType", unless = "#result == null")
    String queryClusterTypeByClusterId(Integer clusterId);

    @Cacheable(cacheNames = "clusterServerInfo", unless = "#result == null")
    SdpsServerInfo queryServerInfoByClusterId(@Param("clusterId") Integer clusterId, @Param("type") String type);
}
