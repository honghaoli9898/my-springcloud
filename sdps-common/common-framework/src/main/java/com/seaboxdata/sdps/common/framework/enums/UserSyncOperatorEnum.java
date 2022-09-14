package com.seaboxdata.sdps.common.framework.enums;

import java.util.List;

import cn.hutool.core.collection.CollUtil;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum UserSyncOperatorEnum implements EnumBase {
	ADD("add", "增加用户"), UPDATE("update", "更新用户"), DELETE("delete", "删除用户");

	public static List<String> getAllCode() {
		List<String> codes = CollUtil.newArrayList();
		for (UserSyncOperatorEnum userSyncEnum : UserSyncOperatorEnum.values()) {
			codes.add(userSyncEnum.code);
		}
		return codes;
	}

	public static UserSyncOperatorEnum getEnumByCode(String code) {
		for (UserSyncOperatorEnum userSyncEnum : UserSyncOperatorEnum.values()) {
			if (userSyncEnum.getCode().equalsIgnoreCase(code)) {
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

	UserSyncOperatorEnum(String code, String message) {
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
