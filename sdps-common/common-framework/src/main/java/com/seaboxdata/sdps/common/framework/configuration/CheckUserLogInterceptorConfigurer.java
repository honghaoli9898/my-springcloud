package com.seaboxdata.sdps.common.framework.configuration;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CheckUserLogInterceptorConfigurer implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new CheckUserLogInterceptor()).addPathPatterns(
				"/**");
	}
}
