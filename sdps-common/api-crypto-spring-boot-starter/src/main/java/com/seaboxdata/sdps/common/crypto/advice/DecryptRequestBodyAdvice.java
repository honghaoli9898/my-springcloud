package com.seaboxdata.sdps.common.crypto.advice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import com.seaboxdata.sdps.common.crypto.algorithm.ApiCryptoAlgorithm;
import com.seaboxdata.sdps.common.crypto.annotation.Decrypt;

/**
 * 请求解密类
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
@ControllerAdvice
@ConditionalOnClass({ HttpServletRequest.class, RequestContextHolder.class })
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private static final Log logger = LogFactory.getLog(DecryptRequestBodyAdvice.class);

    private ApiCryptoAlgorithm apiCryptoAlgorithm;

    private final List<ApiCryptoAlgorithm> apiCryptoAlgorithms;

    public DecryptRequestBodyAdvice(List<ApiCryptoAlgorithm> apiCryptoAlgorithms) {
        this.apiCryptoAlgorithms = apiCryptoAlgorithms;
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 方法上有排除解密注解
        if (!parameter.hasMethodAnnotation(Decrypt.class)) {
            return false;
        }

        if (Objects.nonNull(apiCryptoAlgorithms) && !apiCryptoAlgorithms.isEmpty()) {
            logger.debug("【ApiCrypto】 all Decrypt Algorithm : [" + apiCryptoAlgorithms + "]");

            for (ApiCryptoAlgorithm a : apiCryptoAlgorithms) {
                if (a.isCanRealize(parameter, false)) {
                    apiCryptoAlgorithm = a;
                    return true;
                }
            }
        } else {
            logger.debug("【ApiCrypto】 no Decrypt Algorithm.( 没有可用的 ApiCryptoAlgorithm 实现 )");
        }
        return false;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return apiCryptoAlgorithm.beforeBodyRead(inputMessage, parameter, targetType, converterType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
