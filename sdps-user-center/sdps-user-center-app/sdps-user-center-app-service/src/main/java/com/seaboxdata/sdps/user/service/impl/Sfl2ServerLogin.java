package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.ServerLogin;

@Slf4j
@Service(value = "sfl2")
public class Sfl2ServerLogin implements ServerLogin {
    private RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

    public static void main(String[] args) {
        Sfl2ServerLogin sfl2ServerLogin = new Sfl2ServerLogin();
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = sfl2ServerLogin.appendUrl("10.1.2.162",
                "8881", "/api/login");
        JSONObject body = new JSONObject();

        body.put("autoLogin", "true");
        body.put("grant_type", "password");

        body.put("username", "admin");
        body.put("type", "password");
        body.put("password", "admin");

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        log.info("请求url={},httpEntity={}", url, httpEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, String.class);
        if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
            log.error("调用登录接口失败,url={}", url);
            throw new BusinessException("调用sfl2查询接口失败");
        }
        log.info("----------header={}", responseEntity.getHeaders());
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        if (CollUtil.isEmpty(cookies)) {
            throw new BusinessException("未获取到cookie");
        }
        Map<String, String> result = MapUtil.newHashMap();
        result.put("Cookie", StrUtil.splitTrim(cookies.get(0), ";", 2).get(0));
        System.out.println(result);
    }

    private String appendUrl(String host, String port, String path) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://").append(host).append(":").append(port).append(path);
        return sb.toString();
    }

    @Override
    public Map<String, String> login(SdpsServerInfo sdpsServerInfo, String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = appendUrl(sdpsServerInfo.getHost(),
                sdpsServerInfo.getPort(), sdpsServerInfo.getLoginUrl());
        JSONObject body = new JSONObject();

        body.put("autoLogin", "true");
        body.put("grant_type", "password");
        body.put("type", "password");

        body.put("username", sdpsServerInfo.getUser());
        body.put("password", sdpsServerInfo.getPasswd());

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        log.info("请求url={},httpEntity={}", url, httpEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, String.class);
        if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
            log.error("调用登录接口失败,url={}", url);
            throw new BusinessException("调用Sfl2接口失败");
        }
        log.info("----------header={}", responseEntity.getHeaders());
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        if (CollUtil.isEmpty(cookies)) {
            throw new BusinessException("未获取到sfl2 cookie");
        }
        Map<String, String> result = MapUtil.newHashMap();
        result.put("Cookie", StrUtil.splitTrim(cookies.get(0), ";", 2).get(0));
        return result;
    }
}
