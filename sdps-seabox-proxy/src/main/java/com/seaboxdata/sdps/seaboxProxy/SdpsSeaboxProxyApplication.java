package com.seaboxdata.sdps.seaboxProxy;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@Slf4j
@EnableScheduling
@EnableEurekaClient
@SpringBootApplication
@EnableFeignClients
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
@MapperScan({ "com.seaboxdata.sdps.seaboxProxy.mapper" })
public class SdpsSeaboxProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(SdpsSeaboxProxyApplication.class, args);
		log.info("====SdpsSeaboxProxyApplication====海盒大数据代理服务启动成功");
	}

}