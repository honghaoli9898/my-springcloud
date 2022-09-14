package com.seaboxdata.sdps.bigdataProxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

public interface ISdpsHdfsDbTableService extends IService<SdpsHdfsDbTable>{

	PageResult<SdpsHdfsDbTable> getDbOrTableList(StorgeRequest storgeRequest);

}
