package com.seaboxdata.sdps.provider.gateway.filter;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.Cipher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.crypto.constants.EncodingType;
import com.seaboxdata.sdps.common.crypto.constants.SymmetricType;
import com.seaboxdata.sdps.common.crypto.util.CryptoUtil;
import com.seaboxdata.sdps.common.oauth2.properties.SecurityProperties;

/**
 * @author lengleng
 * @date 2019 /2/1 密码解密工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordDecoderGatewayFilterFactory extends
		AbstractGatewayFilterFactory {

	private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies
			.withDefaults().messageReaders();

	private static final String PASSWORD = "password";

	private static final String KEY_ALGORITHM = "AES";

	private final SecurityProperties securityProperties;

	@Override
	public GatewayFilter apply(Object config) {
		return new PasswordDecoderFilter(config);
	}

	private class PasswordDecoderFilter implements GatewayFilter, Ordered {

		@SuppressWarnings("unused")
		private Object config;

		PasswordDecoderFilter(Object config) {
			this.config = config;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Mono<Void> filter(ServerWebExchange exchange,
				GatewayFilterChain chain) {
			try {
				ServerHttpRequest request = exchange.getRequest();
				if (!StrUtil.containsAnyIgnoreCase(request.getURI().getPath(),
						SecurityConstants.OAUTH_TOKEN_URL)) {
					return chain.filter(exchange);
				}
				String grantType = request.getQueryParams().getFirst(
						"grant_type");
				if (StrUtil.equals(SecurityConstants.REFRESH_TOKEN, grantType)) {
					return chain.filter(exchange);
				}
				Class inClass = String.class;
				Class outClass = String.class;
				ServerRequest serverRequest = ServerRequest.create(exchange,
						messageReaders);
				Mono<?> modifiedBody = serverRequest.bodyToMono(inClass)
						.flatMap(decryptAES());

				BodyInserter bodyInserter = BodyInserters.fromPublisher(
						modifiedBody, outClass);
				HttpHeaders headers = new HttpHeaders();
				headers.putAll(exchange.getRequest().getHeaders());
				headers.remove(HttpHeaders.CONTENT_LENGTH);

				headers.set(HttpHeaders.CONTENT_TYPE,
						MediaType.APPLICATION_JSON_VALUE);
				CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
						exchange, headers);
				return bodyInserter.insert(outputMessage,
						new BodyInserterContext()).then(
						Mono.defer(() -> {
							
							ServerHttpRequest decorator = decorate(exchange,
									headers, outputMessage);
							return chain.filter(exchange.mutate()
									.request(decorator).build());
						}));
			} catch (Exception e) {
				JSONObject message = new JSONObject();
				message.put("code", 1);
				message.put("message", e.getMessage());
				return RewriteRequetFilter.errorHandle(message, exchange);
			}
		}

		@Override
		public int getOrder() {
			return -100;
		}

	}

	/**
	 * 原文解密
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	@SuppressWarnings("rawtypes")
	private Function decryptAES() throws Exception {
		return s -> {
			try {
				Map<String, String> inParamsMap = HttpUtil.decodeParamMap(
						(String) s, CharsetUtil.CHARSET_UTF_8);
				if (inParamsMap.containsKey(PASSWORD)) {
					String password = inParamsMap.get(PASSWORD);
					String[] arr = StrUtil.splitToArray(password, "&");
					password = CryptoUtil
							.symmetric(
									SymmetricType.AES_CFB_PKCS7_PADDING
											.getType(),
									SymmetricType.AES_CFB_PKCS7_PADDING
											.getMethod(),
									Cipher.DECRYPT_MODE,
									securityProperties.getAuth().getEncodeKey(),
									arr[0], EncodingType.BASE64, arr[1],
									StandardCharsets.UTF_8);
					inParamsMap.put(PASSWORD, password);
				} 
				return Mono.just(HttpUtil.toParams(inParamsMap,
						Charset.defaultCharset(), true));
			} catch (Exception e) {
				log.error("解析密码失败", e);
				throw new BusinessException("解析密码失败");
			}
		};
	}

	/**
	 * 报文转换
	 * 
	 * @return
	 */
	private ServerHttpRequestDecorator decorate(ServerWebExchange exchange,
			HttpHeaders headers, CachedBodyOutputMessage outputMessage) {
		return new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public HttpHeaders getHeaders() {
				long contentLength = headers.getContentLength();
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.putAll(super.getHeaders());
				if (contentLength > 0) {
					httpHeaders.setContentLength(contentLength);
				} else {
					httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
				}
				return httpHeaders;
			}

			@Override
			public Flux<DataBuffer> getBody() {
				return outputMessage.getBody();
			}
		};
	}

}
