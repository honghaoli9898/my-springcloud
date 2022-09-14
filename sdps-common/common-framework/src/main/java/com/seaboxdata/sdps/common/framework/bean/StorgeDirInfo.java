package com.seaboxdata.sdps.common.framework.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 资源统计实体类
 */
@Getter
@Setter
public class StorgeDirInfo extends TbDirInfo {
	private String inc;

	private String incRate;

	private String totalFileSizeStr;

	private String startTotalFileSizeStr;

	private Long startTotalFileSize;

	private String dbName;

	private String tableName;

	private String dbType;
}
