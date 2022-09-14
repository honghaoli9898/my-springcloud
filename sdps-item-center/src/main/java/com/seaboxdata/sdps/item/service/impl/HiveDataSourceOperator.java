package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.seaboxdata.sdps.item.mapper.HiveDataBaseOperatorMapper;
import com.seaboxdata.sdps.item.service.AbstractDataBaseOperator;

@Service("hive")
public class HiveDataSourceOperator extends AbstractDataBaseOperator {
	@Autowired
	private HiveDataBaseOperatorMapper hiveDataBaseOperatorMapper;
	@Autowired
	private DynamicSqlExecutor dynamicSqlExecutor;

	@DS("#type")
	@Override
	public boolean isExisitDataBase(String type, String databaseName) {
		List<String> databases = selectDatabases(type);
		return databases.contains(databaseName);
	}

	@DS("#type")
	@Override
	public void createDataBase(String type, String databaseName, String comment) {
		hiveDataBaseOperatorMapper.createDataBase(databaseName, comment);
	}

	@DS("#type")
	@Override
	public List<String> selectTables(String type, String databaseName) {
		String sql = "use " + databaseName + ";\n" + "show tables";
		List<Map<String, Object>> result = dynamicSqlExecutor.querySql(type,
				sql);
		if (CollUtil.isEmpty(result)) {
			return CollUtil.newArrayList();
		}
		return result.stream().map(map -> {
			return map.entrySet().stream().map(entry -> {
				return entry.getValue().toString();
			}).collect(Collectors.toList());
		}).reduce((a, b) -> {
			a.addAll(b);
			return a;
		}).get();
	}

	@DS("#type")
	@Override
	public void dropTable(String type, String databaseName, String table) {
		String sql = "use " + databaseName + ";\n" + "drop table " + table
				+ ";";
		dynamicSqlExecutor.executeSql(type, sql);
	}

}
