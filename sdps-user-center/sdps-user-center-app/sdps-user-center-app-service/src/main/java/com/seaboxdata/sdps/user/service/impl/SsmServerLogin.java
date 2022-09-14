package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.api.ServerLogin;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;

@Slf4j
@Service(value = "ranger")
public class SsmServerLogin implements ServerLogin {
	@Autowired
	BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	private IUserService uerService;

	private RestTemplate restTemplate = new RestTemplate(
			RestTemplateUtil.generateHttpsRequestFactory());

	@Override
	public Map<String, String> login(SdpsServerInfo sdpsServerInfo,
			String username) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
		paramMap.add("username", username);
		SdpsServerInfo rangerInfo = uerService.selectServerUserInfo(username,
				ServerTypeEnum.C.name(), "ranger");
		if (Objects.isNull(rangerInfo)) {
			throw new BusinessException("暂无此用户");
		}
		String password = rangerInfo.getPasswd();
		paramMap.add("password", password);
		HttpEntity<Object> httpEntity = new HttpEntity<>(paramMap, headers);
		String rangerBaseUrl = "http://" + sdpsServerInfo.getHost() + ":"
				+ sdpsServerInfo.getPort().concat(sdpsServerInfo.getLoginUrl());
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				rangerBaseUrl, HttpMethod.POST, httpEntity, String.class);

		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", rangerBaseUrl);
			throw new BusinessException("调用ssm接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		result.put("Cookie", StrUtil.splitTrim(cookies.get(0), ";", 2).get(0)
				.concat("; clientTimeOffset=-480"));
		return result;
	}
}
