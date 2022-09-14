package com.seaboxdata.sdps.common.framework.enums;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum ServerTypeEnum implements EnumBase {
	R("ranger", "ranger服务"), A("ambari", "amabri服务"), S("sdo", "hue服务"), G(
			"grafana", "garfana服务"), L("slog", "slog服务"), SFL2("sfl2", "sfl2服务"), SLOG2(
			"slog2", "slog2服务"), C("currency", "通用"), D("sredis", "sredis服务"), Y(
			"yarn", "yarn服务"), SEA_OSS("SEA_OSS", "海盒分布式对象存储数据系统 SeaOSS服务"), SDT2(
			"sdt2", "sdt2服务"), SHBASE("SHBASE", "shbase服务"), SKAFKA("skafka",
			"skafka服务"), KDC("kdc", "kadmin服务"), SCS("SCS", "kylin服务"), SMS(
			"SMS", "atlas服务");

	public static String getCodeByName(String name) {

		for (ServerTypeEnum type : ServerTypeEnum.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type.code;
			}
		}
		return null;
	}

	/**
	 * code编码
	 */
	public final String code;
	/**
	 * 中文信息描述
	 */
	final String message;

	ServerTypeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getCode() {
		return code;
	}
}
