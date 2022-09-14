package com.seaboxdata.sdps.seaboxProxy.config;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	@Value("${http.maxTotal}")
	private Integer maxTotal;

	@Value("${http.defaultMaxPerRoute}")
	private Integer defaultMaxPerRoute;

	@Value("${http.connectTimeout}")
	private Integer connectTimeout;

	@Value("${http.connectionRequestTimeout}")
	private Integer connectionRequestTimeout;

	@Value("${http.socketTimeout}")
	private Integer socketTimeout;

	@Value("${http.staleConnectionCheckEnabled}")
	private boolean staleConnectionCheckEnabled;

	@Value("${http.validateAfterInactivity}")
	private Integer validateAfterInactivity;

	@Bean(name = "SeaboxRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate(httpRequestFactory());
	}

	@LoadBalanced
	@Bean(name = "SeaboxInnerRestTemplate")
	public RestTemplate innerRestTemplate() {
		return new RestTemplate(httpRequestFactory());
	}

	@Bean(name = "SeaboxClientHttpRequestFactory")
	public ClientHttpRequestFactory httpRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory(httpClient());
	}

	@Bean(name = "SeaboxHttpClient")
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
		connectionManager.setMaxTotal(maxTotal); // 最大连接数
		connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute); // 单个路由最大连接数
		connectionManager.setValidateAfterInactivity(validateAfterInactivity); // 最大空间时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(socketTimeout) // 服务器返回数据(response)的时间，超过抛出read
													// timeout
				.setConnectTimeout(connectTimeout) // 连接上服务器(握手成功)的时间，超出抛出connect
													// timeout
				.setStaleConnectionCheckEnabled(staleConnectionCheckEnabled) // 提交前检测是否可用
				.setConnectionRequestTimeout(connectionRequestTimeout)// 从连接池中获取连接的超时时间，超时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
																		// Timeout
																		// waiting
																		// for
																		// connection
																		// from
																		// pool
				.build();
		return HttpClientBuilder.create().disableCookieManagement()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager).build();
	}

}
