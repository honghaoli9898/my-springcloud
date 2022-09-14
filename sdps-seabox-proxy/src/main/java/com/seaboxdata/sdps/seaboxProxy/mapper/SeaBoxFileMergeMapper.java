package com.seaboxdata.sdps.seaboxProxy.mapper;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import org.apache.ibatis.annotations.Param;

/**
 * 这里DS注解，p0代表第一个参数 p1代表第二个参数
 */
public interface SeaBoxFileMergeMapper {

	@DS("#p1")
	com.github.pagehelper.Page<StorgeDirInfo> getItemStorage(
			@Param("storgeRequest") StorgeRequest storgeRequest, String datasourceKey);

}
