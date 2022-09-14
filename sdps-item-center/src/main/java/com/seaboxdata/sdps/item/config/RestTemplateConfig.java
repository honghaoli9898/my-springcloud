package com.seaboxdata.sdps.item.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.seaboxdata.sdps.common.ribbon.config.RestTemplateProperties;

@Configuration
public class RestTemplateConfig {
	@Autowired
	private RestTemplateProperties restTemplateProperties;

	@Bean(name = "ItemRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate(httpRequestFactory());
	}

	@Bean(name = "ItemClientHttpRequestFactory")
	public ClientHttpRequestFactory httpRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory(httpClient());
	}

	@Bean(name = "ItemHttpClient")
	public HttpClient httpClient() {
		TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
		SSLContext sslContext = null;
		try {
			sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
		} catch (KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException e) {
		}
		SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(
				sslContext, new NoopHostnameVerifier());
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http",
						PlainConnectionSocketFactory.getSocketFactory())
				.register("https", connectionSocketFactory).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
				registry);
		// 最大链接数
		connectionManager.setMaxTotal(restTemplateProperties.getMaxTotal());
		// 同路由并发数20
		connectionManager.setDefaultMaxPerRoute(restTemplateProperties
				.getMaxPerRoute());

		RequestConfig requestConfig = RequestConfig
				.custom()
				// 读超时
				.setSocketTimeout(restTemplateProperties.getReadTimeout())
				// 链接超时
				.setConnectTimeout(restTemplateProperties.getConnectTimeout())
				// 链接不够用的等待时间
				.setConnectionRequestTimeout(
						restTemplateProperties.getReadTimeout()).build();

		return HttpClientBuilder.create().disableCookieManagement()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
				.build();
	}

}
