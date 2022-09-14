package com.seaboxdata.sdps.item.enums;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum DbUrlTypeEnum implements EnumBase {
	M("MYSQL", "com.mysql.jdbc.Driver"), O("ORACLE", "oracle.jdbc.OracleDriver"), P(
			"POSTGRE", "org.postgresql.Driver"), S("SQLSERVER2000",
			"net.sourceforge.jtds.jdbc.Driver"), X("SQLSERVER2005",
			"com.microsoft.sqlserver.jdbc.SQLServerDriver"), H("HIVE",
			"org.apache.hive.jdbc.HiveDriver");
	private String code;
	private String message;

	DbUrlTypeEnum(String code, String message) {
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
