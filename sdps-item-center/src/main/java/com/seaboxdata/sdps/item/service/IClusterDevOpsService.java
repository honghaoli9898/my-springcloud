package com.seaboxdata.sdps.item.service;

import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;

public interface IClusterDevOpsService extends ISuperService<SdpsCluster> {
	public Integer getClusterAssItemCnt(Long item);
}
