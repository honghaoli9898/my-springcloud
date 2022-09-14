package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
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

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.ServerLogin;

@Slf4j
@Service(value = "SCS")
public class ScsServerLogin implements ServerLogin {
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
		String url = appendUrl(sdpsServerInfo.getHost(),
				sdpsServerInfo.getPort(), sdpsServerInfo.getLoginUrl());
		String plainCreds = sdpsServerInfo.getUser() + ":"
				+ sdpsServerInfo.getPasswd();
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.POST, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", url);
			throw new BusinessException("调用scs接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到scs cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		result.put("Cookie", StrUtil.splitTrim(cookies.get(0), ";", 2).get(0));
		return result;
	}
}
