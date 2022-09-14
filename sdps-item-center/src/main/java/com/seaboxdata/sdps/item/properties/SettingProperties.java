package com.seaboxdata.sdps.item.properties;

import static com.seaboxdata.sdps.item.enums.SqlTypeAndJavaTypeEnum.*;

import java.util.Objects;

import lombok.Getter;

@Getter
public class SettingProperties {
	private static SettingProperties settingProperties = null;

	private SettingProperties() {

	}

	public static SettingProperties getInstance() {
		if (Objects.isNull(settingProperties)) {
			settingProperties = new SettingProperties();
		}
		return settingProperties;
	}

	private String intType = INT.getSqlType();

	private String longType = BIGINT.getSqlType();

	private String stringType = STRING.getSqlType();

	private String varcharType = VARCHAR.getSqlType();

	private String charType = CHAR.getSqlType();

	private String booleanType = TINYINT.getSqlType();

	private String dateType = DATE.getSqlType();
	
	private String dateTimeType = DATETIME.getSqlType();

	private String timestampType = TIMESTAMP.getSqlType();

	private String doubleType = DOUBLE.getSqlType();

	private String floatType = DOUBLE.getSqlType();

	private String bigDecimalType = DECIMAL.getSqlType();

	private String intDefaultLength = INT.getDefaultLength();

	private String longDefaultLength = BIGINT.getDefaultLength();

	private String stringDefaultLength = STRING.getDefaultLength();

	private String varcharDefaultLength = VARCHAR.getDefaultLength();

	private String charDefaultLength = CHAR.getDefaultLength();

	private String doubleDefaultLength = DOUBLE.getDefaultLength();

	private String floatDefaultLength = DOUBLE.getDefaultLength();

	private String booleanDefaultLength = TINYINT.getDefaultLength();

	private String dateDefaultLength = DATE.getDefaultLength();

	private String timestampDefaultLength = TIMESTAMP.getDefaultLength();

	private String bigDecimalDefaultLength = DECIMAL.getDefaultLength();
}