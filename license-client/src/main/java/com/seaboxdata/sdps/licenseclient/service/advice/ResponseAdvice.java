package com.seaboxdata.sdps.licenseclient.service.advice;

import com.seaboxdata.sdps.licenseclient.common.ServerResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


/**
 * 你可以在这里修改头部或在主体中添加自定义信息--比如网关需要的内容
 * 这里我们返回服务链路跟踪的traceid
 *
 * @author stonehan
 */
@SuppressWarnings("all")
@RestControllerAdvice
//使用@ControllerAdvice注解也可以
public class ResponseAdvice implements ResponseBodyAdvice<ServerResponse>{

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class clz = returnType.getMethod().getReturnType();
        if (ServerResponse.class.isAssignableFrom(clz)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ServerResponse beforeBodyWrite(ServerResponse body, MethodParameter returnType,
            MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        if (body.getHint() == null) {
            body.setHint(request.getHeaders().getFirst("X-B3-TraceId"));
        }
        //TODO: add your self defined header content
        return body;
    }


}
