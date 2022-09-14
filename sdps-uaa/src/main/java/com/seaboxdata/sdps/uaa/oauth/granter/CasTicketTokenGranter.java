package com.seaboxdata.sdps.uaa.oauth.granter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.seaboxdata.sdps.common.oauth2.service.impl.CasServiceAuthenticationDetails;

public class CasTicketTokenGranter extends AbstractTokenGranter {

	private static final String GRANT_TYPE = "cas_ticket";

	private final AuthenticationManager authenticationManager;

	public CasTicketTokenGranter(AuthenticationManager authenticationManager,
			AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory) {
		this(authenticationManager, tokenServices, clientDetailsService,
				requestFactory, GRANT_TYPE);
	}

	protected CasTicketTokenGranter(
			AuthenticationManager authenticationManager,
			AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory, String grantType) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		this.authenticationManager = authenticationManager;
	}

	// @Override
	// public OAuth2AccessToken grant(String grantType, TokenRequest
	// tokenRequest) {
	// OAuth2AccessToken token = super.grant(grantType, tokenRequest);
	// if (token != null) {
	// DefaultOAuth2AccessToken norefresh = new DefaultOAuth2AccessToken(
	// token);
	// norefresh.setRefreshToken(null);
	// token = norefresh;
	// }
	// return token;
	// }

	@Override
	protected OAuth2Authentication getOAuth2Authentication(
			ClientDetails client, TokenRequest tokenRequest) {

		Map<String, String> parameters = new LinkedHashMap<String, String>(
				tokenRequest.getRequestParameters());
		String username = CasAuthenticationFilter.CAS_STATEFUL_IDENTIFIER;
		String password = parameters.get("ticket");
		String serverUrl = parameters.get("server");

		if (password == null) {
			throw new InvalidRequestException("A cas ticket must be supplied.");
		}
		Authentication userAuth = new UsernamePasswordAuthenticationToken(
				username, password);
		((AbstractAuthenticationToken) userAuth)
				.setDetails(new CasServiceAuthenticationDetails(serverUrl));
		try {
			userAuth = authenticationManager.authenticate(userAuth);
		} catch (AccountStatusException ase) {
			// covers expired, locked, disabled cases (mentioned in section 5.2,
			// draft 31)
			throw new InvalidGrantException(ase.getMessage());
		} catch (BadCredentialsException e) {
			// If the ticket is wrong the spec says we should send 400/invalid
			// grant
			throw new InvalidGrantException(e.getMessage());
		}
		if (userAuth == null || !userAuth.isAuthenticated()) {
			throw new InvalidGrantException("Could not authenticate ticket: "
					+ password);
		}

		OAuth2Request storedOAuth2Request = getRequestFactory()
				.createOAuth2Request(client, tokenRequest);
		return new OAuth2Authentication(storedOAuth2Request, userAuth);
	}
}
