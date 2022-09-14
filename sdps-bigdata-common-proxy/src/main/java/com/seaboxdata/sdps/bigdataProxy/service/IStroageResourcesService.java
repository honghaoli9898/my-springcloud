package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

public interface IStroageResourcesService {

	PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest);

	PageResult<StorgeDirInfo> getFileStorageByTenant(StorgeRequest storgeRequest);

	PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest);

	PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest);

}
