package com.seaboxdata.sdps.common.framework.enums;

import java.util.List;

import cn.hutool.core.collection.CollUtil;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum UserSyncEnum implements EnumBase {
	SMCS("ambari", "ambari用户类型"), SSM("ranger", "ranger用户类型"), KERBEROS(
			"kerberos", "kerberos类型"),LINUX("linux", "linux用户类型");

	public static List<String> getAllCode() {
		List<String> codes = CollUtil.newArrayList();
		for (UserSyncEnum userSyncEnum : UserSyncEnum.values()) {
			codes.add(userSyncEnum.name().toLowerCase());
		}
		return codes;
	}

	public static UserSyncEnum getEnumByCode(String code) {
		for (UserSyncEnum userSyncEnum : UserSyncEnum.values()) {
			if (userSyncEnum.code.equalsIgnoreCase(code)) {
				return userSyncEnum;
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

	UserSyncEnum(String code, String message) {
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

	public static String getCodeByName(String name) {

		for (ServerTypeEnum type : ServerTypeEnum.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type.code;
			}
		}
		return null;
	}
}
