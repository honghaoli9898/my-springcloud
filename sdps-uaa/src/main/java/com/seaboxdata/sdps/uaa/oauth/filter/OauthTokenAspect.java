package com.seaboxdata.sdps.uaa.oauth.filter;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.context.TenantContextHolder;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.utils.AddrUtil;
import com.seaboxdata.sdps.common.framework.bean.LoginUserVo;
import com.seaboxdata.sdps.common.oauth2.properties.AuthProperties;
import com.seaboxdata.sdps.common.oauth2.properties.SecurityProperties;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;

/**
 * oauth-token拦截器 1. 赋值租户 2. 统一返回token格式
 *
 * @author zlt
 * @date 2020/3/29
 *       <p>
 *       Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
@Slf4j
@Component
@Aspect
public class OauthTokenAspect {
	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private TokenStore tokenStore;

	@Resource
	private SecurityProperties securityProperties;

	@Around("execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
	public Object handleControllerMethod(ProceedingJoinPoint joinPoint)
			throws Throwable {
		String keyName = "";
		String limitKey = "";
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String ip = AddrUtil.getRemoteAddr(request);
		try {
			Object[] args = joinPoint.getArgs();
			Principal principal = (Principal) args[0];
			Map<String, String> parameters = (Map<String, String>) args[1];
			String isKeepLogin = parameters.get("keepLogin");
			String grantType = parameters.get(OAuth2Utils.GRANT_TYPE);

			String username = parameters.get(SecurityConstants.USER_NAME);
			keyName = username;
			if (grantType.equals("password_code")) {
				AuthProperties authProperties = securityProperties.getAuth();
				if (authProperties.getOnlyOneLogin()) {
					Set<String> scanForUserKeys = redisRepository.scanWithConnection(SecurityConstants.LOGIN_KEY);
					log.info("scanForUserKeys: {}", JSONUtil.toJsonStr(scanForUserKeys));

					if (scanForUserKeys != null && (scanForUserKeys.size() != 1 || !scanForUserKeys.toArray()[0].equals(username))) {
						// Long delCnt = redisRepository.delCollection(scanForUserKeys);
						// redisRepository.delCollection(scanForAccessKeys);
						// log.info("共移除在线用户 {} 个", scanForUserKeys.size());

						// 移除所有用户 token
						for (String scanForUserKey : scanForUserKeys) {
							LoginUserVo loginUserVo = (LoginUserVo) redisRepository.get(scanForUserKey);
							log.info("用户为：{}", JSONUtil.toJsonStr(loginUserVo));
							OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(loginUserVo.getToken());
							tokenStore.removeAccessToken(oAuth2AccessToken);
							log.info("移除成功");
						}
					}
				}

				if (StrUtil.isBlank(isKeepLogin)
						|| !Objects.equals("1", isKeepLogin)) {
					if (StrUtil.isBlank(username)) {
						return ResponseEntity.status(HttpStatus.OK).body(
								Result.failed("用户名不能为空"));
					}
					if (redisRepository.exists(SecurityConstants.LOGIN_KEY
							.concat(username))) {
						Map<String, Object> map = MapUtil.newHashMap();
						map.put(SecurityConstants.ALREADY_LOGIN, true);
						map.put(SecurityConstants.USER_NAME, username);
						return ResponseEntity.status(HttpStatus.OK).body(
								Result.succeed(map));
					}
				}
				limitKey = SecurityConstants.LIMIT_KEY.concat(keyName)
						.concat("_")
						.concat(String.valueOf(Ipv4Util.ipv4ToLong(ip)));
				Object obj = redisRepository.get(limitKey);
				if (Objects.isNull(obj)) {
					redisRepository.setExpire(limitKey, 1,
							SecurityConstants.LIMIT_TIME, TimeUnit.SECONDS);
				} else {
					Long sum = Long.valueOf(obj.toString());
					if (sum >= SecurityConstants.LIMIT_COUNT) {
						return ResponseEntity
								.status(HttpStatus.OK)
								.body(Result.failed("登录次数过多请"
										.concat(String
												.valueOf(SecurityConstants.LIMIT_TIME / 60))
										.concat("分钟后尝试")));
					} else {
						sum += 1;
						redisRepository.setExpire(limitKey, sum,
								SecurityConstants.LIMIT_TIME, TimeUnit.SECONDS);
					}
				}
			}
			if (!(principal instanceof Authentication)) {
				throw new InsufficientAuthenticationException(
						"There is no client authentication. Try adding an appropriate authentication filter.");
			}
			String clientId = getClientId(principal);

			if (!parameters
					.containsKey(SecurityConstants.ACCOUNT_TYPE_PARAM_NAME)) {
				parameters.put(SecurityConstants.ACCOUNT_TYPE_PARAM_NAME,
						SecurityConstants.DEF_ACCOUNT_TYPE);
			}

			// 保存租户id
			TenantContextHolder.setTenant(clientId);
			Object proceed = joinPoint.proceed(args);
			if (SecurityConstants.AUTHORIZATION_CODE.equals(grantType)) {
				/*
				 * 如果使用 @EnableOAuth2Sso 注解不能修改返回格式，否则授权码模式可以统一改 因为本项目的
				 * sso-demo/ss-sso 里面使用了 @EnableOAuth2Sso
				 * 注解，所以这里就不修改授权码模式的token返回值了
				 */
				return proceed;
			} else {
				ResponseEntity<OAuth2AccessToken> responseEntity = (ResponseEntity<OAuth2AccessToken>) proceed;
				OAuth2AccessToken body = responseEntity.getBody();
				if (grantType.equals("password_code")) {
					LoginUserVo loginUserVo = new LoginUserVo(ip,
							DateUtil.now(), body.getValue());
					ClientDetails clientDetails = (ClientDetails) redisRepository
							.getRedisTemplate().opsForValue()
							.get(clientRedisKey(clientId));
					int validitySeconds = Objects.nonNull(clientDetails) ? clientDetails
							.getAccessTokenValiditySeconds()
							: SecurityConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
					redisRepository.setExpire(
							SecurityConstants.LOGIN_KEY.concat(keyName),
							loginUserVo, validitySeconds, TimeUnit.SECONDS);
					redisRepository.del(limitKey);
				}
				return ResponseEntity.status(HttpStatus.OK).body(
						Result.succeed(body));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			TenantContextHolder.clear();
		}
	}

	private String getClientId(Principal principal) {
		Authentication client = (Authentication) principal;
		if (!client.isAuthenticated()) {
			throw new InsufficientAuthenticationException(
					"The client is not authenticated.");
		}
		String clientId = client.getName();
		if (client instanceof OAuth2Authentication) {
			clientId = ((OAuth2Authentication) client).getOAuth2Request()
					.getClientId();
		}
		return clientId;
	}

	private String clientRedisKey(String clientId) {
		return SecurityConstants.CACHE_CLIENT_KEY + ":" + clientId;
	}
}
