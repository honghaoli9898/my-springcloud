package com.seaboxdata.sdps.bigdataProxy;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@Slf4j
@EnableEurekaClient
@SpringBootApplication
@MapperScan({ "com.seaboxdata.sdps.bigdataProxy.mapper" })
@EnableFeignClients
@EnableCaching
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
public class SdpsBigdataCommonProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdpsBigdataCommonProxyApplication.class, args);
		log.info("====SdpsBigdataCommonProxyApplication====大数据公共代理服务启动成功");
	}
}