package com.seaboxdata.sdps.common.log.aspect;

import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.utils.AddrUtil;
import com.seaboxdata.sdps.common.log.model.RequestInfo;
import com.seaboxdata.sdps.common.log.model.RequestInfo.RequestInfoBuilder;
import com.seaboxdata.sdps.common.log.monitor.PointUtil;

@Aspect
@ConditionalOnClass({ HttpServletRequest.class, RequestContextHolder.class })
public class RequestLogAspect {
	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}

	@Autowired
	private Ip2regionSearcher regionSearcher;

	@Value("${spring.application.name}")
	private String applicationName;
	@Value("${spring.cloud.client.ip-address}")
	private String ipAddress;

	@Pointcut("execution(* com.seaboxdata.sdps..*.controller.*..*(..))")
	public void requestServer() {
	}

	@Around("requestServer()")
	public Object doAround(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		long start = System.currentTimeMillis();
		Object result;
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		RequestContextHolder.setRequestAttributes(
				RequestContextHolder.getRequestAttributes(), true);
		MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint
				.getSignature();
		if (Objects.nonNull(attributes)) {
			HttpServletRequest request = attributes.getRequest();
			String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
			String username = request.getHeader(SecurityConstants.USER_HEADER);
			String url = request.getRequestURI().toString();
			String startTime = DateUtil.now();
			Map<String, Object> requestParams = getRequestParamsByProceedJoinPoint(proceedingJoinPoint);
			String userIp = AddrUtil.getRemoteAddr(request);
			RequestInfoBuilder requestInfoBuilder = RequestInfo.builder()
					.serverIp(ipAddress).applicationName(applicationName)
					.startTime(startTime).userId(userId).username(username)
					.url(url).className(methodSignature.getDeclaringTypeName())
					.methodName(methodSignature.getName())
					.requestParams(requestParams).userIp(userIp)
					.area(regionSearcher.getAddress(userIp));
			result = proceedingJoinPoint.proceed();
			String endTime = DateUtil.now();
			RequestInfo requestInfo = requestInfoBuilder.result(result)
					.endTime(endTime)
					.timeCost(System.currentTimeMillis() - start).build();
			String type = attributes.getResponse().getHeader("Content-Type");
			if (!(StrUtil.isNotBlank(type) && type
					.contains("application/octet-stream"))) {
				PointUtil.info("1", "request-normal-info",
						JSONObject.toJSONString(requestInfo));
			}
			return result;
		} else {
			result = proceedingJoinPoint.proceed();
		}
		return result;
	}

	@AfterThrowing(pointcut = "requestServer()", throwing = "e")
	public void doAfterThrow(JoinPoint joinPoint, RuntimeException e) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		MethodSignature methodSignature = (MethodSignature) joinPoint
				.getSignature();
		HttpServletRequest request = attributes.getRequest();
		String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
		String username = request.getHeader(SecurityConstants.USER_HEADER);
		String url = request.getRequestURI().toString();
		String startTime = DateUtil.now();
		String userIp = AddrUtil.getRemoteAddr(request);
		Map<String, Object> requestParams = getRequestParamsByProceedJoinPoint(joinPoint);
		RequestInfo requestInfo = RequestInfo.builder().serverIp(ipAddress)
				.applicationName(applicationName).startTime(startTime)
				.userId(userId).username(username).url(url)
				.className(methodSignature.getDeclaringTypeName())
				.methodName(methodSignature.getName())
				.requestParams(requestParams).userIp(userIp)
				.area(regionSearcher.getAddress(userIp)).exception(e).build();
		PointUtil.info("2", "request-error-info",
				JSONObject.toJSONString(requestInfo));
	}

	private Map<String, Object> getRequestParamsByProceedJoinPoint(
			ProceedingJoinPoint proceedingJoinPoint) {
		String[] paramNames = ((MethodSignature) proceedingJoinPoint
				.getSignature()).getParameterNames();
		Object[] paramValues = proceedingJoinPoint.getArgs();
		return buildRequestParam(paramNames, paramValues);
	}

	private Map<String, Object> getRequestParamsByProceedJoinPoint(
			JoinPoint joinPoint) {
		String[] paramNames = ((MethodSignature) joinPoint.getSignature())
				.getParameterNames();
		Object[] paramValues = joinPoint.getArgs();
		return buildRequestParam(paramNames, paramValues);
	}

	private Map<String, Object> buildRequestParam(String[] paramNames,
			Object[] paramValues) {
		Map<String, Object> requestParams = MapUtil.newHashMap();
		for (int i = 0; i < paramNames.length; i++) {
			Object value = paramValues[i];
			if (value instanceof MultipartFile) {
				MultipartFile file = (MultipartFile) value;
				value = file.getOriginalFilename();
			}
			if (value instanceof ServletRequest) {
				value = "servletRequest";
			}
			if (value instanceof ServletResponse) {
				value = "servletResponse";
			}
			if (value instanceof HandlerMethod) {
				value = "handlerMethod";
			}
			if (value instanceof HttpRequestHandler) {
				value = "httpRequestHandler";
			}
			if (value instanceof AbstractBindingResult) {
				value = "bindingResult";
			}
			if (value instanceof DataBinder) {
				value = "dataBinder";
			}
			if (value instanceof ModelAndView) {
				value = "modelAndView";
			}
			requestParams.put(paramNames[i], value);
		}

		return requestParams;
	}
}
