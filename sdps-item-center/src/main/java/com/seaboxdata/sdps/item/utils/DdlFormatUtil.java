package com.seaboxdata.sdps.item.utils;

import java.util.Map;
import java.util.Objects;

import cn.hutool.core.map.MapUtil;

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.item.enums.DbTypeEnum;
import com.seaboxdata.sdps.item.model.SdpsTable;

public class DdlFormatUtil {
	private static final Map<String, DdlFormat> ddlFormatMap = MapUtil
			.newConcurrentHashMap(4);
	static {
		ddlFormatMap.put(DbTypeEnum.M.getCode().toString(),
				new MysqlDdlFormat());
		ddlFormatMap
				.put(DbTypeEnum.H.getCode().toString(), new HiveDdlFormat());
	}

	public static String buildDdlFieldScript(String type, SdpsTable table) {
		DdlFormat ddlFormat = ddlFormatMap.get(type.toUpperCase());
		if (Objects.isNull(ddlFormat)) {
			throw new BusinessException("暂不支持该类型的表创建");
		}
		return ddlFormat.buildDdlFieldScript(table);
	}
}
