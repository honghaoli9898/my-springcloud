package com.seaboxdata.sdps.bigdataProxy.service.impl;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.bigdataProxy.feign.ItemFeignService;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.service.IStroageResourcesService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

@Service
public class StroageResourcesServiceImpl implements IStroageResourcesService {

	@Autowired
	private ItemFeignService itemFeignService;
	@Autowired
	private IClusterDevOpsService clusterDevOpsService;
	@Autowired
	private CommonBigData commonBigData;

	@Override
	public PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		clusterDevOpsService.getEnableClusterList(Lists.list(storgeRequest.getClusterId()));
		return commonBigData.getItemStorage(storgeRequest);
	}

	@Override
	public PageResult<StorgeDirInfo> getFileStorageByTenant(StorgeRequest storgeRequest) {
		clusterDevOpsService.getEnableClusterList(Lists.list(storgeRequest.getClusterId()));
		return commonBigData.getFileStorageByTenant(storgeRequest);
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		clusterDevOpsService.getEnableClusterList(Lists.list(storgeRequest.getClusterId()));
		return commonBigData.subStorgeTrend(storgeRequest);
	}

	@Override
	public PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		clusterDevOpsService.getEnableClusterList(Lists.list(storgeRequest.getClusterId()));
		return commonBigData.subStorgeRank(storgeRequest);
	}
}
