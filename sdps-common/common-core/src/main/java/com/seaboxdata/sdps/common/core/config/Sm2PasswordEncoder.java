package com.seaboxdata.sdps.common.core.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.utils.SmUtils;

@Slf4j
public class Sm2PasswordEncoder implements PasswordEncoder {

	private String publicKeyStr;
	private String privateKeyStr;

	public Sm2PasswordEncoder(String publicKeyStr, String privateKeyStr) {
		this.privateKeyStr = privateKeyStr;
		this.publicKeyStr = publicKeyStr;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return SmUtils.encryptStr(rawPassword.toString(), publicKeyStr);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (StrUtil.isBlank(encodedPassword)) {
			return false;
		}
		try {
			return SmUtils.decryptStr(encodedPassword, privateKeyStr).equals(
					rawPassword);
		} catch (Exception e) {
			log.error("解析密码报错", e);
		}
		return false;
	}

}
