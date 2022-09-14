package com.seaboxdata.sdps.common.framework.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.hutool.core.util.StrUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.seaboxdata.sdps.common.core.config.RequestWrapper;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.utils.JsonUtil;

@Slf4j
public class CheckUserLogInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if ("/error".equals(request.getRequestURI())) {
			return true;
		}
		boolean result = isJson(request);
		if ("POST".equals(request.getMethod()) && result) {
			String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
			if (StrUtil.isBlank(userId))
				return true;
			String json = "";
			try {
				json = new RequestWrapper(request).getBodyString();
			} catch (Exception e) {
				log.error("检查userId与登录id报错", e);
			}
			if (StrUtil.isBlank(json))
				return true;
			JsonNode jsonObject = JsonUtil.parse(json);
			String userIdRe = jsonObject.get("userId").asText();
			if (StrUtil.isNotEmpty(userIdRe) && StrUtil.isNotEmpty(userId)) {
				if (!userId.equals(userIdRe))
					throw new RuntimeException("userId与登录id不符");
			}

		}

		return true;
	}

	private boolean isJson(HttpServletRequest request) {
		if (request.getContentType() != null) {
			return (request.getContentType().equals(
					MediaType.APPLICATION_JSON_VALUE) || request
					.getContentType().equals(
							MediaType.APPLICATION_JSON_UTF8_VALUE));
		}
		return false;
	}

}
