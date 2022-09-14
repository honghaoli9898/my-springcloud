package com.seaboxdata.sdps.user;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.seaboxdata.sdps.common.ribbon.annotation.EnableBaseFeignInterceptor;
import com.seaboxdata.sdps.common.ribbon.annotation.EnableFeignInterceptor;

@Slf4j
@SpringBootApplication
@MapperScan({ "com.seaboxdata.sdps.user.mybatis.mapper"})
@EnableEurekaClient
@EnableFeignClients(basePackages={"com.seaboxdata.sdps.common.core.feign","com.seaboxdata.sdps.user.feign"})
@EnableTransactionManagement
@EnableBaseFeignInterceptor
@EnableFeignInterceptor
@EnableScheduling
public class UserCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
        log.info("=============user-center服务启动成功===========");
    }
}
