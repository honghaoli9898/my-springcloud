package com.seaboxdata.sdps.provider.gateway.config;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.oauth2.properties.SecurityProperties;
import com.seaboxdata.sdps.provider.gateway.auth.CustomAuthenticationManager;
import com.seaboxdata.sdps.provider.gateway.auth.CustomServerWebExchangeMatchers;
import com.seaboxdata.sdps.provider.gateway.auth.JsonAccessDeniedHandler;
import com.seaboxdata.sdps.provider.gateway.auth.JsonAuthenticationEntryPoint;
import com.seaboxdata.sdps.provider.gateway.auth.Oauth2AuthSuccessHandler;
import com.seaboxdata.sdps.provider.gateway.auth.PermissionAuthManager;
import com.seaboxdata.sdps.provider.gateway.utils.ReactiveAddrUtil;

/**
 * 资源服务器配置
 */
@Configuration
public class ResourceServerConfiguration {
	@Autowired
	private SecurityProperties securityProperties;

	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private PermissionAuthManager permissionAuthManager;

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		// 认证处理器
		ReactiveAuthenticationManager customAuthenticationManager = new CustomAuthenticationManager(
				tokenStore);
		JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
		// token转换器
		ServerBearerTokenAuthenticationConverter tokenAuthenticationConverter = new ServerBearerTokenAuthenticationConverter();
		tokenAuthenticationConverter.setAllowUriQueryParameter(true);
		// oauth2认证过滤器
		AuthenticationWebFilter oauth2Filter = new AuthenticationWebFilter(
				customAuthenticationManager);
		oauth2Filter
				.setServerAuthenticationConverter(tokenAuthenticationConverter);
		oauth2Filter
				.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
						entryPoint));
		oauth2Filter
				.setRequiresAuthenticationMatcher(new CustomServerWebExchangeMatchers(
						securityProperties));
		oauth2Filter
				.setAuthenticationSuccessHandler(new Oauth2AuthSuccessHandler());
		http.addFilterAt(oauth2Filter, SecurityWebFiltersOrder.AUTHENTICATION);

		ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = http
				.authorizeExchange();
		if (securityProperties.getAuth().getHttpUrls().length > 0) {
			authorizeExchange.pathMatchers(
					securityProperties.getAuth().getHttpUrls()).authenticated();
		}
		if (securityProperties.getIgnore().getUrls().length > 0) {
			authorizeExchange.pathMatchers(
					securityProperties.getIgnore().getUrls()).permitAll();
		}
		if (securityProperties.getWhite().getResource().size() > 0) {
			Map<String, List<String>> map = securityProperties.getWhite()
					.getResource();
			map.entrySet()
					.stream()
					.forEach(
							data -> {
								authorizeExchange
										.pathMatchers(
												Convert.toStrArray(data
														.getValue()))
										.access((authentication, context) -> whiteListIp(
												authentication, context,
												data.getKey()));
							});
		}
		authorizeExchange.pathMatchers(HttpMethod.OPTIONS).permitAll()
				.anyExchange().access(permissionAuthManager).and()
				.exceptionHandling()
				.accessDeniedHandler(new JsonAccessDeniedHandler())
				.authenticationEntryPoint(entryPoint).and().headers()
				.frameOptions().disable().and().httpBasic().disable().csrf()
				.disable();
		return http.build();
	}

	private Mono<AuthorizationDecision> whiteListIp(
			Mono<Authentication> authentication, AuthorizationContext context,
			String address) {
		List<String> whiteIpList = StrUtil.split(address, '-');
		String ip = ReactiveAddrUtil.getRemoteAddr(context.getExchange()
				.getRequest());
		if (whiteIpList.contains(ip)) {
			MultiValueMap<String, String> headerValues = new LinkedMultiValueMap(
					4);
			ServerHttpRequest serverHttpRequest = context.getExchange()
					.getRequest().mutate().headers(h -> {
						h.addAll(headerValues);
					}).build();
			ServerWebExchange build = context.getExchange().mutate()
					.request(serverHttpRequest).build();
		}

		return authentication.map(
				(a) -> new AuthorizationDecision(a.isAuthenticated()))
				.defaultIfEmpty(
						new AuthorizationDecision(
								(whiteIpList.contains(ip) ? true : false)));
	}
}
