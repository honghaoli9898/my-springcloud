package com.seaboxdata.sdps.item.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.database.DatabaseDto;
import com.seaboxdata.sdps.item.model.SdpsDatabase;
import com.seaboxdata.sdps.item.vo.database.DatabaseRequest;

public interface SdpsDatabaseMapper extends SuperMapper<SdpsDatabase> {

	Page<DatabaseDto> findDatabasesByExample(@Param("param") DatabaseRequest param);

	List<Map<String, String>> getItemInfoByDatabaseName(@Param("names") Set<String> names);
}