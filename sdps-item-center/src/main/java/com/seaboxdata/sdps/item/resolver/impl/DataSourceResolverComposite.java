package com.seaboxdata.sdps.item.resolver.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.item.resolver.DataSourceResolver;

@Component
public class DataSourceResolverComposite implements DataSourceResolver {
	private final List<DataSourceResolver> dataSourceResolvers = new ArrayList<>();
	private final Map<String, DataSourceResolver> dataSourceResolverCache = new ConcurrentHashMap<>(
			4);

	public DataSourceResolverComposite() {
		dataSourceResolvers.add(new MysqlDataSourceResolver());
		dataSourceResolvers.add(new HiveDataSourceResolver());
	}

	public DataSourceResolverComposite addResolver(DataSourceResolver resolver) {
		this.dataSourceResolvers.add(resolver);
		return this;
	}

	public DataSourceResolverComposite addResolvers(
			DataSourceResolver... resolvers) {
		if (resolvers != null) {
			Collections.addAll(this.dataSourceResolvers, resolvers);
		}
		return this;
	}

	public DataSourceResolverComposite addResolvers(
			List<? extends DataSourceResolver> resolvers) {

		if (resolvers != null) {
			this.dataSourceResolvers.addAll(resolvers);
		}
		return this;
	}

	public List<DataSourceResolver> getResolvers() {
		return Collections.unmodifiableList(this.dataSourceResolvers);
	}

	public void clear() {
		this.dataSourceResolvers.clear();
	}

	@Override
	public boolean supportsParameter(String type) {
		return getDataSourceResolver(type) != null;
	}

	@Override
	public DataSourceProperty resolveArgument(String type, String properties)
			throws Exception {
		DataSourceResolver resolver = getDataSourceResolver(type);
		if (resolver == null) {
			throw new BusinessException("Unsupported parameter type [" + type
					+ "]. supportsParameter should be called first.");
		}
		return resolver.resolveArgument(type, properties);
	}

	private DataSourceResolver getDataSourceResolver(String type) {
		DataSourceResolver result = this.dataSourceResolverCache.get(type
				.toUpperCase());
		if (result == null) {
			for (DataSourceResolver resolver : this.dataSourceResolvers) {
				if (resolver.supportsParameter(type.toLowerCase())) {
					result = resolver;
					this.dataSourceResolverCache
							.put(type.toUpperCase(), result);
					break;
				}
			}
		}
		return result;
	}

}
