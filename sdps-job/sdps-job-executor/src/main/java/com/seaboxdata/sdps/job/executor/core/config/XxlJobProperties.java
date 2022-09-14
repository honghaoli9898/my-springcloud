package com.seaboxdata.sdps.job.executor.core.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {
	private XxlJobAdminProperties admin;
	private String accessToken;
	private XxlJobExecutorProperties executor;
}
