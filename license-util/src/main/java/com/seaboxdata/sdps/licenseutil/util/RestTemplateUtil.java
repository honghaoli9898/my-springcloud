package com.seaboxdata.sdps.licenseutil.util;

import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;

/**
 * @author allen
 */
@Slf4j
public class RestTemplateUtil {

	public static HttpComponentsClientHttpRequestFactory generateHttpsRequestFactory() {
		try {
			TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
			SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(
					sslContext, new NoopHostnameVerifier());

			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setHttpClient(httpClient);
			factory.setConnectTimeout(10 * 1000);
			factory.setReadTimeout(30 * 1000);
			return factory;
		} catch (Exception e) {
			throw new RuntimeException("创建HttpsRestTemplate失败", e);
		}
	}

	/**
	 * restTemplate调用api(GET方式)
	 *
	 * @return data
	 */
	public static <T> T restGet(RestTemplate restTemplate, String url,
			Class<T> responseType) {
		log.info("restTemplate GET调用接口: " + url);
		String obj = restTemplate.getForObject(url, String.class);
		JSONObject json = JSONObject.parseObject(obj);
		log.info(json.toJSONString());
		if (null != json.get("data")) {
			T data = JSONObject.parseObject(json.get("data").toString(),
					responseType);
			log.info("restTemplate GET调用接口: " + url + " response data: "
					+ data.toString());
			return data;
		} else {
			log.error("服务调用异常，url: " + url);
			throw BusinessException.REST_GET_SERVICE_EXCEPTION;
		}
	}

	/**
	 * restTemplate调用api(GET方式)
	 *
	 * @return data
	 */
	public static JSONObject restGet(RestTemplate restTemplate, String url) {
		log.info("restTemplate GET调用接口: " + url);
		String obj = restTemplate.getForObject(url, String.class);
		JSONObject json = JSONObject.parseObject(obj);
		log.info(json.toJSONString());
		return json;
	}

	/**
	 * restTemplate调用api(GET方式) static方法
	 *
	 * @return data
	 */
	public static <T> T restGetStatic(String url, Class<T> responseType) {
		log.info("restTemplate调用接口: " + url);
		String obj = new RestTemplate(generateHttpsRequestFactory())
				.getForObject(url, String.class);
		JSONObject json = JSONObject.parseObject(obj);
		return JSONObject
				.parseObject(json.get("data").toString(), responseType);
	}

	/**
	 * restTemplate调用api(GET方式)
	 *
	 * @return List<T>
	 */
	public static <T> List<T> restGetList(RestTemplate restTemplate,
			String url, Class<T> responseType) {
		log.info("restTemplate GET调用LIST接口: " + url);
		String obj = restTemplate.getForObject(url, String.class);
		JSONObject json = JSONObject.parseObject(obj);
		if (null != json.get("data")) {
			return JSONObject.parseArray(json.get("data").toString(),
					responseType);
		} else {
			log.error("服务调用异常，url: " + url);
			throw BusinessException.REST_POST_SERVICE_EXCEPTION;
		}
	}

	/**
	 * restTemplate调用api(POST方式,JSON请求体)
	 *
	 * @return JSON
	 */
	public static JSONObject restPost(RestTemplate restTemplate, String url,
			String requestBody) {
		log.info("restTemplate调用接口/POST: " + url);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", "application/json");
		requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
		HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
				requestHeaders);
		String obj = restTemplate.postForObject(url, httpEntity, String.class);
		JSONObject json = JSONObject.parseObject(obj);
		log.info("restPost响应:" + json.toJSONString());
		return json;
	}

	/**
	 * 返回指定类型对象
	 *
	 * @param restTemplate
	 * @param url
	 * @param requestBody
	 * @param tClass
	 * @param <T>
	 * @return
	 */
	public static <T> T restPost(RestTemplate restTemplate, String url,
			String requestBody, Class<T> tClass) {
		log.info("restTemplate调用接口/restPost:{} ;params : {}", url, requestBody);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", "application/json");
		requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
		HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
				requestHeaders);
		T res = restTemplate.postForObject(url, httpEntity, tClass);
		log.info("restPost响应:" + JSON.toJSONString(res));
		return res;
	}

	/**
	 * 发送get请求 返回简单类型对象
	 *
	 * @param restTemplate
	 * @param url
	 *            请求地址
	 * @param httpHeaders
	 *            请求头
	 * @param tClass
	 *            响应类型
	 * @param params
	 *            请求参数
	 * @param <T>
	 *            响应数据
	 * @return
	 */
	public static <T> T restGet(RestTemplate restTemplate, String url,
			HttpHeaders httpHeaders, Class<T> tClass, Map<String, ?> params) {
		StringBuilder sbUrl = new StringBuilder(url);
		if (null != params) {
			int i = 1;
			for (String key : params.keySet()) {
				if (i == 1) {
					sbUrl.append("?").append(key).append("=").append("{")
							.append(key).append("}");
				} else {
					sbUrl.append("&").append(key).append("=").append("{")
							.append(key).append("}");
				}
				i++;
			}
		} else {
			params = Maps.newHashMap();
		}
		log.info("restTemplate调用接口/restGet:{} ;params : {}", sbUrl.toString(),
				JSON.toJSONString(params));
		ResponseEntity<T> obj = restTemplate.exchange(sbUrl.toString(),
				HttpMethod.GET, new HttpEntity<>(httpHeaders), tClass, params);
		log.info("restGet响应:" + JSON.toJSONString(obj.getBody()));
		return obj.getBody();
	}

	/**
	 * 发送get请求 返回复杂类型对象
	 *
	 * @param restTemplate
	 * @param url
	 *            请求地址
	 * @param httpHeaders
	 *            请求头
	 * @param params
	 *            请求参数
	 * @param <T>
	 *            响应数据
	 * @return
	 */
	public static <T> T restGet(RestTemplate restTemplate, String url,
			HttpHeaders httpHeaders, TypeReference<T> valueTypeRef,
			Map<String, ?> params) {
		StringBuilder sbUrl = new StringBuilder(url);
		if (null != params) {
			int i = 1;
			for (String key : params.keySet()) {
				if (i == 1) {
					sbUrl.append("?").append(key).append("=").append("{")
							.append(key).append("}");
				} else {
					sbUrl.append("&").append(key).append("=").append("{")
							.append(key).append("}");
				}
				i++;
			}
		} else {
			params = Maps.newHashMap();
		}
		log.info("restTemplate调用接口/restGet:{} ;params : {}", sbUrl.toString(),
				JSON.toJSONString(params));
		ResponseEntity<String> obj = restTemplate.exchange(sbUrl.toString(),
				HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class,
				params);
		log.info("restGet响应:" + obj.getBody());
		T t = JacksonUtil.jsonToObjType(obj.getBody(), valueTypeRef);
		return t;
	}

	/**
	 * 发送post请求 返回复杂类型对象
	 *
	 * @param restTemplate
	 * @param url
	 *            请求地址
	 * @param httpHeaders
	 *            请求头
	 * @param <T>
	 *            响应数据
	 * @return
	 */
	public static <T> T restPost(RestTemplate restTemplate, String url,
			HttpHeaders httpHeaders, String requestBody,
			TypeReference<T> valueTypeRef) {

		log.info("restTemplate调用接口/restPost:{} ;params : {}", url, requestBody);

		if (null == httpHeaders) {
			httpHeaders = new HttpHeaders();
		}

		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json;charset=UTF-8");

		ResponseEntity<String> obj = restTemplate.exchange(url,
				HttpMethod.POST, new HttpEntity<>(requestBody, httpHeaders),
				String.class);
		log.info("restPost响应:" + obj.getBody());
		T t = JacksonUtil.jsonToObjType(obj.getBody(), valueTypeRef);
		return t;
	}

	/**
	 * 发送put请求 返回复杂类型对象
	 *
	 * @param restTemplate
	 * @param url
	 *            请求地址
	 * @param httpHeaders
	 *            请求头
	 * @param <T>
	 *            响应数据
	 * @return
	 */
	public static <T> T restPut(RestTemplate restTemplate, String url,
			HttpHeaders httpHeaders, String requestBody,
			TypeReference<T> valueTypeRef) {

		log.info("restTemplate调用接口/restPut:{} ;params : {}", url, requestBody);

		if (null == httpHeaders) {
			httpHeaders = new HttpHeaders();
		}

		ResponseEntity<String> obj = restTemplate.exchange(url, HttpMethod.PUT,
				new HttpEntity<>(requestBody, httpHeaders), String.class);
		log.info("restPut响应:" + obj.getBody());
		T t = JacksonUtil.jsonToObjType(obj.getBody(), valueTypeRef);
		return t;
	}

}
