package com.seaboxdata.sdps.item.mapper;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;

public interface SdpsServerInfoMapper extends SuperMapper<SdpsServerInfo> {

	public SdpsServerInfo selectServerInfo(
			@Param("clusterId") Integer clusterId, @Param("type") String type);
}