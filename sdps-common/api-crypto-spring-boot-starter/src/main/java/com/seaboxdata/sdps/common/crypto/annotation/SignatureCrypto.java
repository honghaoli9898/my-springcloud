package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

import com.seaboxdata.sdps.common.crypto.annotation.ApiCrypto;
import com.seaboxdata.sdps.common.crypto.constants.CryptoType;

/**
 * 签名注解
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiCrypto(encryptType = CryptoType.SIGNATURE, decryptType = CryptoType.SIGNATURE)
public @interface SignatureCrypto {


    /**
     * 自定义超时时间 （优先）
     * <p>
     * 小于等于 "0" 不限制
     *
     * @return long
     * @author hermes-di
     **/
    long timeout() default 0L;

    /**
     * 自定义签名 秘钥（优先）
     *
     * @return java.lang.String 字符串
     * @author hermes-di
     **/
    String SecretKey() default "";
}
