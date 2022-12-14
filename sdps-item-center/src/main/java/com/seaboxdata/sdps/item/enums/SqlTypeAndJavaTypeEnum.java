package com.seaboxdata.sdps.item.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author breezes_y@163.com
 * @date 2021/1/30 19:02
 * @description
 */
public enum SqlTypeAndJavaTypeEnum {

	BIGINT("BIGINT", Arrays.asList("long", "Long"), "(20)"), INT("INT", Arrays
			.asList("int", "Integer"), ""), VARCHAR("VARCHAR", Arrays
			.asList("String"), "(255)"), TINYINT("TINYINT", Arrays.asList(
			"boolean", "Boolean"), "(1)"), DOUBLE("DOUBLE", Arrays.asList(
			"double", "float", "Double", "Float"), ""), DATE("DATE",
			Collections.singletonList("Date"), ""), DECIMAL("DECIMAL",
			Collections.singletonList("BigDecimal"), "(18,2)"), STRING(
			"string", Collections.singletonList("String"), ""), CHAR("CHAR",
			Arrays.asList("char"), "(1)"), TIMESTAMP("TIMESTAMP", Collections
			.singletonList("Date"), "(0)"),DATETIME("DATETIME", Collections
					.singletonList("Date"), "");

	private final String sqlType;

	private final List<String> javaType;

	private final String defaultLength;

	SqlTypeAndJavaTypeEnum(String sqlType, List<String> javaType,
			String defaultLength) {
		this.sqlType = sqlType;
		this.javaType = javaType;
		this.defaultLength = defaultLength;
	}

	public static SqlTypeAndJavaTypeEnum findByJavaType(String javaType) {
		if (StringUtils.isBlank(javaType)) {
			throw new RuntimeException("异常实体");
		}
		for (SqlTypeAndJavaTypeEnum sqlTypeEnum : values()) {
			if (sqlTypeEnum.getJavaType().contains(javaType)) {
				return sqlTypeEnum;
			}
		}
		return VARCHAR;
	}

	public String getSqlType() {
		return sqlType;
	}

	public List<String> getJavaType() {
		return javaType;
	}

	public String getDefaultLength() {
		return defaultLength;
	}
}
