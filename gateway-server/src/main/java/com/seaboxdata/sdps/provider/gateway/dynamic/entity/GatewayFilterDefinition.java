package com.seaboxdata.sdps.provider.gateway.dynamic.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 过滤器模型 zhuyu 2019-01-17
 */
@Getter
@Setter
@ToString
public class GatewayFilterDefinition {

	// Filter Name
	private String name;
	// 对应的路由规则
	private Map<String, String> args = new LinkedHashMap<>();

}
