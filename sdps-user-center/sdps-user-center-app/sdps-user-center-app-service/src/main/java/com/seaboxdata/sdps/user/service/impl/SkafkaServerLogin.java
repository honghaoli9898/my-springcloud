package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.api.ServerLogin;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;

@Slf4j
@Service(value = "skafka")
public class SkafkaServerLogin implements ServerLogin {
	@Autowired
	BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	private IUserService uerService;

	private RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

	@Override
	public Map<String, String> login(SdpsServerInfo sdpsServerInfo,
			String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add("X-Data-Center", "cn");
		headers.add("X_Project_Type", "open");
		JSONObject body = new JSONObject();
		body.put("username", username);
		body.put("password", sdpsServerInfo.getPasswd());
		body.put("dataCenter", "cn");
		HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

		String skafkaBaseUrl = "http://".concat(sdpsServerInfo.getHost())
				.concat(":").concat(sdpsServerInfo.getPort())
				.concat(sdpsServerInfo.getLoginUrl());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				skafkaBaseUrl, HttpMethod.POST, httpEntity, String.class);

		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", skafkaBaseUrl);
			throw new BusinessException("调用skafka登录接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		StringBuilder sb = new StringBuilder();
		cookies.forEach(cookie -> {
			sb.append(StrUtil.splitTrim(cookie, ";", 2).get(0));
			sb.append("; ");
		});
		JSONObject jsonObj = JSONObject.parseObject(responseEntity.getBody());
		sb.append("username=").append(username).append("; role=")
				.append(jsonObj.getJSONObject("data").getString("role"));
		result.put("Cookie", sb.toString());
		return result;
	}

}
