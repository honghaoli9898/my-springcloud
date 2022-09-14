package com.seaboxdata.sdps.common.framework.enums;

import java.util.List;

import cn.hutool.core.collection.CollUtil;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum UserSyncStatusEnum implements EnumBase {
	NOT("not", "未同步"), SYNCING("syncing", "同步中"), SYNC_ERROR("error", "同步异常"), SYNC_SUCCESS(
			"success", "同步成功");

	public static List<String> getAllCode() {
		List<String> codes = CollUtil.newArrayList();
		for (UserSyncStatusEnum userSyncEnum : UserSyncStatusEnum.values()) {
			codes.add(userSyncEnum.code);
		}
		return codes;
	}

	public static UserSyncStatusEnum getEnumByCode(String code) {
		for (UserSyncStatusEnum userSyncEnum : UserSyncStatusEnum.values()) {
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

	UserSyncStatusEnum(String code, String message) {
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
