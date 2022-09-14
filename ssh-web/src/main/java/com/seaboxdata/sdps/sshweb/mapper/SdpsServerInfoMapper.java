package com.seaboxdata.sdps.sshweb.mapper;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import org.apache.ibatis.annotations.Param;

public interface SdpsServerInfoMapper extends SuperMapper<SdpsServerInfo> {

	public SdpsServerInfo selectServerInfo(
			@Param("clusterId") Integer clusterId, @Param("type") String type);
}