package com.seaboxdata.sdps.job.executor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@SpringBootApplication
@MapperScan({ "com.seaboxdata.sdps.job.executor.mybatis.mapper" })
@EnableEurekaClient
@EnableFeignClients(basePackages = { "com.seaboxdata.sdps.common.core.feign",
		"com.seaboxdata.sdps.job.executor.feign" })
@EnableTransactionManagement
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
public class SdpsJobExecutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdpsJobExecutorApplication.class, args);
	}

}