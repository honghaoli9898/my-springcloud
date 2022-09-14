package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

/**
 * 忽略 解密/解密
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Decrypt
@Encrypt
public @interface Crypto {
}
