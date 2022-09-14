package com.seaboxdata.sdps.item.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.item.model.Field;
import com.seaboxdata.sdps.item.model.SdpsTable;

public class HiveDdlFormat implements DdlFormat {

	public DdlBuilder buildDdlTableHeadScript(HiveDdlBuilder builder,
			SdpsTable table) {
		((HiveDdlBuilder) builder.create()).addTableType(table.getIsExternal())
				.tableName(table.getEnName()).LeftParenthesis(true).wrap();
		return builder;
	}
	public static List<String> getDuplicateElements(List<Field> list) {
		return list.stream() //
                .map(e -> { // 获取deptCode或deptAlias的Stream
                    return e.getName();
                }).collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
                .entrySet().stream() // 所有 entry 对应的 Stream
                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
                .collect(Collectors.toList()); // 转化为 List
    }
	public String buildDdlFieldScript(SdpsTable table) {
		if (!table.getCreateMode()) {
			return table.getFieldSql();
		}
		DdlBuilder builder = buildDdlTableHeadScript(new HiveDdlBuilder(),
				table);
		List<Field> fields = JSONObject.parseArray(table.getFieldSql(),
				Field.class);
		List<String> duplicateFieldList= getDuplicateElements(fields);
		if(CollUtil.isNotEmpty(duplicateFieldList)){
			throw new BusinessException("存在重复字段:["+duplicateFieldList+"]");
		}
		List<Field> partitionList = fields
				.stream()
				.filter(field -> {
					return (Objects.nonNull(field.getPartition()) && field
							.getPartition());
				}).sorted(Comparator.comparing(Field::getLocation))
				.collect(Collectors.toList());
		List<Field> fieldList = fields
				.stream()
				.filter(field -> {
					return (Objects.isNull(field.getPartition()) || !field
							.getPartition());
				}).sorted(Comparator.comparing(Field::getLocation))
				.collect(Collectors.toList());
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
			builder = builder
					.space(4)
					.addColumn(
							String.format("%-" + maxFieldStringLength + "s",
									tableColumn))
					.addType(
							String.format("%-" + maxFieldSqlTypeStringLength
									+ "s", field.getSqlType()));
			if (StrUtil.isNotBlank(field.getComment())) {
				builder.space().addComment(field.getComment());
			}
			builder.addComma().wrap();
		}

		builder = builder.remove(2).wrap().rightParenthesis();
		builder = builder.space().addComment(table.getCnName());
		if (CollUtil.isNotEmpty(partitionList)) {
			((HiveDdlBuilder) builder).addHivePartition(partitionList);
		}
		((HiveDdlBuilder) builder).addHiveFormat(table.getSenior());
		return builder.end();
	}

	public static void main(String[] args) {
		HiveDdlFormat hiveDdlFormat = new HiveDdlFormat();
		SdpsTable sdpsTable = new SdpsTable();
		String fields = "[{'name':'id','type':'int','comment':'字段id','partition':'false'},{'name':'name','type':'string','comment':'字段name','location':'2','partition':false},{'name':'date','type':'string','comment':'时间分区','location':'3','partition':true}]";
		sdpsTable.setFieldSql(fields);
		sdpsTable.setCreateMode(true);
		sdpsTable.setIsExternal(true);
		sdpsTable.setEnName("test");
		System.out.println(hiveDdlFormat.buildDdlFieldScript(sdpsTable));
	}

}
