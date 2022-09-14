package com.seaboxdata.sdps.common.framework.bean.ambari;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

public enum AmbariRoleEnum implements EnumBase {
	ADMINISTRATOR("CLUSTER.ADMINISTRATOR", "集群超级管理员"), OPERATOR(
			"CLUSTER.OPERATOR", "集群管理员"), USER("CLUSTER.USER", "集群人员"), SERVICEADMINISTRATOR(
			"SERVICE.ADMINISTRATOR", "服务超级管理员"),SERVICEOPERATOR("SERVICE.OPERATOR","服务管理员");
	/**
	 * 异常码
	 */
	private String code;
	/**
	 * 异常描述
	 */
	private String message;

	/**
     *
     */

	AmbariRoleEnum(String code, String message) {
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
