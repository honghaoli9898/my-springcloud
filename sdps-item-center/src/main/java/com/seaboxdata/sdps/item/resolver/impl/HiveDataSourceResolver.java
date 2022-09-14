package com.seaboxdata.sdps.item.resolver.impl;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.seaboxdata.sdps.item.constant.DataSourceConstant;
import com.seaboxdata.sdps.item.enums.DbTypeEnum;
import com.seaboxdata.sdps.item.resolver.DataSourceResolver;

@Slf4j
public class HiveDataSourceResolver implements DataSourceResolver {
	@Override
	public boolean supportsParameter(String type) {
		return Objects.equals(type.toUpperCase(), DbTypeEnum.H.getCode()
				.toString());
	}

	@Override
	public DataSourceProperty resolveArgument(String type, String properties)
			throws Exception {
		JSONObject jsonObject = JSONUtil.parseObj(properties);
		String host = jsonObject.getStr(DataSourceConstant.HOST, "");
		int port = jsonObject.getInt(DataSourceConstant.PORT, -1);
		String service = jsonObject.getStr(DataSourceConstant.SERVICE, "");
		boolean server_ha = jsonObject.getBool(DataSourceConstant.SERVER_HA,
				false);
		String username = jsonObject.getStr(DataSourceConstant.USERNAME, "");
		String password = jsonObject.getStr(DataSourceConstant.PASSWORD, "");
		String urlParam = jsonObject.getStr(DataSourceConstant.URL_PARAM, "");

		DataSourceProperty dataSourceProperty = new DataSourceProperty();
		dataSourceProperty.setLazy(false);
		dataSourceProperty.setPassword(password);
		dataSourceProperty.setUsername(username);
		dataSourceProperty.setDriverClassName(DbTypeEnum.H.getMessage());
		String url = null;
		StringBuffer sb = new StringBuffer();

		if (server_ha) {
			sb.append("jdbc:hive2://")
					.append(host)
					.append("/")
					.append(service)
					.append(StrUtil.isBlank(urlParam) ? ";serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2"
							: urlParam);
			url = sb.toString();
		} else {
			sb.append("jdbc:hive2://")
					.append(host)
					.append(":")
					.append(port)
					.append("/")
					.append(service)
					.append(StrUtil.isBlank(urlParam) ? ";auth=noSasl"
							: urlParam).toString();
			url = sb.toString();
		}
		log.info("hive url:{}", url);
		dataSourceProperty.setUrl(url);
		return dataSourceProperty;
	}
}
