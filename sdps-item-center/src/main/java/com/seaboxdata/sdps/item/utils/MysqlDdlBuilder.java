package com.seaboxdata.sdps.item.utils;

import java.util.Objects;

import cn.hutool.core.util.StrUtil;

public class MysqlDdlBuilder extends DdlBuilder {
	public DdlBuilder isPrimaryKey(Boolean isPrimaryKey) {
		if (Objects.isNull(isPrimaryKey)) {
			return this;
		}
		if (isPrimaryKey) {
			ddl.append("NOT NULL AUTO_INCREMENT PRIMARY KEY");
		}
		return this;
	}

	public DdlBuilder isNullOrDefaultValue(Boolean isPrimaryKey,
			Boolean isNull, String value) {
		if (Objects.nonNull(isPrimaryKey) && isPrimaryKey) {
			return this;
		}
		if (Objects.isNull(isPrimaryKey) || Objects.isNull(isNull) || isNull) {
			ddl.append(" DEFAULT NULL");
		} else if (!isNull) {
			ddl.append(" NOT NULL");
			if (StrUtil.isNotBlank(value)) {
				ddl.append(" DEFAULT ").append(value);
			}
		}
		return this;
	}

	public DdlBuilder addEngine(String comment) {
		ddl.append(
				"ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='")
				.append(comment).append("'");
		return this;
	}
}
