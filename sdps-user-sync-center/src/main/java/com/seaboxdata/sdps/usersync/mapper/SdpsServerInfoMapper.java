package com.seaboxdata.sdps.usersync.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;

public interface SdpsServerInfoMapper extends SuperMapper<SdpsServerInfo> {

    public List<Integer> selectClusterIdHasRanger();

    public SdpsServerInfo selectServerInfo(
            @Param("clusterId") Integer clusterId, @Param("type") String type);
}