package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

import com.seaboxdata.sdps.common.crypto.constants.CryptoType;

/**
 * 加密、解密 标记注解
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface ApiCrypto {

    /**
     * 加密 类型（枚举）
     *
     * @return cn.hermesdi.crypto.constants.CryptoType 加密解密 类型枚举（用于描述）
     * @author hermes-di
     **/
    CryptoType encryptType();


    /**
     * 解密 类型（枚举）
     *
     * @return cn.hermesdi.crypto.constants.CryptoType 加密解密 类型枚举（用于描述）
     * @author hermes-di
     **/
    CryptoType decryptType();
}
