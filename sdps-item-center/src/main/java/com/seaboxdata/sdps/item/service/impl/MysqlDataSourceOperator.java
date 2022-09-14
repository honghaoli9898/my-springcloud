package com.seaboxdata.sdps.item.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.seaboxdata.sdps.item.mapper.MysqlDataBaseOperatorMapper;
import com.seaboxdata.sdps.item.service.AbstractDataBaseOperator;

@Service("mysql")
public class MysqlDataSourceOperator extends AbstractDataBaseOperator {
	@Autowired
	private MysqlDataBaseOperatorMapper mysqlDataBaseOperatorMapper;

	@DS("#type")
	@Override
	public boolean isExisitDataBase(String type, String databaseName) {
		return StrUtil.isNotBlank(mysqlDataBaseOperatorMapper
				.exisitDatabase(databaseName));
	}

	@DS("#type")
	@Override
	public void createDataBase(String type, String databaseName, String comment) {
		mysqlDataBaseOperatorMapper.createDataBase(databaseName);
	}

}
