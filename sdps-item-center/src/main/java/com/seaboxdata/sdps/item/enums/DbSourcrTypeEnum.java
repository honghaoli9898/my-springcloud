package com.seaboxdata.sdps.item.enums;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum DbSourcrTypeEnum implements EnumBase {
	extend("extend", "外部可扩展的"), cluster("cluster", "集群初始化的"),S("datasource","数据库类型");
	private String code;
	private String message;

	DbSourcrTypeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public Object getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
