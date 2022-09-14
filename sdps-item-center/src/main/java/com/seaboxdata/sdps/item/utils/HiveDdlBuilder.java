package com.seaboxdata.sdps.item.utils;

import java.util.List;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.item.model.Field;
import com.seaboxdata.sdps.item.model.HiveDelimiter;

public class HiveDdlBuilder extends DdlBuilder {
	public DdlBuilder addTableType(Boolean isExternal) {
		if (Objects.isNull(isExternal))
			return this;
		if (isExternal) {
			ddl.append("EXTERNAL");
			return this.space();
		}
		return this;
	}

	public DdlBuilder addHivePartition(List<Field> fields) {
		ddl.append("partitioned by");
		LeftParenthesis(false);
		fields.forEach(field -> {
			ddl.append("`").append(field.getTableColumn()).append("` ")
					.append(field.getSqlType());
			if (StrUtil.isNotBlank(field.getComment())) {
				space().addComment(field.getComment());
			}
			ddl.append(",");
		});
		remove(1);
		rightParenthesis().wrap();
		return this;
	}

	public DdlBuilder addHiveFormat(String senior) {
		if (StrUtil.isBlank(senior)) {
			senior = "{}";
		}
		HiveDelimiter delimiter = JSONObject.parseObject(senior,
				HiveDelimiter.class);
		ddl.append("row format delimited");
		wrap();
		ddl.append("fields terminated by '");
		if (StrUtil.isNotBlank(delimiter.getFieldDiv())) {
			ddl.append(delimiter.getFieldDiv()).append("'");
		} else {
			ddl.append("\t'");
		}
		wrap();
		if (StrUtil.isNotBlank(delimiter.getArrayDiv())) {
			ddl.append("collection items terminated by '")
					.append(delimiter.getArrayDiv()).append("'");
			wrap();
		}

		if (StrUtil.isNotBlank(delimiter.getMapDiv())) {
			ddl.append("map keys terminated by '")
					.append(delimiter.getMapDiv()).append("'");
			wrap();
		}
		ddl.append("stored as ");
		if (StrUtil.isNotBlank(delimiter.getFileFormat())) {
			ddl.append(delimiter.getFileFormat());
		} else {
			ddl.append("textfile");
		}
		return this;
	}
}
