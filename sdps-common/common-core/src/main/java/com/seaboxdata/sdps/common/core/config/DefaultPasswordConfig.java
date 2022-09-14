package com.seaboxdata.sdps.common.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.seaboxdata.sdps.common.core.utils.PwdEncoderUtil;

/**
 * 密码工具类
 */
//@Import(GlobalParameterProperties.class)
public class DefaultPasswordConfig {
//	@Autowired
//	private GlobalParameterProperties globalParameterProperties;

	/**
	 * 装配BCryptPasswordEncoder用户密码的匹配
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public PasswordEncoder passwordEncoder() {
		return PwdEncoderUtil.getDelegatingPasswordEncoder("bcrypt");
	}

//	@Bean
//	@ConditionalOnProperty(prefix = "global.parameter", name = "encoder", havingValue = "SM2")
//	public PasswordEncoder passwordEncoderSm2() throws Exception {
//		Assert.isTrue(
//				StrUtil.isNotBlank(globalParameterProperties.getPublicKey())
//						|| StrUtil.isNotBlank(globalParameterProperties
//								.getPrivateKey()),
//				"you select sm2 password encoder,but privateKey or public is null");
//		return new Sm2PasswordEncoder(globalParameterProperties.getPublicKey(),
//				globalParameterProperties.getPrivateKey());
//	}
}
