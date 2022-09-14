package com.seaboxdata.sdps.seaboxProxy.service;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

public interface IStorageResourceService {

	Page<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest);

	Page<StorgeDirInfo> getFileStorageByTenant(StorgeRequest storgeRequest);

	Page<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest);

	Page<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest);

}
