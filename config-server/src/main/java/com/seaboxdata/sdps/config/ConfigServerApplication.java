package com.seaboxdata.sdps.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(ConfigServerApplication.class);
	
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        logger.info("====ConfigServerApplication====配置中心启动成功");
    }
}
