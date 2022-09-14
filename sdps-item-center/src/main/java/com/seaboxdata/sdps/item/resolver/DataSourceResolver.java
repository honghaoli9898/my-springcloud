package com.seaboxdata.sdps.item.resolver;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;

public interface DataSourceResolver {
	boolean supportsParameter(String type);

	DataSourceProperty resolveArgument(String type,String properties) throws Exception;
}
