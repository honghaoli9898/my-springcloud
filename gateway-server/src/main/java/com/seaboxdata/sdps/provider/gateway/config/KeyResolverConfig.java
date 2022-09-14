package com.seaboxdata.sdps.provider.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.seaboxdata.sdps.provider.gateway.utils.ReactiveAddrUtil;

import reactor.core.publisher.Mono;

@Configuration
public class KeyResolverConfig {

	@Bean
	public KeyResolver pathKeyResolver() {
		// 写法1
		return exchange -> Mono
				.just(exchange.getRequest().getPath().toString());

	}

	/**
	 * 根据请求IP限流
	 * 
	 * @return
	 */
	@Bean
	@Primary
	public KeyResolver ipKeyResolver() {
		return exchange -> Mono.just(ReactiveAddrUtil.getRemoteAddr(exchange.getRequest()));
	}

	/**
	 * 根据请求参数中的userId进行限流
	 * 
	 * 请求地址写法：http://localhost:8801/rate/123?userId=lisi
	 * 
	 * @return
	 */
	@Bean
	public KeyResolver userKeyResolver() {
		return exchange -> Mono.just(exchange.getRequest().getQueryParams()
				.getFirst("userId"));
	}
}
