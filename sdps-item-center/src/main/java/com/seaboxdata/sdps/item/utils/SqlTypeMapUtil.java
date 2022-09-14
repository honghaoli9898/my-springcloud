package com.seaboxdata.sdps.item.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.seaboxdata.sdps.item.properties.SettingProperties;

/**
 * @author breezes_y@163.com
 * @date 2021/2/10 23:15
 * @description 数据库类型和java类型映射工具类
 */
public class SqlTypeMapUtil {

	private static SqlTypeMapUtil sqlTypeMapUtil;

	private static ConcurrentHashMap<String, ConvertBean> map;

	private SqlTypeMapUtil() {
	}

	public static synchronized SqlTypeMapUtil getInstance() {
		if (sqlTypeMapUtil == null) {
			sqlTypeMapUtil = new SqlTypeMapUtil();
			map = sqlTypeMapUtil.convertMapInit();
		}
		return sqlTypeMapUtil;
	}

	public ConcurrentHashMap<String, ConvertBean> convertMapInit() {
		SettingProperties properties = SettingProperties.getInstance();
		ConcurrentHashMap<String, ConvertBean> convertMap = new ConcurrentHashMap<>();
		convertMap.put("int", new ConvertBean(properties.getIntType(),
				properties.getIntDefaultLength()));
		convertMap.put("bigint", new ConvertBean(properties.getLongType(),
				properties.getLongDefaultLength()));
		convertMap.put("double", new ConvertBean(properties.getDoubleType(),
				properties.getDoubleDefaultLength()));
		convertMap.put("float", new ConvertBean(properties.getFloatType(),
				properties.getFloatDefaultLength()));
		convertMap.put("boolean", new ConvertBean(properties.getBooleanType(),
				properties.getBooleanDefaultLength()));
		convertMap.put("tinyint", new ConvertBean(properties.getBooleanType(),
				properties.getBooleanDefaultLength()));
		convertMap.put("date", new ConvertBean(properties.getDateType(),
				properties.getDateDefaultLength()));
		convertMap.put("datetime", new ConvertBean(properties.getDateType(),
				properties.getDateDefaultLength()));
		convertMap.put("string", new ConvertBean(properties.getStringType(),
				properties.getStringDefaultLength()));
		convertMap.put("char", new ConvertBean(properties.getCharType(),
				properties.getCharDefaultLength()));
		convertMap.put("varchar", new ConvertBean(properties.getVarcharType(),
				properties.getVarcharDefaultLength()));
		convertMap.put(
				"decimal",
				new ConvertBean(properties.getBigDecimalType(), properties
						.getBigDecimalDefaultLength()));
		convertMap.put(
				"timestamp",
				new ConvertBean(properties.getTimestampType(), properties
						.getTimestampDefaultLength()));

		return convertMap;
	}

	public ConvertBean typeConvert(String javaType) {
		if (StringUtils.isBlank(javaType)) {
			return null;
		}
		return map.get(javaType);
	}

	public static class ConvertBean {
		private String sqlType;
		private String sqlTypeLength;

		public ConvertBean() {
		}

		public ConvertBean(String sqlType, String sqlTypeLength) {
			this.sqlType = sqlType;
			this.sqlTypeLength = sqlTypeLength;
		}

		public String getSqlType() {
			return sqlType;
		}

		public void setSqlType(String sqlType) {
			this.sqlType = sqlType;
		}

		public String getSqlTypeLength() {
			return sqlTypeLength;
		}

		public void setSqlTypeLength(String sqlTypeLength) {
			this.sqlTypeLength = sqlTypeLength;
		}
	}
}
