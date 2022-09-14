package com.seaboxdata.sdps.common.framework.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.seaboxdata.sdps.common.framework.converter.DateConverter;

@Configuration
public class DateConverterConfig {

	@Bean
	public DateConverter getDateConverter() {
		return new DateConverter();
	}
}
