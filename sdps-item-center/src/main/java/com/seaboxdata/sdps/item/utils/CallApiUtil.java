package com.seaboxdata.sdps.item.utils;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;

@Slf4j
public class CallApiUtil {
	public static String getUrl(String path, String ip, String port) {
		StringBuilder newUrl = new StringBuilder("http://");
		if (StrUtil.isNotBlank(path)) {
			newUrl.append(ip).append(":").append(port).append(path);
		}
		return newUrl.toString();
	}

	public static JSONObject getApi(RestTemplate restTemplate,
			HttpHeaders headers, String url, String requestMode,
			Map<Integer, Object> param) {
		String newUrl = replaceStr(url, "\\{\\}", param);
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("请求url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()
				|| HttpStatus.FOUND.value() != responseEntity
						.getStatusCodeValue()) {
			log.error("调用接口失败");
			throw new BusinessException("调用接口失败");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public static JSONObject getApi(RestTemplate restTemplate,
			HttpHeaders headers, String url, String requestMode) {
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				getRequestMode(requestMode), httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()
				|| HttpStatus.FOUND.value() != responseEntity
						.getStatusCodeValue()) {
			log.error("调用接口失败");
			throw new BusinessException("调用接口失败");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public static JSONObject getApi(RestTemplate restTemplate,
			HttpHeaders headers, String url, String requestMode, Object data) {
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
		log.info("请求url={},httpEntity={}", url, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				getRequestMode(requestMode), httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()
				|| HttpStatus.FOUND.value() != responseEntity
						.getStatusCodeValue()) {
			log.error("调用接口失败");
			throw new BusinessException("调用接口失败");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public static void main(String[] args) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add("X-Requested-With", "XMLHttpRequest");
		headers.add(
				"Cookie",
				"csrftoken=sAYAdDLf2XzSBsUi9NTpCKrHxFG6AWFgEaEgZR2hKVRgof8ziZ7UC1Wv2KMdAayb;sessionid=v5kuo42xchi6ow5rfuii1okw0zg1z0fj");
		headers.add("X-CSRFToken",
				"sAYAdDLf2XzSBsUi9NTpCKrHxFG6AWFgEaEgZR2hKVRgof8ziZ7UC1Wv2KMdAayb");
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		String url = "http://10.1.3.25:8013/filebrowser/rmtree?skip_trash={}&next=/filebrowser/view={}";
		Map<Integer, Object> param = MapUtil.newHashMap();
		parts.add("csrfmiddlewaretoken",
				"sAYAdDLf2XzSBsUi9NTpCKrHxFG6AWFgEaEgZR2hKVRgof8ziZ7UC1Wv2KMdAayb");
		parts.add("path", "/project/wsz/sdps-24.sql");
		param.put(1, "true");
		param.put(2, "/project/wsz");
		getApi(restTemplate, headers, url, "POST", parts, param);
	}

	public static JSONObject getApi(RestTemplate restTemplate,
			HttpHeaders headers, String url, String requestMode, Object data,
			Map<Integer, Object> param) {
		String newUrl = replaceStr(url, "\\{\\}", param);
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
		log.info("请求url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()
				&& HttpStatus.FOUND.value() != responseEntity
						.getStatusCodeValue()) {
			log.error("调用接口失败");
			throw new BusinessException("调用接口失败");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	private static HttpMethod getRequestMode(String method) {
		HttpMethod httpMethod = null;
		switch (method) {
		case "GET":
			httpMethod = HttpMethod.GET;
			break;
		case "POST":
			httpMethod = HttpMethod.POST;
			break;
		case "PUT":
			httpMethod = HttpMethod.PUT;
			break;
		case "DELETE":
			httpMethod = HttpMethod.DELETE;
			break;
		default:
			httpMethod = HttpMethod.GET;
			break;
		}
		return httpMethod;
	}

	private static String replaceStr(CharSequence str, String regex,
			Map<Integer, Object> map) {
		if (StrUtil.isEmpty(str)) {
			return StrUtil.str(str);
		}
		final Matcher matcher = Pattern.compile(regex).matcher(str);
		final StringBuffer buffer = new StringBuffer();
		int i = 1;
		while (matcher.find()) {
			try {
				matcher.appendReplacement(buffer, map.get(i).toString());
			} catch (Exception e) {

			}
			i++;
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public static void download(RestTemplate restTemplate, HttpHeaders headers,
			String url, String requestMode, Map<Integer, Object> param,
			String filePath) {
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		String newUrl = replaceStr(url, "\\{\\}", param);
		ResponseEntity<byte[]> exchange = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, byte[].class);
		if (HttpStatus.OK.value() != exchange.getStatusCodeValue()
				&& HttpStatus.FOUND.value() != exchange.getStatusCodeValue())
			throw new BusinessException("调用接口失败");
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletResponse res = requestAttributes.getResponse();
		res.setContentType("application/octet-stream");
		res.setHeader(
				"Content-Disposition",
				"attachment; filename="
						+ filePath.substring(filePath.lastIndexOf("/") + 1));
		try {
			res.getOutputStream().write(exchange.getBody());
		} catch (IOException e) {
			log.error("返回文件流失败", e);
			throw new BusinessException("返回文件流失败");
		}
	}
}
