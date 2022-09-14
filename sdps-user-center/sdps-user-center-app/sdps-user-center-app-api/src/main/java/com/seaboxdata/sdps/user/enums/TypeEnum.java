package com.seaboxdata.sdps.user.enums;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum TypeEnum implements EnumBase {
	S("S", "系统"), O("O", "项目"), T("T", "租户"), external("external", "外部"), internal(
			"internal", "内部"), G("G", "用户组类型"), M("M", "成员类型"), SEABOX(
			"seabox", "海盒大数据");
	private String code;
	private String message;

	TypeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public Object getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
