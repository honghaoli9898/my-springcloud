package com.seaboxdata.sdps.common.crypto.annotation;

import java.lang.annotation.*;

/**
 * 忽略 解密 注解
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {
}
