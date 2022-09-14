package com.seaboxdata.sdps.common.oauth2.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author zlt
 * @date 2019/1/4
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "sdps.security")
@RefreshScope
public class SecurityProperties {
	private AuthProperties auth = new AuthProperties();
	
	private CasProperties cas = new CasProperties();
	
	private PermitProperties ignore = new PermitProperties();

	private ValidateCodeProperties code = new ValidateCodeProperties();

	private IpWhiteProperties white = new IpWhiteProperties();

	private CorsAllowProperties cores = new CorsAllowProperties();

}
