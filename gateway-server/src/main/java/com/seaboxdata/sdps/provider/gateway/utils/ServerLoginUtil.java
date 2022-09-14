package com.seaboxdata.sdps.provider.gateway.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.provider.gateway.constant.LoginServerConstants;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerLoginUtil {
	public static final Map<String, String> charMap = MapUtil.newHashMap();
	public static final Map<String, String> charEncodeMap = MapUtil
			.newHashMap();
	public static final List<String> needLoginServer = CollUtil.newArrayList();
	static {
		charMap.put("|", "@");
		charEncodeMap.put("@", "%7C");
		needLoginServer.add(ServerTypeEnum.A.name());
		needLoginServer.add(ServerTypeEnum.S.name());
		needLoginServer.add(ServerTypeEnum.G.name());
		needLoginServer.add(ServerTypeEnum.L.name());
		needLoginServer.add(ServerTypeEnum.SFL2.name());
		needLoginServer.add(ServerTypeEnum.R.name());
		needLoginServer.add(ServerTypeEnum.SLOG2.name());
		needLoginServer.add(ServerTypeEnum.D.name());
		needLoginServer.add(ServerTypeEnum.SEA_OSS.name());
		needLoginServer.add(ServerTypeEnum.SDT2.name());
		needLoginServer.add(ServerTypeEnum.SHBASE.name());
		needLoginServer.add(ServerTypeEnum.SKAFKA.name());
		needLoginServer.add(ServerTypeEnum.SCS.name());
		needLoginServer.add(ServerTypeEnum.SMS.name());
	}

	public static Map<String, String> login(MenuService menuService,
			String clusterId, String serverType, String username) {
		Result<Map<String, String>> loginResult = menuService.serverLogin(
				clusterId, serverType, username, false);
		if (loginResult.getCode() == 0) {
			return loginResult.getData();
		}
		return MapUtil.newConcurrentHashMap();
	}

	public static void saveLoginCert(RedisRepository redisRepository,
			Map<String, String> cert, String clusterId, String serverType,
			String username) {
		String redis_key = appendRedisKey(clusterId, serverType, username);
		redisRepository.setExpire(redis_key, cert,
				LoginServerConstants.redis_timeout, TimeUnit.MINUTES);
		LoginServerConstants.loginServerMap.put(clusterId
				+ LoginServerConstants.redis_join + serverType, username);
	}

	public static String appendRedisKey(String clusterId, String serverType,
			String username) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(LoginServerConstants.login_cert_redis_key)
				.append(clusterId).append(LoginServerConstants.redis_join)
				.append(serverType).append(LoginServerConstants.redis_join)
				.append(username);
		String redis_key = stringBuffer.toString();
		return redis_key.toString();
	}

	public static void requestHeaderHanderByServerType(
			ServerHttpRequest.Builder requestBuilder, String serverType) {
		switch (serverType) {
		case "S":
			requestBuilder
					.headers(httpHeaders -> {
						httpHeaders
								.remove(LoginServerConstants.sdp_authorization_header);
						List<String> cert = httpHeaders
								.get(LoginServerConstants.sdo_authorization_header);
						if (CollUtil.isNotEmpty(cert)) {
							httpHeaders
									.add(LoginServerConstants.sdp_authorization_header,
											cert.get(0));
							httpHeaders
									.remove(LoginServerConstants.sdo_authorization_header);
						}
					});
			break;
		case "G":
			requestBuilder.headers(httpHeaders -> {
				httpHeaders
						.remove(LoginServerConstants.sdp_authorization_header);
			});
			break;
		case "SLOG2":
			requestBuilder.headers(httpHeaders -> {
				httpHeaders
						.remove(LoginServerConstants.sdp_authorization_header);
			});
			break;
		default:
			break;
		}
	}

	public static String usernameHandle(String serverType, String username) {
		switch (serverType) {
		case "A":
			username = LoginServerConstants.ambari_login_username;
			break;
		case "S":
			username = LoginServerConstants.hue_login_username;
			break;
		case "L":
			username = LoginServerConstants.log_search_login_username;
			break;
		case "SFL2":
			username = LoginServerConstants.sfl2_login_username;
			break;
		case "SLOG2":
			username = LoginServerConstants.slog2_login_username;
			break;
		case "D":
			username = LoginServerConstants.sredis_login_username;
			break;
		case "SEA_OSS":
			username = LoginServerConstants.SEA_OSS_LOGIN_USERNAME;
			break;
		case "SDT2":
			username = LoginServerConstants.SDT2_LOGIN_USERNAME;
			break;
		case "SHBASE":
			username = LoginServerConstants.shbase_login_username;
			break;
		case "SCS":
			username = LoginServerConstants.scs_login_username;
			break;
		case "SMS":
			username = LoginServerConstants.sms_login_username;
			break;
		default:
			break;
		}
		return username;
	}
}
