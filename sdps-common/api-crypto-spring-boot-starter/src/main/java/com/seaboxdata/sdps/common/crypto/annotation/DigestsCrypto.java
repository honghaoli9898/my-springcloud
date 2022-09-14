package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

import com.seaboxdata.sdps.common.crypto.annotation.ApiCrypto;
import com.seaboxdata.sdps.common.crypto.constants.CryptoType;
import com.seaboxdata.sdps.common.crypto.constants.DigestsType;
import com.seaboxdata.sdps.common.crypto.constants.EncodingType;

/**
 * 摘要算法注解（MD、SHA）
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiCrypto(encryptType = CryptoType.DIGEST, decryptType = CryptoType.DIGEST)
public @interface DigestsCrypto {

    /**
     * 摘要加密类型
     *
     * @return cn.hermesdi.crypto.constants.DigestsType 摘要加密 类型枚举
     * @author hermes-di
     **/
    DigestsType type() default DigestsType.MD5;

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
