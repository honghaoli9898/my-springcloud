package com.sdps.common.config;

import org.springframework.context.annotation.Configuration;

import com.seaboxdata.sdps.common.core.config.DefaultAsycTaskConfig;

/**
 * 线程池配置、启用异步
 * @Async quartz 需要使用
 *
 * @author zlt
 */
@Configuration
public class AsycTaskExecutorConfig extends DefaultAsycTaskConfig {

}
