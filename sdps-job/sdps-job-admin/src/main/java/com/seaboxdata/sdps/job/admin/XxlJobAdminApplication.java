package com.seaboxdata.sdps.job.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
public class XxlJobAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(XxlJobAdminApplication.class, args);
	}

}