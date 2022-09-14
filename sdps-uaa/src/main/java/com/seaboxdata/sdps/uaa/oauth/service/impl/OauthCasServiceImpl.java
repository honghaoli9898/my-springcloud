package com.seaboxdata.sdps.uaa.oauth.service.impl;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.feign.UserService;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;

@Service("OauthCasService")
@Slf4j
public class OauthCasServiceImpl implements
		AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	@Resource
	private UserService userService;

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token)
			throws UsernameNotFoundException {
		String name = token.getName();
		log.info("获得的用户名：" + name);
		LoginAppUser loginAppUser = userService.findByUsername(name);
		if (loginAppUser == null) {
			throw new InternalAuthenticationServiceException("用户名或密码错误");
		}
		return checkUser(loginAppUser);
	}

	private LoginAppUser checkUser(LoginAppUser loginAppUser) {
		if (loginAppUser != null && !loginAppUser.isEnabled()) {
			throw new DisabledException("用户已作废");
		}
		return loginAppUser;
	}
}
