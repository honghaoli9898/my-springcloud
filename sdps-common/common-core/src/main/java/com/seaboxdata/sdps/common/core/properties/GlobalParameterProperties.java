package com.seaboxdata.sdps.common.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "global.parameter")
public class GlobalParameterProperties {
	private String privateKey;
	private String publicKey;
	private String encoder;
}
