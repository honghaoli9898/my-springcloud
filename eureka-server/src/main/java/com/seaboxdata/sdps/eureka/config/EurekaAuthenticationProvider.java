package com.seaboxdata.sdps.eureka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.seaboxdata.sdps.eureka.constants.SystemConstants;
import com.seaboxdata.sdps.eureka.secret.RsaUtils;

import cn.hutool.core.util.StrUtil;

@Configuration
public class EurekaAuthenticationProvider implements AuthenticationProvider {
	@Autowired
	private UserDetailsService userDetailsService;
	//@Value("${secret.public.key}")
	private String securetKey;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		UserDetails userDetails = userDetailsService
				.loadUserByUsername(username);
		if (userDetails == null) {
			throw new BadCredentialsException("用户名不存在");
		} else {
			if (StrUtil.isBlank(password)) {
				throw new BadCredentialsException("用户名或密码错误");
			}
			if (!SystemConstants.PASSWORD.equals(password)) {
				password = password.replaceAll("6", "/").replaceAll("\\.", "=");
				password = RsaUtils.decryptPublic(password, securetKey);
			}
			if (!SystemConstants.PASSWORD.equals(password)) {
				throw new BadCredentialsException("用户名或密码错误");
			} else {
				UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
						userDetails, userDetails.getPassword(),
						userDetails.getAuthorities());
				result.setDetails(userDetails);
				return result;
			}
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
