package com.seaboxdata.sdps;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages={"com.seaboxdata.sdps.common.core.feign"})

@EnableTransactionManagement
@EnableBaseFeignInterceptor
@MapperScan({ "com.seaboxdata.sdps.sshweb.mapper" })
@EnableFeignInterceptor
public class SshWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(SshWebApplication.class, args);
	}

}
