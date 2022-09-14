package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DataBaseCommonOperatorMapper {

	void dropDatabase(@Param("name") String databaseName);

	List<String> selectDatabases();

	List<String> selectTables(@Param("name") String databaseName);

	void dropTable(@Param("databaseName") String databaseName,
			@Param("tableName") String table);

}
