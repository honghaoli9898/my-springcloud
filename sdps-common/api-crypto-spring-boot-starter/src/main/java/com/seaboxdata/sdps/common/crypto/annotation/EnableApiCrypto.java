package com.seaboxdata.sdps.common.crypto.annotation;

import org.springframework.context.annotation.Import;

import com.seaboxdata.sdps.common.crypto.advice.DecryptRequestBodyAdvice;
import com.seaboxdata.sdps.common.crypto.advice.EncryptResponseBodyAdvice;
import com.seaboxdata.sdps.common.crypto.config.ApiCryptoConfig;
import com.seaboxdata.sdps.common.crypto.config.ApiCryptoConfiguration;

import java.lang.annotation.*;

/**
 * ApiCrypto 自动装配注解
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ EncryptResponseBodyAdvice.class, DecryptRequestBodyAdvice.class,
		ApiCryptoConfig.class, ApiCryptoConfiguration.class })
public @interface EnableApiCrypto {
}
