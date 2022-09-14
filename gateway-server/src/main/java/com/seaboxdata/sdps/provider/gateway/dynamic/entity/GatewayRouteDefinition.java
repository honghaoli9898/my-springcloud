package com.seaboxdata.sdps.provider.gateway.dynamic.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 路由模型 zhuyu 2019-01-17
 */
@Getter
@Setter
@ToString
public class GatewayRouteDefinition {

	// 路由的Id
	private String id;
	// 路由断言集合配置
	private List<GatewayPredicateDefinition> predicates = new ArrayList<>();
	// 路由过滤器集合配置
	private List<GatewayFilterDefinition> filters = new ArrayList<>();
	// 路由规则转发的目标uri
	private String uri;
	// 路由执行的顺序
	private int order = 0;

}