package com.seaboxdata.sdps.common.core.utils;

import java.io.PrintWriter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebUtils {

	/**
	 * 返回json数据
	 *
	 * @param response
	 * @param object
	 */
	public static void writeJson(HttpServletResponse response, int status,
			Object object) {
		response.setHeader("Content-Type", "application/json;charset=UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(status);
		PrintWriter out = null;
		try {
			String data = object instanceof String ? (String) object : JsonUtil
					.toJSONString(object);
			out = response.getWriter();
			out.print(data);
			out.flush();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 返回json数据
	 *
	 * @param response
	 * @param object
	 */
	public static void writeJson(HttpServletResponse response, Object object) {
		writeJson(response, HttpServletResponse.SC_OK, object);
	}

	/**
	 * 返回json数据
	 *
	 * @param response
	 * @param object
	 */
	public static void writeJson(ServletResponse response, Object object) {
		if (response instanceof HttpServletResponse) {
			writeJson((HttpServletResponse) response, object);
		}
	}
}
