package com.seaboxdata.sdps.usersync.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sdps.usersync.thread")
public class RequestProcessorThreadPoolPoperties {
	/**
	 * 线程池维护线程的最小数量.
	 */
	private Integer corePoolSize;
	/**
	 * 线程池维护线程的最大数量
	 */
	private Integer maxPoolSize;
	/**
	 * 队列最大长度
	 */
	private Integer queueCapacity;
	/**
	 * 线程池前缀
	 */
	private String threadNamePrefix;

	/**
	 * 阻塞队列大小
	 */
	private Integer blockingQueueSize;
	
	private Integer queueNumber;

}
