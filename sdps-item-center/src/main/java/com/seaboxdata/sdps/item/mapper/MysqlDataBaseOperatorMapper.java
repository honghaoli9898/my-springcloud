package com.seaboxdata.sdps.item.mapper;

import org.apache.ibatis.annotations.Param;

public interface MysqlDataBaseOperatorMapper {
	void createDataBase(@Param("name") String databaseName);

	String exisitDatabase(@Param("name") String databaseName);
}
