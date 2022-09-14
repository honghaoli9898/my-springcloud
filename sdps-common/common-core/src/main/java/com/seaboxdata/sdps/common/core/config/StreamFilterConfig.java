package com.seaboxdata.sdps.common.core.config;

import javax.servlet.Filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(Filter.class)
public class StreamFilterConfig {
	@Bean
	public FilterRegistrationBean somFilterRegistration() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean<Filter>();
		filterRegistrationBean.setFilter(new ReplaceStreamFilter());
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setName("streamFilter");
		return filterRegistrationBean;
	}

}
