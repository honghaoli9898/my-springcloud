package com.seaboxdata.sdps.common.log.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.seaboxdata.sdps.common.log.properties.AuditLogProperties;
import com.seaboxdata.sdps.common.log.properties.LogDbProperties;
import com.seaboxdata.sdps.common.log.properties.TraceProperties;
import com.zaxxer.hikari.HikariConfig;

/**
 * 日志自动配置
 *
 */
@EnableConfigurationProperties({ TraceProperties.class,
		AuditLogProperties.class })
public class LogAutoConfigure {
	/**
	 * 日志数据库配置
	 */
	@Configuration
	@ConditionalOnClass(HikariConfig.class)
	@EnableConfigurationProperties(LogDbProperties.class)
	public static class LogDbAutoConfigure {
	}
}
