package com.seaboxdata.sdps.item.aspect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;

import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.item.anotation.DataPermission;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.Request;

/**
 * 审计日志切面
 *
 */
@Aspect
@Component
public class DataPermissionAspect {
	@Autowired
	private IItemService itemService;

	@Before("@within(dataPermission) || @annotation(dataPermission)")
	public void beforeMethod(JoinPoint joinPoint, DataPermission dataPermission) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		MethodSignature methodSignature = (MethodSignature) joinPoint
				.getSignature();
		HttpServletRequest request = attributes.getRequest();
		String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
		String userName = request.getHeader(SecurityConstants.USER_HEADER);
		if (Objects.equals(userName, "admin")) {
			return;
		}
		List<ItemDto> itemDtos = itemService.selectItemByUser(
				Long.valueOf(userId), false);
		Set<Long> itemIds = itemDtos.stream().map(ItemDto::getId)
				.collect(Collectors.toSet());
		if (dataPermission == null) {
			// 获取类上的注解
			dataPermission = joinPoint.getTarget().getClass()
					.getDeclaredAnnotation(DataPermission.class);
		}
		String sql = getSql(dataPermission, itemIds);
		Map<String, Object> requestParams = getRequestParamsByProceedJoinPoint(joinPoint);
		requestParams.forEach((k, v) -> {
			if (v instanceof Request) {
				Request req = (Request) v;
				req.setDataPermissionSql(sql);
			}

		});
	}

	private String getSql(DataPermission dataPermission,
			Collection<Long> itemIds) {
		String joinName = dataPermission.joinName();

		StringBuffer sb = new StringBuffer();
		sb.append(joinName).append(" in(");
		itemIds.forEach(id -> {
			sb.append("'").append(id).append("',");
		});
		if (CollUtil.isNotEmpty(itemIds)) {
			sb.deleteCharAt(sb.length() - 1);
		} else {
			sb.append("''");
		}
		sb.append(")");
		return sb.toString();
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
			requestParams.put(paramNames[i], value);
		}

		return requestParams;
	}

}
