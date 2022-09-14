package com.seaboxdata.sdps.item.utils;

import java.util.List;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.item.model.Field;
import com.seaboxdata.sdps.item.model.SdpsTable;

/**
 * @author breezes_y@163.com
 * @date 2021/1/30 16:48
 * @description
 */
public class MysqlDdlFormat implements DdlFormat {

	public DdlBuilder buildDdlTableHeadScript(DdlBuilder builder,
			SdpsTable table) {
		builder.create().tableName(table.getEnName()).LeftParenthesis(true)
				.wrap();
		return builder;
	}

	public String buildDdlFieldScript(SdpsTable table) {
		if (!table.getCreateMode()) {
			return table.getFieldSql();
		}
		DdlBuilder builder = buildDdlTableHeadScript(new MysqlDdlBuilder(),
				table);
		List<Field> fieldList = JSONObject.parseArray(table.getFieldSql(),
				Field.class);
		int maxFieldStringLength = 0;
		int maxFieldSqlTypeStringLength = 0;
		for (Field field : fieldList) {
			if (maxFieldStringLength <= field.getTableColumn().length()) {
				maxFieldStringLength = field.getTableColumn().length();
			}
			if (maxFieldSqlTypeStringLength <= field.getSqlType().length()) {
				maxFieldSqlTypeStringLength = field.getSqlType().length();
			}
		}
		maxFieldStringLength++;
		maxFieldSqlTypeStringLength++;

		for (Field field : fieldList) {
			String tableColumn = field.getTableColumn();
			builder = ((MysqlDdlBuilder) ((MysqlDdlBuilder) builder
					.space(4)
					.addColumn(
							String.format("%-" + maxFieldStringLength + "s",
									tableColumn))
					.addType(
							String.format("%-" + maxFieldSqlTypeStringLength
									+ "s", field.getSqlType())))
					.isPrimaryKey(field.getPrimaryKey())).isNullOrDefaultValue(
					field.getPrimaryKey(), field.getIsNull(),
					field.getDefaultValue());
			if (StrUtil.isNotBlank(field.getComment())) {
				builder.space().addComment(field.getComment());
			}
			builder.addComma().wrap();
		}
		builder = ((MysqlDdlBuilder) builder.remove(2).wrap()
				.rightParenthesis()).addEngine(table.getDescription());
		return builder.end();
	}

	public static void main(String[] args) {
		MysqlDdlFormat mysqlDdlFormat = new MysqlDdlFormat();
		SdpsTable sdpsTable = new SdpsTable();
		String fields = "[{'name':'id','type':'int','size':'11','primaryKey':true,'comment':'字段id','location':'1','isNull':false},{'name':'name','type':'varchar','size':'24','comment':'字段name','location':'2','isNull':true},{'name':'date','type':'date','comment':'时间分区','location':'3','isNull':true}]";
		sdpsTable.setFieldSql(fields);
		sdpsTable.setCreateMode(true);
		sdpsTable.setDescription("test表");
		sdpsTable.setEnName("test");
		System.out.println(mysqlDdlFormat.buildDdlFieldScript(sdpsTable));
	}
}
