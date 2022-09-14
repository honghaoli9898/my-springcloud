package com.seaboxdata.sdps.bigdataProxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsServerInfoDto;
import com.seaboxdata.sdps.bigdataProxy.vo.DevOpsRequest;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;

@Mapper
public interface SdpsServerInfoMapper extends SuperMapper<SdpsServerInfo> {

	Page<SdpsServerInfoDto> selectServerInfoPage(
			@Param("request") DevOpsRequest request);


	/**
	 * 查询yarn信息
	 * @param serverId 集群ID
	 * @param port 端口
	 * @return
	 */
	SdpsServerInfo selectYarnInfoById(@Param("serverId") Integer serverId, @Param("port") Integer port);


	/**
	 * 查询clusterId
	 * @return clusterId 列表
	 */
	List<SdpsServerInfo> selectClusterId();
}
