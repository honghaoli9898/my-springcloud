package com.seaboxdata.sdps.common.utils.excelutil;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class RestTemplateUtil {

    /**
     * 发送get请求 返回简单类型对象
     *
     * @param restTemplate
     * @param url          请求地址
     * @param httpEntity   http实体
     * @param responseType 响应类型
     * @param params       请求参数
     * @return
     */
    public static <T> ResponseEntity<T> restGet(RestTemplate restTemplate, String url, HttpEntity httpEntity, Class<T> responseType,
                                                Map<String, ?> params) {
        StringBuilder sbUrl = new StringBuilder(url);
        if (null != params) {
            int i = 1;
            for (String key : params.keySet()) {
                if (i == 1) {
                    sbUrl.append("?").append(key).append("=").append("{").append(key).append("}");
                } else {
                    sbUrl.append("&").append(key).append("=").append("{").append(key).append("}");
                }
                i++;
            }
        } else {
            params = Maps.newHashMap();
        }
        log.info("restTemplate调用接口restGet:{} ;params : {}", sbUrl.toString(), JSON.toJSONString(params));
        ResponseEntity<T> responseEntity = restTemplate.exchange(
                sbUrl.toString(),
                HttpMethod.GET,
                httpEntity,
                responseType,
                params
        );
        return responseEntity;
    }

    /**
     * 发送delete请求 返回简单类型对象
     *
     * @param restTemplate
     * @param url          请求地址
     * @param httpEntity   http实体
     * @param responseType 响应类型
     * @param params       请求参数
     * @return
     */
    public static <T> ResponseEntity<T> restDelete(RestTemplate restTemplate, String url, HttpEntity httpEntity, Class<T> responseType,
                                                   Map<String, ?> params) {
        StringBuilder sbUrl = new StringBuilder(url);
        if (null != params) {
            int i = 1;
            for (String key : params.keySet()) {
                if (i == 1) {
                    sbUrl.append("?").append(key).append("=").append("{").append(key).append("}");
                } else {
                    sbUrl.append("&").append(key).append("=").append("{").append(key).append("}");
                }
                i++;
            }
        } else {
            params = Maps.newHashMap();
        }
        log.info("restTemplate调用接口restDelete:{} ;params : {}", sbUrl.toString(), JSON.toJSONString(params));
        ResponseEntity<T> responseEntity = restTemplate.exchange(
                sbUrl.toString(),
                HttpMethod.DELETE,
                httpEntity,
                responseType,
                params
        );
        return responseEntity;
    }
}
