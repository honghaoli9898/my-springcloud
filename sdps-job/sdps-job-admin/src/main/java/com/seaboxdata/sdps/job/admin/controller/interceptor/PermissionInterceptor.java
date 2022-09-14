package com.seaboxdata.sdps.job.admin.controller.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.job.admin.controller.annotation.PermissionLimit;
import com.seaboxdata.sdps.job.admin.core.model.XxlJobUser;
import com.seaboxdata.sdps.job.admin.core.util.I18nUtil;
import com.seaboxdata.sdps.job.admin.service.LoginService;

/**
 * 权限拦截
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter {

	@Resource
	private LoginService loginService;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		if (!(handler instanceof HandlerMethod)) {
			return super.preHandle(request, response, handler);
		}

		// if need login
		boolean needLogin = true;
		boolean needAdminuser = false;
		HandlerMethod method = (HandlerMethod) handler;
		PermissionLimit permission = method
				.getMethodAnnotation(PermissionLimit.class);
		if (permission != null) {
			needLogin = permission.limit();
			needAdminuser = permission.adminuser();
		}

		if (needLogin) {
			String username = request.getHeader(SecurityConstants.USER_HEADER);
			if (StrUtil.isNotBlank(username)
					&& !StrUtil.equalsIgnoreCase(
							CommonConstant.ADMIN_USER_NAME, username)) {
				throw new RuntimeException(
						I18nUtil.getString("system_permission_limit"));
			}
			XxlJobUser loginUser = loginService.ifLogin(request, response);
			if (loginUser == null) {
				// response.setStatus(302);
				// response.setHeader("location",
				// request.getContextPath()+"/toLogin");
				// return false;
				loginUser = new XxlJobUser();
				loginUser.setId(1);
				loginUser.setUsername("admin");
				loginUser.setPassword("e10adc3949ba59abbe56e057f20f883e");
				loginUser.setRole(1);
				request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);
				return super.preHandle(request, response, handler);
			}
			if (needAdminuser && loginUser.getRole() != 1) {
				throw new RuntimeException(
						I18nUtil.getString("system_permission_limit"));
			}

			request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);
		}

		return super.preHandle(request, response, handler);
	}

}
