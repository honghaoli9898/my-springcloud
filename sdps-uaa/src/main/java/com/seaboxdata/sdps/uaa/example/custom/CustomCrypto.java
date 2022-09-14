package com.seaboxdata.sdps.uaa.example.custom;

import java.lang.annotation.*;

/**
 * 自定义加密解密注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomCrypto {
    /**
     * 是否请求体解密
     */
    boolean isDecryption();

    /**
     * 是否响应体加密
     */
    boolean isEncryption();

    /**
     * 加密的盐
     */
    String salt();
}
