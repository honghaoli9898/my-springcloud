package com.seaboxdata.sdps.common.core.aspect;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.seaboxdata.sdps.common.core.annotation.Limit;
import com.seaboxdata.sdps.common.core.model.Result;

@Slf4j
@Aspect
@ConditionalOnClass({ HttpServletRequest.class, RequestContextHolder.class })
public class LimitAop {
	/**
	 * 不同的接口，不同的流量控制 map的key为 Limiter.key
	 */
	private final Map<String, RateLimiter> limitMap = Maps.newConcurrentMap();

	@Around("@annotation(com.seaboxdata.sdps.common.core.annotation.Limit)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		// 拿limit的注解
		Limit limit = method.getAnnotation(Limit.class);
		if (limit != null) {
			// key作用：不同的接口，不同的流量控制
			String key = limit.key();
			RateLimiter rateLimiter = null;
			// 验证缓存是否有命中key
			if (!limitMap.containsKey(key)) {
				// 创建令牌桶
				rateLimiter = RateLimiter.create(limit.permitsPerSecond());
				limitMap.put(key, rateLimiter);
				log.info("新建了令牌桶={}，容量={}", key, limit.permitsPerSecond());
			}
			rateLimiter = limitMap.get(key);
			// 拿令牌
			boolean acquire = rateLimiter.tryAcquire(limit.timeout(),
					limit.timeunit());
			// 拿不到命令，直接返回异常提示
			if (!acquire) {
				log.debug("令牌桶={}，获取令牌失败", key);
				this.responseFail(limit.msg());
				return null;
			}
		}
		return joinPoint.proceed();
	}

	/**
	 * 直接向前端抛出异常
	 * 
	 * @param msg
	 *            提示信息
	 */
	private void responseFail(String msg) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getResponse();
		Result<Object> resultData = Result.failed(msg);
		ResponseEntity.status(HttpStatus.OK).body(Result.succeed(resultData));
	}
}