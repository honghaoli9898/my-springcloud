package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

import com.seaboxdata.sdps.common.crypto.annotation.ApiCrypto;
import com.seaboxdata.sdps.common.crypto.constants.CryptoType;
import com.seaboxdata.sdps.common.crypto.constants.EncodingType;

/**
 * 编码注解
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiCrypto(encryptType = CryptoType.ENCODING, decryptType = CryptoType.ENCODING)
public @interface EncodingCrypto {

    /**
     * 编码类型
     * <p>
     * 默认为配置文件配置的编码类型
     *
     * @return cn.hermesdi.crypto.constants.EncodingType 编码 类型枚举
     * @author hermes-di
     **/
    EncodingType encodingType() default EncodingType.DEFAULT;
}
