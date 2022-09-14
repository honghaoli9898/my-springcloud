package com.seaboxdata.sdps.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.ServerLogin;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * oos 登录Service
 *
 * @author jiaohongtao
 * @since 2022/04/01
 */
@Slf4j
@Service("SEA_OSS")
public class SeaOssServerLogin implements ServerLogin {
    private final RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

    @Override
    public Map<String, String> login(SdpsServerInfo sdpsServerInfo, String username) {
        // 构建 body
        JSONObject body = new JSONObject();
        body.put("id", 1);
        body.put("jsonrpc", "2.0");
        body.put("method", "web.Login");
        JSONObject userPasswd = new JSONObject().fluentPut("username", sdpsServerInfo.getUser())
                .fluentPut("password", sdpsServerInfo.getPasswd());
        body.put("params", userPasswd);

        // 构建 header和 url
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36");
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        String url = appendUrl(sdpsServerInfo.getHost(), sdpsServerInfo.getPort(), sdpsServerInfo.getLoginUrl());
        log.info("请求url={},httpEntity={}", url, httpEntity);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        if (HttpStatus.OK != responseEntity.getStatusCode()) {
            log.error("调用登录接口失败,url={}", url);
            throw new BusinessException("调用SeaOss登录接口失败");
        }
        Map<String, String> resultMap = new HashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(responseEntity.getBody());
        resultMap.put("Authorization", "Bearer " + jsonObject.getJSONObject("result").getString("token"));
        // 只做记录，无实际用处
        resultMap.put("Cookie", jsonObject.toJSONString());
        return resultMap;
    }

    /**
     * 拼接 OSS 登录url
     *
     * @param host ip
     * @param port 端口
     * @param path 路径
     */
    private String appendUrl(String host, String port, String path) {
        return "http://" + host + ":" + port + path;
    }
}
