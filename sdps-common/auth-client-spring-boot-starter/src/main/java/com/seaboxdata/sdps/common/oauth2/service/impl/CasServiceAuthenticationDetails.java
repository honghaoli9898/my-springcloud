package com.seaboxdata.sdps.common.oauth2.service.impl;

import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;

public class CasServiceAuthenticationDetails implements
		ServiceAuthenticationDetails {

	private static final long serialVersionUID = 1L;
	private String serverUrl;

	public CasServiceAuthenticationDetails(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public String getServiceUrl() {
		return serverUrl;
	}

}
