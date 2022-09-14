package com.seaboxdata.sdps.uaa.oauth.cas;

import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

import com.seaboxdata.sdps.common.oauth2.properties.SecurityProperties;
import com.seaboxdata.sdps.uaa.oauth.service.impl.OauthCasServiceImpl;

/**
 * mobile的相关处理配置
 *
 * @author zlt
 *         <p>
 *         Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
@Component
public class CasAuthenticationSecurityConfig extends
		SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private SecurityProperties securityProperties;

	@Autowired
	private OauthCasServiceImpl oauthCasService;

	@Override
	public void configure(HttpSecurity http) {
		CasAuthenticationProvider authenticationProvider = new CasAuthenticationProvider();
		authenticationProvider.setKey("casProvider");
		authenticationProvider
				.setAuthenticationUserDetailsService(oauthCasService);
		Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(
				securityProperties.getCas().getServerUrl());
		ticketValidator.setAcceptAnyProxy(true);
		ticketValidator
				.setProxyGrantingTicketStorage(new ProxyGrantingTicketStorageImpl());
		authenticationProvider.setTicketValidator(ticketValidator);
		http.authenticationProvider(authenticationProvider);
	}
}
