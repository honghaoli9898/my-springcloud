package com.seaboxdata.sdps.item.service;

import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.item.mapper.DataBaseCommonOperatorMapper;

public class AbstractDataBaseOperator implements DataBaseOperator {

	@DS("#type")
	@Override
	public void dropDatabase(String type, String databaseName) {
		DataBaseCommonOperatorMapper commonOperatorMapper = SpringUtil
				.getBean(DataBaseCommonOperatorMapper.class);
		commonOperatorMapper.dropDatabase(databaseName);
	}

	@DS("#type")
	@Override
	public List<String> selectDatabases(String type) {
		DataBaseCommonOperatorMapper commonOperatorMapper = SpringUtil
				.getBean(DataBaseCommonOperatorMapper.class);
		return commonOperatorMapper.selectDatabases();
	}

	@DS("#type")
	@Override
	public List<String> selectTables(String type, String databaseName) {
		DataBaseCommonOperatorMapper commonOperatorMapper = SpringUtil
				.getBean(DataBaseCommonOperatorMapper.class);
		return commonOperatorMapper.selectTables(databaseName);
	}

	@DS("#type")
	@Override
	public boolean isExisitDataBase(String type, String databaseName) {
		return false;
	}

	@DS("#type")
	@Override
	public void createDataBase(String type, String databaseName, String comment) {

	}

	@DS("#type")
	@Override
	public void dropTable(String type, String databaseName, String table) {
		DataBaseCommonOperatorMapper commonOperatorMapper = SpringUtil
				.getBean(DataBaseCommonOperatorMapper.class);
		commonOperatorMapper.dropTable(databaseName, table);
	}

}
