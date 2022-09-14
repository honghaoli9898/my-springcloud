package com.seaboxdata.sdps.common.crypto.advice;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.seaboxdata.sdps.common.crypto.algorithm.ApiCryptoAlgorithm;
import com.seaboxdata.sdps.common.crypto.annotation.Encrypt;

/**
 * 加密响应类
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 **/
@ControllerAdvice
@ConditionalOnClass({ HttpServletRequest.class, RequestContextHolder.class })
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object>, Serializable {

	private static final long serialVersionUID = -7849007763166479170L;

	private static final Log logger = LogFactory.getLog(EncryptResponseBodyAdvice.class);

    private ApiCryptoAlgorithm apiCryptoAlgorithm;

    private final List<ApiCryptoAlgorithm> apiCryptoAlgorithms;

    public EncryptResponseBodyAdvice(List<ApiCryptoAlgorithm> apiCryptoAlgorithms) {
        this.apiCryptoAlgorithms = apiCryptoAlgorithms;
    }


    @Override
    public boolean supports(MethodParameter parameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 方法上有排除加密注解
        if (!parameter.hasMethodAnnotation(Encrypt.class)) {
            return false;
        }

        if (Objects.nonNull(apiCryptoAlgorithms) && !apiCryptoAlgorithms.isEmpty()) {
            logger.debug("【ApiCrypto】 all Encrypt Algorithm : [" + apiCryptoAlgorithms + "]");

            for (ApiCryptoAlgorithm a : apiCryptoAlgorithms) {
                if (a.isCanRealize(parameter, false)) {
                    apiCryptoAlgorithm = a;
                    return true;
                }
            }

        } else {
            logger.debug("【ApiCrypto】 no Encrypt Algorithm.( 没有可用的 ApiCryptoAlgorithm 实现 )");
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        return apiCryptoAlgorithm.responseBefore(body, methodParameter, mediaType, aClass, serverHttpRequest, serverHttpResponse);
    }

}
