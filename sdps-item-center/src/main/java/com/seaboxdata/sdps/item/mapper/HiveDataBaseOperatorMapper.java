package com.seaboxdata.sdps.item.mapper;

import org.apache.ibatis.annotations.Param;

public interface HiveDataBaseOperatorMapper {
	void createDataBase(@Param("name") String databaseName,
			@Param("comment") String comment);
}
