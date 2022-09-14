package com.seaboxdata.sdps.item.service.impl;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.item.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.item.service.IClusterDevOpsService;

@Service
public class ClusterDevOpsServiceImpl extends
		SuperServiceImpl<SdpsClusterMapper, SdpsCluster> implements
		IClusterDevOpsService {

	@Override
	public Integer getClusterAssItemCnt(Long item) {
		return null;
	}

}
