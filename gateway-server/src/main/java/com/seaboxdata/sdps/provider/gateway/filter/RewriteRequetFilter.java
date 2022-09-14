package com.seaboxdata.sdps.provider.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.provider.gateway.constant.LoginServerConstants;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;
import com.seaboxdata.sdps.provider.gateway.utils.ServerLoginUtil;

@Slf4j
@Component
public class RewriteRequetFilter implements GlobalFilter, Ordered {
	@Autowired
	private MenuService menuService;

	@Autowired
	private RedisRepository redisRepository;

	@Override
	public int getOrder() {
		return 10001;
	}

	private void replaceUrl(ServerWebExchange exchange) {
		try {
			URI requestUrl = exchange
					.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
			String url = requestUrl.toString();
			if (StrUtil.contains(url, "@")) {
				url = url.replaceAll("@", "%7C");
			}
			URI uri = null;

			uri = new URI(url);

			exchange.getAttributes().put(
					ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, uri);
		} catch (URISyntaxException e) {
			log.error("url转换报错", e);
		}
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange,
			GatewayFilterChain chain) {
		try {
			replaceUrl(exchange);
			ServerHttpRequest request = exchange.getRequest();
			String path = request.getURI().getPath();
			ServerHttpRequest.Builder requestBuilder = request.mutate();
			List<String> usernames = request.getHeaders().get(
					SecurityConstants.USER_HEADER);
			List<String> clusterIds = request.getHeaders().get(
					LoginServerConstants.cluster_id);
			List<String> serverTypes = request.getHeaders().get(
					LoginServerConstants.server_type);
			if (CollUtil.isNotEmpty(clusterIds)
					&& CollUtil.isNotEmpty(serverTypes)
					&& CollUtil.isNotEmpty(usernames)) {
				String clusterId = clusterIds.get(0);
				String serverType = serverTypes.get(0);
				String username = usernames.get(0);
				ServerLoginUtil.requestHeaderHanderByServerType(requestBuilder,
						serverType);
				if (ServerLoginUtil.needLoginServer.contains(serverType)) {
					username = ServerLoginUtil.usernameHandle(serverType,
							username);
					Map<String, String> certs = getLoginCert(clusterId,
							serverType, username);
					if (MapUtil.isEmpty(certs)) {
						Map<String, String> loginResult = ServerLoginUtil
								.login(menuService, clusterId, serverType,
										username);
						if (MapUtil.isEmpty(loginResult)) {
							JSONObject message = new JSONObject();
							message.put("code", 1);
							message.put("message", "组件登录失败");
							return errorHandle(message, exchange);
						}
						ServerLoginUtil.saveLoginCert(redisRepository,
								loginResult, clusterId, serverType, username);
						certs = loginResult;
					}
					addHeader(certs, requestBuilder);
					if (LoginServerConstants.cert_url_path.equals(path)) {
						JSONObject message = new JSONObject();
						message.put("code", 0);
						message.put("message", "组件登录凭证");
						message.put("data", certs);
						return errorHandle(message, exchange);
					}
				}
				ServerHttpRequest result = requestBuilder.build();
				return chain.filter(exchange.mutate().request(result).build());
			} else {
				return chain.filter(exchange);
			}
		} catch (Exception e) {
			JSONObject message = new JSONObject();
			message.put("code", 1);
			message.put("message", "组件登录报错");
			return errorHandle(message, exchange);
		}
	}

	private Map<String, String> getLoginCert(String clusterId,
			String serverType, String username) {
		String redis_key = ServerLoginUtil.appendRedisKey(clusterId,
				serverType, username);
		Object object = redisRepository.get(redis_key);
		if (Objects.isNull(object)) {
			return null;
		}
		if (object instanceof Map) {
			Map<String, String> cert = (Map<String, String>) object;
			return cert;
		}
		return MapUtil.newHashMap();

	}

	private void addHeader(Map<String, String> cert,
			ServerHttpRequest.Builder requestBuilder) {
		cert.forEach((k, v) -> {
			requestBuilder.header(k, v);
		});
	}

	public static Mono<Void> errorHandle(JSONObject error, ServerWebExchange exchange) {
		ServerHttpResponse response = exchange.getResponse();
		byte[] bits = error.toJSONString().getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bits);
		response.setStatusCode(HttpStatus.OK);
		response.getHeaders().add("Content-Type",
				"application/json;charset=UTF-8");
		return response.writeWith(Mono.just(buffer));
	}
}