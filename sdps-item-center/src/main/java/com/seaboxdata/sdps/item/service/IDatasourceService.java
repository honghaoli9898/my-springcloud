package com.seaboxdata.sdps.item.service;

import java.util.List;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceParam;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.vo.datasource.DataSourceRequest;

public interface IDatasourceService extends ISuperService<SdpsDatasource> {

	public List<SdpsDatasourceType> findDataSourceType(DataSourceRequest request);

	public void saveDataSourceType(SdpsDatasource datasource) throws Exception;

	public PageResult<DataSourceDto> findDatasourcePage(
			PageRequest<DataSourceRequest> request);

	public List<SdpsDatasourceParam> findDatasourceParamByType(
			DataSourceRequest request);

	public void updateDatasourceById(SdpsDatasource datasource)
			throws Exception;

	public void deleteDatasourceByIds(DataSourceRequest request);

	public Result<List<DataSourceDto>> findDatasource(DataSourceRequest request);

	public Result<List<SdpsDatasourceType>> selectDataSourceType(
			DataSourceRequest request);

	String getHivePrincipal(Integer clusterId);

	void updateKeytabs(List<SdpServerKeytab> list);

}
