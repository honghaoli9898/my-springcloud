package com.seaboxdata.sdps.provider.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.oauth2.properties.SecurityProperties;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;

import java.util.Date;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * @author :pengsong license过滤器
 */
@Component
@Slf4j
public class LicenseFilter implements GlobalFilter, Ordered {

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private SecurityProperties securityProperties;

	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange,
			GatewayFilterChain chain) {
		RequestPath requestPath = exchange.getRequest().getPath();
		String uri = requestPath.toString();
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		for (String path : securityProperties.getAuth().getLicenseUrls()) {
			if (antPathMatcher.match(path, uri)) {
				return chain.filter(exchange);
			}
		}

		// 白名单
		// if (uri.contains("/license-client/client/getServiceDetails") ||
		// uri.contains("users/current")
		// || uri.contains("/api-uaa/captcha/get") ||
		// uri.contains("/api-uaa/captcha/check") ||
		// uri.contains("/api-uaa/oauth/token")
		// || uri.contains("/license-client/client/licenseUpload") ||
		// uri.contains("/api-user/menus/current") ||
		// uri.contains("/api-user/UC30/UC3001")
		// || uri.contains("api-proxy/devops/warning/count") ||
		// uri.contains("/api-user/UC30/UC3018") ||
		// uri.contains("menus/findAlls") ||
		// uri.contains("menus/menus/saveOrUpdate") ||
		// uri.contains("api-uaa/oauth/remove/token")) {
		//
		// }
		log.info("expire:" + expireDate);
		if (Objects.nonNull(expireDate)) {
			if (expireDate.compareTo(new Date()) > 0) {
				return chain.filter(exchange);
			} else {
				JSONObject message = new JSONObject();
				message.put("code", 700301);
				message.put("msg", "海盒license已过期，请联系管理员续约license");

				return RewriteRequetFilter.errorHandle(message, exchange);
			}
		} else {
			JSONObject message = new JSONObject();
			message.put("code", 700302);
			message.put("msg", "未安装海盒license，请联系管理安装海盒license");
			return RewriteRequetFilter.errorHandle(message, exchange);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}