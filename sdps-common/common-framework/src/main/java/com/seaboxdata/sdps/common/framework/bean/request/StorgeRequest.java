package com.seaboxdata.sdps.common.framework.bean.request;

import java.util.Collection;
import java.util.List;

import lombok.Data;

import com.seaboxdata.sdps.common.framework.enums.QueryEnum;

@Data
public class StorgeRequest {
	private Integer clusterId;
	private Integer page;
	private Integer size;
	/**
	 * 排序的列
	 */
	private String orderColumn;
	
	private String orderColumnType;
	/**
	 * 正序倒序，默认倒序
	 */
	private Boolean desc = true;
	/**
	 * 开始日期
	 */
	private String startDay;
	/**
	 * 结束日期
	 */
	private String endDay;
	/**
	 * 租户名集合
	 */
	private Collection<String> tenants;

	/**
	 * 路径深度
	 */
	private Collection<Integer> pathIndexs;

	private Boolean isStartDay;

	private Collection<Integer> types;

	private Collection<String> paths;

	private String datasourceKey;

	private String type;
	/**
	 * 路径名
	 */
	private String path;
	/**
	 * 库名
	 */
	private String dbName;
	/**
	 * 表名
	 */
	private String table;

	private List<String> names;
	/**
	 * 路径深度
	 */
	private Integer pathDepth = 3;

	private QueryEnum storageType;

	private String period;

	private String tenant;
	
	private String searchPath;

}
