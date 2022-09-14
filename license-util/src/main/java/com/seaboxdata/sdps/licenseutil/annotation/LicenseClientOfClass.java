package com.seaboxdata.sdps.licenseutil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截类专用
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LicenseClientOfClass {

    /**
     * 拦截时是否执行目标方法,true执行,false不执行,默认false
     * @return
     */
    boolean excunteTarget() default false;

}
