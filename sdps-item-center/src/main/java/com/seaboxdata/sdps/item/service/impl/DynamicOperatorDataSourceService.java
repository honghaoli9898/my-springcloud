package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.BasicDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.BeeCpDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.Dbcp2DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.JndiDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceTypeMapper;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.resolver.DataSourceResolver;
import com.seaboxdata.sdps.item.resolver.impl.DataSourceResolverComposite;

@Service
@Slf4j
public class DynamicOperatorDataSourceService {
	@Autowired
	private DataSourceResolverComposite dataSourceResolverComposite;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private DefaultDataSourceCreator dataSourceCreator;
	@Autowired
	private BasicDataSourceCreator basicDataSourceCreator;
	@Autowired
	private JndiDataSourceCreator jndiDataSourceCreator;
	@Autowired
	private DruidDataSourceCreator druidDataSourceCreator;
	@Autowired
	private HikariDataSourceCreator hikariDataSourceCreator;
	@Autowired
	private BeeCpDataSourceCreator beeCpDataSourceCreator;
	@Autowired
	private Dbcp2DataSourceCreator dbcp2DataSourceCreator;

	@Autowired
	private SdpsDatasourceMapper datasourceMapper;
	@Autowired
	private SdpsDatasourceTypeMapper datasourceTypeMapper;
	@Autowired
	private DataSourceResolver dataSourceResolver;

	public Set<String> add(String poolName,
			DataSourceProperty dataSourceProperty) {
		dataSourceProperty.setPoolName(poolName);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = dataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addJndi(String pollName, String jndiName) {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = jndiDataSourceCreator
				.createDataSource(jndiName);
		ds.addDataSource(pollName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addDruid(String poolName,
			DataSourceProperty dataSourceProperty) {
		dataSourceProperty.setLazy(true);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = druidDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addHikariCP(String poolName,
			DataSourceProperty dataSourceProperty) {
		dataSourceProperty.setLazy(true);// 3.4.0版本以下如果有此属性，需手动设置，不然会空指针。
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = hikariDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addBeeCp(String poolName,
			DataSourceProperty dataSourceProperty) {
		dataSourceProperty.setLazy(true);// 3.4.0版本以下如果有此属性，需手动设置，不然会空指针。
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = beeCpDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addDbcp(String poolName,
			DataSourceProperty dataSourceProperty) {
		dataSourceProperty.setLazy(true);// 3.4.0版本以下如果有此属性，需手动设置，不然会空指针。
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = dbcp2DataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Result remove(String name) {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		ds.removeDataSource(name);
		return Result.succeed(ds.getCurrentDataSources().keySet(), "操作成功");
	}

	public Set<String> add(String poolName, String type, String properties)
			throws Exception {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		Set<String> poolNames = ds.getCurrentDataSources().keySet();
		if (poolNames.contains(poolName)) {
			return poolNames;
		}
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(type, properties);
		DataSource dataSource = dataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return poolNames;
	}

	public Set<String> addDruid(String poolName, String type, String properties)
			throws Exception {
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(type, properties);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = druidDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addHikariCP(String poolName, String type,
			String properties) throws Exception {
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(type, properties);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = hikariDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addBeeCp(String poolName, String type, String properties)
			throws Exception {
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(type, properties);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = beeCpDataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> addDbcp(String poolName, String type, String properties)
			throws Exception {
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(type, properties);
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		DataSource dataSource = dbcp2DataSourceCreator
				.createDataSource(dataSourceProperty);
		ds.addDataSource(poolName, dataSource);
		return ds.getCurrentDataSources().keySet();
	}

	public Set<String> getCurrentDataSources() {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		return ds.getCurrentDataSources().keySet();
	}

	public void restartDataSources(List<String> poolNames) {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		Set<String> allPoolName = ds.getCurrentDataSources().keySet();
		poolNames.forEach(poolName -> {
			if (allPoolName.contains(poolName)) {
				remove(poolName);
				SdpsDatasource datasource = datasourceMapper
						.selectOne(new QueryWrapper<SdpsDatasource>().eq(
								"name", poolName));
				SdpsDatasourceType datasourceType = datasourceTypeMapper
						.selectById(datasource.getTypeId());
				try {
					DataSourceProperty dataSourceProperty = dataSourceResolver
							.resolveArgument(datasourceType.getName(),
									datasource.getProperties());
					add(poolName, dataSourceProperty);
				} catch (Exception e) {
					log.error("解析数据源报错", e);
				}
			}
		});
	}

}
