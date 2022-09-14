package com.seaboxdata.sdps.licenseutil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截方法专用(前端请求的接口)
 * @author 赵志华
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LicenseClientOfMethod {
    /**
     * 拦截时是否执行目标方法,true执行,false不执行,默认false
     *
     * @return
     */
    boolean excunteTarget() default false;
}
