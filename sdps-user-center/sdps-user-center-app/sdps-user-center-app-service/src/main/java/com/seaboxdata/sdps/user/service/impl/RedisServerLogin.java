package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.ServerLogin;

@Slf4j
@Service(value = "sredis")
public class RedisServerLogin implements ServerLogin {
	private RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

	private String appendUrl(String host, String port, String path) {
		StringBuffer sb = new StringBuffer();
		sb.append("http://").append(host).append(":").append(port).append(path);
		return sb.toString();
	}

	@Override
	public Map<String, String> login(SdpsServerInfo sdpsServerInfo,
			String username) {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
		headers.add(
				"Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		String url = appendUrl(sdpsServerInfo.getHost(),
				sdpsServerInfo.getPort(), sdpsServerInfo.getLoginUrl());
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", url);
			throw new BusinessException("调用sredis登录接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		String cookie = StrUtil.splitTrim(cookies.get(0), ";", 2).get(0);
		cookie = cookie.concat(";CACHE_CLOUD_USER_STATUS=admin");
		result.put("Cookie", cookie);
		return result;
	}

	public static void main(String[] args) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
		headers.add(
				"Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				"http://10.1.3.25:8081/manage/login", HttpMethod.GET,
				httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			throw new BusinessException("调用sredis登录接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		String cookie = StrUtil.splitTrim(cookies.get(0), ";", 2).get(0);
		cookie = cookie.concat(";CACHE_CLOUD_USER_STATUS=admin");
		result.put("Cookie", cookie);
		System.out.println(result);
	}
}
