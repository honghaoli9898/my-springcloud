package com.seaboxdata.sdps.usersync.mapper;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SdpsServerKeytabMapper extends SuperMapper<SdpServerKeytab> {
    Page<SdpServerKeytab> selectKeytab(@Param("clusterId") Integer clusterId, @Param("keytabName") String keytabName, @Param("principalTypeList") List<String> principalTypeList);
}