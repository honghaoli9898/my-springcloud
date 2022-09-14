package com.seaboxdata.sdps.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.ServerLogin;

import lombok.extern.slf4j.Slf4j;

import org.omg.CORBA.NameValuePair;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service(value = "sdt2")
public class SdtServerLogin implements ServerLogin {
    public static void main(String[] args) {
        SdtServerLogin login = new SdtServerLogin();
        SdpsServerInfo info = new SdpsServerInfo();
        info.setHost("10.1.3.25");
        info.setPort("12345");
        info.setLoginUrl("/dolphinscheduler/login");
        info.setUser("admin");
        info.setPasswd("dolphinscheduler123");
        login.login(info, "admin");
    }

    private RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

    private String appendUrl(String host, String port, String path) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://").append(host).append(":").append(port).append(path);
        return sb.toString();
    }

    @Override
    public Map<String, String> login(SdpsServerInfo sdpsServerInfo, String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String url = appendUrl(sdpsServerInfo.getHost(),
                sdpsServerInfo.getPort(), sdpsServerInfo.getLoginUrl());

        MultiValueMap<String, String> params= new LinkedMultiValueMap<>();
        //  也支持中文
        params.add("userName", sdpsServerInfo.getUser());
        params.add("userPassword", sdpsServerInfo.getPasswd());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        log.info("请求url={},httpEntity={}", url, httpEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, String.class);
        if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
            log.error("调用登录接口失败,url={}", url);
            throw new BusinessException("调用Sdt2接口失败");
        }
        JSONObject jsonObject = JSONObject.parseObject(responseEntity.getBody());
        if (0 != jsonObject.getInteger("code")) {
            log.error("调用登录接口失败,url={}", url);
            throw new BusinessException("调用Sdt2接口失败");
        }
        List<String> cookieList = responseEntity.getHeaders().get("Set-Cookie");
        if (CollUtil.isEmpty(cookieList)) {
            throw new BusinessException("未获取到cookie");
        }
        String cookie = "";
        cookieList = CollUtil.sub(cookieList, 0, 2);
        for (String string : cookieList) {
            cookie = cookie + StrUtil.splitTrim(string, ";", 2).get(0) + ";";
        }

        Map<String, String> result = MapUtil.newHashMap();
        result.put("Cookie", cookie.substring(0, cookie.length() - 1));
        return result;
    }
}
