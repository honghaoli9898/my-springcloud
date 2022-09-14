package com.seaboxdata.sdps.job.executor.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.seaboxdata.sdps.job.core.executor.impl.XxlJobSpringExecutor;

@Configuration
public class XxlJobConfig {
	private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

	@Autowired
	private XxlJobProperties xxlJobProperties;

	@Bean
	public XxlJobSpringExecutor xxlJobExecutor() {
		logger.info(">>>>>>>>>>> xxl-job config init.");
		XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
		xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdmin()
				.getAddresses());
		xxlJobSpringExecutor.setAppname(xxlJobProperties.getExecutor()
				.getAppname());
		xxlJobSpringExecutor.setAddress(xxlJobProperties.getExecutor()
				.getAddress());
		xxlJobSpringExecutor.setIp(xxlJobProperties.getExecutor().getIp());
		xxlJobSpringExecutor.setPort(xxlJobProperties.getExecutor().getPort());
		xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
		xxlJobSpringExecutor.setLogPath(xxlJobProperties.getExecutor()
				.getLogpath());
		xxlJobSpringExecutor.setLogRetentionDays(xxlJobProperties.getExecutor()
				.getLogretentiondays());

		return xxlJobSpringExecutor;
	}

	/**
	 * 针对多网卡、容器内部署等情况，可借助 "spring-cloud-commons" 提供的 "InetUtils" 组件灵活定制注册IP；
	 *
	 * 1、引入依赖： <dependency> <groupId>org.springframework.cloud</groupId>
	 * <artifactId>spring-cloud-commons</artifactId>
	 * <version>${version}</version> </dependency>
	 *
	 * 2、配置文件，或者容器启动变量 spring.cloud.inetutils.preferred-networks: 'xxx.xxx.xxx.'
	 *
	 * 3、获取IP String ip_ =
	 * inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
	 */

}