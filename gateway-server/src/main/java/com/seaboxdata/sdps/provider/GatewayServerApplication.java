package com.seaboxdata.sdps.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;

@EnableEurekaClient
@SpringBootApplication
@EnableHystrix
@EnableFeignClients
@EnableDiscoveryClient
@EnableBaseFeignInterceptor
@EnableScheduling
public class GatewayServerApplication {
	private static final Logger logger = LoggerFactory
			.getLogger(GatewayServerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GatewayServerApplication.class, args);
		logger.info("====GatewayServerApplication====网关启动成功");
	}

}