package com.seaboxdata.sdps.common.core.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReplaceStreamFilter implements Filter {

	@Override
	public void destroy() {
		Filter.super.destroy();
		log.info("------------StreamFilter destroy-----------");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		Filter.super.init(filterConfig);
		log.info("------------StreamFilter init-----------");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (StrUtil.isNotBlank(request.getContentType())
				&& !request.getContentType().contains("multipart/form-data")) {
			ServletRequest requestWrapper = new RequestWrapper(
					(HttpServletRequest) request);
			chain.doFilter(requestWrapper, response);
		} else {
			chain.doFilter(request, response);
		}
	}

}
