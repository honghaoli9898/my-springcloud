package com.seaboxdata.sdps.bigdataProxy.mapper;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

public interface SdpsHdfsDbTableMapper extends SuperMapper<SdpsHdfsDbTable> {
	Page<SdpsHdfsDbTable> selectDbOrTablePage(
			@Param("request") StorgeRequest storgeRequest);

}