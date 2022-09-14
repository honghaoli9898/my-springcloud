package com.seaboxdata.sdps.common.core.resolver;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.feign.UserService;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.utils.LoginUserUtils;

/**
 * Token转化SysUser
 *
 */
@Slf4j
public class TokenArgumentResolver implements HandlerMethodArgumentResolver {
	private UserService userService;

	public TokenArgumentResolver(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 入参筛选
	 *
	 * @param methodParameter
	 *            参数集合
	 * @return 格式化后的参数
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.hasParameterAnnotation(LoginUser.class)
				&& methodParameter.getParameterType().equals(SysUser.class);
	}

	/**
	 * @param methodParameter
	 *            入参集合
	 * @param modelAndViewContainer
	 *            model 和 view
	 * @param nativeWebRequest
	 *            web相关
	 * @param webDataBinderFactory
	 *            入参解析
	 * @return 包装对象
	 */
	@Override
	public Object resolveArgument(MethodParameter methodParameter,
			ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest nativeWebRequest,
			WebDataBinderFactory webDataBinderFactory) {
		LoginUser loginUser = methodParameter
				.getParameterAnnotation(LoginUser.class);
		boolean isFull = loginUser.isFull();
		HttpServletRequest request = nativeWebRequest
				.getNativeRequest(HttpServletRequest.class);
		// 账号类型
		String accountType = request
				.getHeader(SecurityConstants.ACCOUNT_TYPE_HEADER);
		return LoginUserUtils.getCurrentUser(request, isFull);
	}
}
