package com.seaboxdata.sdps.item.resolver.impl;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.seaboxdata.sdps.item.constant.DataSourceConstant;
import com.seaboxdata.sdps.item.enums.DbTypeEnum;
import com.seaboxdata.sdps.item.resolver.DataSourceResolver;
import com.seaboxdata.sdps.item.utils.DbUtil;

@Slf4j
public class MysqlDataSourceResolver implements DataSourceResolver {
	@Override
	public boolean supportsParameter(String type) {
		return Objects.equals(type.toUpperCase(), DbTypeEnum.M.getCode()
				.toString());
	}

	@Override
	public DataSourceProperty resolveArgument(String type, String properties)
			throws Exception {

		JSONObject jsonObject = JSONUtil.parseObj(properties);
		String host = jsonObject.getStr(DataSourceConstant.HOST, "");
		int port = jsonObject.getInt(DataSourceConstant.PORT, -1);
		String service = jsonObject.getStr(DataSourceConstant.SERVICE, "");
		String username = jsonObject.getStr(DataSourceConstant.USERNAME, "");
		String password = jsonObject.getStr(DataSourceConstant.PASSWORD, "");
		String urlParam = jsonObject.getStr(DataSourceConstant.URL_PARAM, "");
		DataSourceProperty dataSourceProperty = new DataSourceProperty();
		dataSourceProperty.setLazy(true);
		dataSourceProperty.setPassword(password);
		dataSourceProperty.setUsername(username);
		dataSourceProperty.setDriverClassName(DbTypeEnum.M.getMessage());
		dataSourceProperty.setUrl(DbUtil.getUrl(host, port, service,
				DbTypeEnum.M.name(), urlParam));
		log.info("mysql url:{}", dataSourceProperty.getUrl());
		return dataSourceProperty;
	}
}
