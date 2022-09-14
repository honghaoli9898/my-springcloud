package com.seabox.sdps.dynamicroute;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@Slf4j
@EnableDiscoveryClient
@MapperScan({ "com.seabox.sdps.dynamicroute.respository" })
@SpringBootApplication
@EnableFeignClients
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
public class DynamicRouteApplication {

	public static void main(String[] args) {
		SpringApplication.run(DynamicRouteApplication.class, args);
		log.info("====DynamicRouteApplication====大数据动态代理服务启动成功");
	}

}
