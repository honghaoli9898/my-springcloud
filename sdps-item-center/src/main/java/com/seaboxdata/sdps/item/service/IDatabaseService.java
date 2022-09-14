package com.seaboxdata.sdps.item.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.item.dto.database.DatabaseDto;
import com.seaboxdata.sdps.item.model.SdpsDatabase;
import com.seaboxdata.sdps.item.vo.database.DatabaseRequest;

public interface IDatabaseService extends ISuperService<SdpsDatabase> {

	void saveAndCreateDatabase(Long userId, SdpsDatabase database);

	void deleteDatabaseByIds(Long userId, DatabaseRequest request);

	List<DatabaseDto> selectDatabase(DatabaseRequest request);

	Map<String, String> getItemInfoByDatabaseName(Set<String> nameSet);

	PageResult<DatabaseDto> findDatabases(Integer page, Integer size,
			DatabaseRequest param);

}
