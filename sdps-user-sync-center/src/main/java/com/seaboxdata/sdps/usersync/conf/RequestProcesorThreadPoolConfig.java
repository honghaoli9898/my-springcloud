package com.seaboxdata.sdps.usersync.conf;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.seaboxdata.sdps.common.core.config.ContextDecorator;
import com.seaboxdata.sdps.common.core.utils.CustomThreadPoolTaskExecutor;
import com.seaboxdata.sdps.usersync.properties.RequestProcessorThreadPoolPoperties;
import com.seaboxdata.sdps.usersync.thread.RequestProcessorThreadPool;

@Configuration
public class RequestProcesorThreadPoolConfig {
	@Autowired
	private RequestProcessorThreadPoolPoperties requestProcessorThreadPoolPoperties;

	@Bean
	public RequestProcessorThreadPool requestProcessorThreadPool() {
		ThreadPoolTaskExecutor executor = new CustomThreadPoolTaskExecutor();
		executor.setCorePoolSize(requestProcessorThreadPoolPoperties
				.getCorePoolSize());
		executor.setMaxPoolSize(requestProcessorThreadPoolPoperties
				.getMaxPoolSize());
		executor.setQueueCapacity(requestProcessorThreadPoolPoperties
				.getQueueCapacity());
		executor.setThreadNamePrefix(requestProcessorThreadPoolPoperties
				.getThreadNamePrefix());
		executor.setTaskDecorator(new ContextDecorator());
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		RequestProcessorThreadPool requestProcessorThreadPool = new RequestProcessorThreadPool(
				executor, requestProcessorThreadPoolPoperties);
		return requestProcessorThreadPool;
	}

}
