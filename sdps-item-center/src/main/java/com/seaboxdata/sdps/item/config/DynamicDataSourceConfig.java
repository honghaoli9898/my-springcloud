package com.seaboxdata.sdps.item.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.hutool.core.map.MapUtil;

import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.item.resolver.DataSourceResolver;

@Slf4j
@Configuration
public class DynamicDataSourceConfig {

	@Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.dynamic.datasource.master.url}")
	private String url;

	@Value("${spring.datasource.dynamic.datasource.master.username}")
	private String username;

	@Value("${spring.datasource.dynamic.datasource.master.password}")
	private String password;

	@Bean
	public DynamicDataSourceProvider jdbcDynamicDataSourceProvider(
			KerberosProperties kerberosProperties,
			DataSourceResolver dataSourceResolver) {
		return new AbstractJdbcDataSourceProvider(driverClassName, url,
				username, password) {
			@Override
			protected Map<String, DataSourceProperty> executeStmt(
					Statement statement) throws SQLException {
				ResultSet rs = statement
						.executeQuery("select sd.name,sd.properties,sdt.name as type from sdps_datasource as sd inner join sdps_datasource_type as sdt on sdt.id = sd.type_id where sd.is_valid = 1");
				Map<String, DataSourceProperty> map = MapUtil.newHashMap();
				while (rs.next()) {
					String name = rs.getString("name");
					String properties = rs.getString("properties");
					String type = rs.getString("type");
					DataSourceProperty dataSourceProperty = null;
					try {
						dataSourceProperty = dataSourceResolver
								.resolveArgument(type, properties);
						map.put(name, dataSourceProperty);
					} catch (Exception e) {
						log.error("解析数据源报错", e);
					}
				}
				return map;
			}
		};
	}

}
