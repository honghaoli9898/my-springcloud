package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;

public interface SdpsDatasourceTypeMapper extends
		SuperMapper<SdpsDatasourceType> {

	public List<SdpsDatasourceType> selectDataSourceType(
			@Param("type") String type, @Param("isValid") Boolean isValid);
}