package com.seaboxdata.sdps.uaa;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.seaboxdata.sdps.common.crypto.annotation.EnableApiCrypto;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@Slf4j
@EnableFeignClients(basePackages={"com.seaboxdata.sdps.common.core.feign"})
@EnableFeignInterceptor
@EnableDiscoveryClient
@EnableRedisHttpSession
@SpringBootApplication
@EnableApiCrypto
public class UaaServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(UaaServerApplication.class, args);
		log.info("=========uaa server启动成功==============");
	}

	@Bean
	public HttpFirewall allowUrlSemicolonHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowSemicolon(true);
		return firewall;
	}
}
