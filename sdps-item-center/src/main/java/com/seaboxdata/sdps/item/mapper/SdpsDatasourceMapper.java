package com.seaboxdata.sdps.item.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.vo.datasource.DataSourceRequest;
import com.seaboxdata.sdps.item.vo.item.DataHistogramRequest;

public interface SdpsDatasourceMapper extends SuperMapper<SdpsDatasource> {

	Page<DataSourceDto> selectDatasource(@Param("param") DataSourceRequest param);

	List<DataSourceDto> selectClusterIdAssItem(
			@Param("itemIds") Collection<Long> itemIds);

	List<String> selectKerberosHive(
			@Param("clusterIds") List<Integer> clusterIds);

	List<DataSourceDto> selectHistogram(@Param("request") DataHistogramRequest request);
}