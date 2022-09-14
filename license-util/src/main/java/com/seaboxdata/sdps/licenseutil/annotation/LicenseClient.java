package com.seaboxdata.sdps.licenseutil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @LicenseClient 用于注解需要被License授权的整个服务,用于入口类中
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LicenseClient {
    /**
     * 可以设置被拦截的包
     *
     * @return
     */
    String[] packege() default {};

    /**
     * 拦截时是否执行目标方法
     * @return
     */
    boolean excunteTarget() default false;
}
