package com.seaboxdata.sdps.common.crypto.ov;

import java.lang.annotation.Annotation;

import com.seaboxdata.sdps.common.crypto.bean.ApiCryptoBody;

/**
 * 自定义加密数据响应格式接口，实现该接口重写 responseBody 方法自定义返回体
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@FunctionalInterface
public interface IApiResponseBody {

    /**
     * 自定义加密数据响应体格式
     *
     * @param annotation: 执行注解
     * @param cryptoBody: 响应数据
     * @return java.lang.Object 响应体
     * @author hermes-di
     */
    Object responseBody(Annotation annotation, ApiCryptoBody cryptoBody);
}
