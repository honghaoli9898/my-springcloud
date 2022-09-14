package com.seaboxdata.sdps.uaa.example.custom;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaboxdata.sdps.common.crypto.algorithm.ApiCryptoAlgorithm;
import com.seaboxdata.sdps.common.crypto.exception.ApiDecodeException;
import com.seaboxdata.sdps.common.crypto.exception.ApiEncryptException;

/**
 * 自定义实现一种模式： 交互数据&md5(交互数据+盐)
 * 例如：
 * 要传输的数据是 ：abc123
 * 盐(salt)是   : 123456
 * 最后传输数据是 : abc123&D698BE90ADDD35AB
 * 这样子就可以保证数据传输安全不被篡改了。
 */
public class CustomApiCrypto implements ApiCryptoAlgorithm {
    @Autowired
    private ObjectMapper objectMapper;

    private static final Log logger = LogFactory.getLog(CustomApiCrypto.class);

    /**
     * 处理自定义 加密/解密注解 或 其他方式
     *
     * @param methodParameter:   方法参数
     * @param requestOrResponse: 请求还是响应，true请求，false响应
     * @return boolean 返回 true 将执行下面两个对应的方法，false则忽略
     **/
    @Override
    public boolean isCanRealize(MethodParameter methodParameter, boolean requestOrResponse) {
        // 获取执行 方法或类上的指定注解（注解在类上并且是被继承的无效）
        CustomCrypto annotation = this.getAnnotation(methodParameter, CustomCrypto.class);
        if (annotation == null) {
            return false;
        }
        // 是否请求解密
        if (annotation.isDecryption() && !requestOrResponse) {
            return true;
        }
        // 是否响应加密
        if (annotation.isEncryption() && !requestOrResponse) {
            return true;
        }
        return false;
    }

    /**
     * 当 isCanRealize 返回true，并且为请求时执行
     *
     * @param httpInputMessage: 请求内容
     * @param methodParameter:  执行方法参数
     * @param type:             目标类型
     * @param aClass:           消息转换器
     * @return org.springframework.http.HttpInputMessage 请求的数据输入流
     **/
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        // 请求内容
        byte[] byteArr = new byte[httpInputMessage.getBody().available()];
        httpInputMessage.getBody().read(byteArr);
        String str = new String(byteArr);
        // 拆分出数据和签名
        String[] strings = str.split("&");
        String data = strings[0];
        String md5 = strings[1];
        // 获取盐
        CustomCrypto annotation = this.getAnnotation(methodParameter, CustomCrypto.class);
        String salt = annotation.salt();
        // md5加密一下
        String test = DigestUtils.md5DigestAsHex((data + salt).getBytes(StandardCharsets.UTF_8));
        // 对比原来的md5加密是否一致，不一致说明被修改过请求体了
        if (!test.equals(md5)) {
            throw new ApiDecodeException("验证失败");
        }

        return this.stringToInputStream(data.getBytes(StandardCharsets.UTF_8), httpInputMessage.getHeaders(), logger);
    }

    /**
     * 当 isCanRealize 返回 true，并且为响应时执行
     *
     * @param body:               方法执行后返回的参数
     * @param methodParameter:    方法执行参数
     * @param mediaType:          数据类型，json，text，html等等
     * @param aClass:             消息转换器
     * @param serverHttpRequest:  请求
     * @param serverHttpResponse: 响应
     * @return java.lang.Object   最终响应内容
     **/
    @Override
    public Object responseBefore(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        CustomCrypto annotation = this.getAnnotation(methodParameter, CustomCrypto.class);

        // 如果方法返回内容是字符串
        if (body instanceof String) {
            // md5加密一下
            String md5 = DigestUtils.md5DigestAsHex((body + annotation.salt()).getBytes(StandardCharsets.UTF_8));
            return body + "&" + md5;
        }

        // 如果方法返回内容是对象
        String json = null;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ApiEncryptException("转json字符串失败");
        }

        // md5加密一下
        String md5 = DigestUtils.md5DigestAsHex((json + annotation.salt()).getBytes(StandardCharsets.UTF_8));
        return body + "&" + md5;
    }
}
