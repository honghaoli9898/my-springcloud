package com.seaboxdata.sdps.bigdataProxy.config;

import org.springframework.context.annotation.Configuration;

import com.seaboxdata.sdps.common.core.config.DefaultAsycTaskConfig;

/**
 * @author zlt
 * 线程池配置、启用异步
 * @Async quartz 需要使用
 */
@Configuration
public class AsycTaskExecutorConfig extends DefaultAsycTaskConfig {

}
