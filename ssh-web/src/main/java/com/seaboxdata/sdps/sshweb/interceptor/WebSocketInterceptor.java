package com.seaboxdata.sdps.sshweb.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.sshweb.constant.ConstantPool;

public class WebSocketInterceptor implements HandshakeInterceptor {
	/**
	 * @Description: Handler处理前调用
	 * @Param: [serverHttpRequest, serverHttpResponse, webSocketHandler, map]
	 * @return: boolean
	 * @Author: NoCortY
	 * @Date: 2020/3/1
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
			ServerHttpResponse serverHttpResponse,
			WebSocketHandler webSocketHandler, Map<String, Object> map)
			throws Exception {
		boolean result = false;
		if (serverHttpRequest instanceof ServletServerHttpRequest) {
			String userId = ((ServletServerHttpRequest) serverHttpRequest)
					.getServletRequest().getParameter("userId");
			// 将userId放到websocketsession中
			if (StrUtil.isNotBlank(userId)) {
				map.put(ConstantPool.USER_UUID_KEY, userId);
				result = true;
			}
		}
		return result;
	}

	@Override
	public void afterHandshake(ServerHttpRequest serverHttpRequest,
			ServerHttpResponse serverHttpResponse,
			WebSocketHandler webSocketHandler, Exception e) {

	}
}
