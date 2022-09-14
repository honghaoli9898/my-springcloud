package com.seaboxdata.sdps.item.service;

import java.util.List;

public interface DataBaseOperator {
	boolean isExisitDataBase(String type, String databaseName);

	void createDataBase(String type, String databaseName, String comment);

	void dropDatabase(String type, String databaseName);

	List<String> selectDatabases(String type);

	List<String> selectTables(String type, String databaseName);

	void dropTable(String type, String databaseName, String table);
}
