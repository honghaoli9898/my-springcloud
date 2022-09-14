package com.seaboxdata.sdps.provider.gateway.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {
	private static final String ALL = "*";

	private static final String MAX_AGE = "3600";

	// @Order(Ordered.HIGHEST_PRECEDENCE)
	// @Bean
	// public CorsWebFilter corsFilter() {
	// CorsConfiguration config = new CorsConfiguration();
	// // cookie跨域
	// config.setAllowCredentials(Boolean.TRUE);
	// config.addAllowedMethod(ALL);
	// config.addAllowedOrigin(ALL);
	// config.addAllowedHeader(ALL);
	// // 配置前端js允许访问的自定义响应头
	// config.addExposedHeader("setToken");
	//
	// UrlBasedCorsConfigurationSource source = new
	// UrlBasedCorsConfigurationSource(
	// new PathPatternParser());
	// source.registerCorsConfiguration("/**", config);
	//
	// return new CorsWebFilter(source);
	// }
	// @Autowired
	// private SecurityProperties securityProperties;

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public WebFilter corsFilter() {
		return (ServerWebExchange ctx, WebFilterChain chain) -> {
			ServerHttpRequest request = ctx.getRequest();
			if (!CorsUtils.isCorsRequest(request)
					|| Objects.equals(request.getMethodValue(),
							HttpMethod.OPTIONS)) {
				return chain.filter(ctx);
			}
			HttpHeaders requestHeaders = request.getHeaders();
			ServerHttpResponse response = ctx.getResponse();
			HttpMethod requestMethod = requestHeaders
					.getAccessControlRequestMethod();
			HttpHeaders headers = response.getHeaders();

			// if (!CollUtil.contains(securityProperties.getCores().getAllows(),
			// requestHeaders.getOrigin())) {
			// ServerWebExchangeUtils.setResponseStatus(ctx, HttpStatus.OK);
			// ServerHttpResponse httpResponse = ctx.getResponse();
			// httpResponse.setStatusCode(HttpStatus.OK);
			// Result resData = Result.failed("非法跨域请求");
			// DataBuffer buffer = httpResponse.bufferFactory().wrap(
			// JSONObject.toJSONBytes(resData));
			// return httpResponse.writeWith(Mono.just(buffer));
			// }

			if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
						requestHeaders.getOrigin());
			}
			if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)) {
				headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
						requestHeaders.getAccessControlRequestHeaders());
			}
			if (requestMethod != null
					&& !headers
							.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)) {
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
						requestMethod.name());
			}
			if (!headers
					.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)) {
				headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
						"true");
			}
			if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)) {
				headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
			}
			if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_MAX_AGE)) {
				headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
			}
			if (request.getMethod() == HttpMethod.OPTIONS) {
				response.setStatusCode(HttpStatus.OK);
				return Mono.empty();
			}
			return chain.filter(ctx);
		};

	}
}
