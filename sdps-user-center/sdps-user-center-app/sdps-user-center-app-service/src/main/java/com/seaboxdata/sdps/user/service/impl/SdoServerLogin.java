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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
@Service(value = "sdo")
public class SdoServerLogin implements ServerLogin {


	private RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());

	private String appendUrl(String host, String port, String path) {
		StringBuffer sb = new StringBuffer();
		sb.append("http://").append(host).append(":").append(port).append(path);
		return sb.toString();
	}

	private Map<String, String> preLogin(String url) {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", url);
			throw new BusinessException("调用sdo登录接口失败");
		}
		List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookies)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		String cookie = "";
		cookies = CollUtil.sub(cookies, 0, 2);
		for (String string : cookies) {
			cookie = cookie + StrUtil.splitTrim(string, ";", 2).get(0) + ";";
		}
		result.put("Cookie", cookie.substring(0, cookie.length() - 1));
		return result;
	}

	private Map<String, String> getToken(String url, Map<String, String> cookies) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookies.get("Cookie"));
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", url);
			throw new BusinessException("调用sdo登录接口失败");
		}
		JSONObject token = JSONObject.parseObject(responseEntity.getBody());
		Map<String, String> result = MapUtil.newHashMap();
		result.put("csrfmiddlewaretoken", token.getString("token"));
		return result;
	}

	private Map<String, String> login(String url, Map<String, String> cookies,String referer) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookies.get("Cookie"));
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Referer", referer);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("username", cookies.get("username"));
		params.add("password", cookies.get("password"));
		params.add("next", "/");
		params.add("csrfmiddlewaretoken", cookies.get("csrfmiddlewaretoken"));
		HttpEntity<Object> httpEntity = new HttpEntity<>(params, headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.POST, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()
				&& HttpStatus.FOUND.value() != responseEntity
						.getStatusCodeValue()) {
			log.error("调用登录接口失败,url={}", url);
			throw new BusinessException("调用sdo登录接口失败");
		}
		List<String> cookieList = responseEntity.getHeaders().get("Set-Cookie");
		if (CollUtil.isEmpty(cookieList)) {
			throw new BusinessException("未获取到cookie");
		}
		Map<String, String> result = MapUtil.newHashMap();
		String cookie = "";
		cookieList = CollUtil.sub(cookieList, 0, 2);
		String xcsrftoken = StrUtil.splitTrim(cookieList.get(0), ";", 2).get(0);
		for (String string : cookieList) {
			cookie = cookie + StrUtil.splitTrim(string, ";", 2).get(0) + ";";
		}
		result.put("Cookie", cookie.substring(0, cookie.length() - 1));
		result.put("X-CSRFToken", StrUtil.splitTrim(xcsrftoken, "=", 2).get(1));
		return result;
	}

	@Override
	public Map<String, String> login(SdpsServerInfo sdpsServerInfo,
			String username) {
		List<String> urls = StrUtil.split(sdpsServerInfo.getLoginUrl(), '|');
		String url = appendUrl(sdpsServerInfo.getHost(),
				sdpsServerInfo.getPort(), urls.get(0));
		String referer = url;
		Map<String, String> cookies = preLogin(url);
		url = appendUrl(sdpsServerInfo.getHost(), sdpsServerInfo.getPort(),
				urls.get(1));
		cookies.putAll(getToken(url, cookies));
		url = appendUrl(sdpsServerInfo.getHost(), sdpsServerInfo.getPort(),
				urls.get(2));
		cookies.put("username", sdpsServerInfo.getUser());
		cookies.put("password", sdpsServerInfo.getPasswd());
		return login(url, cookies, referer);
	}
	
	public static void main(String[] args) {
//		String loginUrl = "/accounts/login?next=/|/accounts/getToken|/accounts/login";
		String loginUrl = "/accounts/login?next=/desktop/log_analytics|/accounts/getToken|/accounts/login/";
		List<String> urls = StrUtil.split(loginUrl, '|');
		SdoServerLogin sdoServerLogin = new SdoServerLogin();
		String url = sdoServerLogin.appendUrl("10.1.2.5",
				"8888", urls.get(0));
		String newUrl = url;
		Map<String, String> cookies = sdoServerLogin.preLogin(url);
		url = sdoServerLogin.appendUrl("10.1.2.5","8888",
				urls.get(1));
		cookies.putAll(sdoServerLogin.getToken(url, cookies));
		url = sdoServerLogin.appendUrl("10.1.2.5","8888",
				urls.get(2));
		cookies.put("username", "admin");
		cookies.put("password", "admin");
		System.out.println(sdoServerLogin.login(url, cookies,newUrl));
	}
}
