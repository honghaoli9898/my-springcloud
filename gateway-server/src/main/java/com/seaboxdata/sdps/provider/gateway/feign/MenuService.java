package com.seaboxdata.sdps.provider.gateway.feign;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.provider.gateway.feign.fallback.MenuServiceFallbackFactory;

@FeignClient(name = ServiceNameConstants.USER_SERVICE, fallbackFactory = MenuServiceFallbackFactory.class, decode404 = true)
public interface MenuService {
	/**
	 * 角色菜单列表
	 * 
	 * @param roleCodes
	 */
	@GetMapping(value = "/menus/{roleCodes}")
	List<SysMenu> findByRoleCodes(@PathVariable("roleCodes") String roleCodes);

	/**
	 * 组件登录
	 * 
	 * @return
	 */
	@GetMapping(value = "/server/login")
	public Result<Map<String, String>> serverLogin(
			@RequestParam("clusterId") String clusterId,
			@RequestParam("type") String type,
			@RequestParam("username") String username,
			@RequestParam(value = "isCache") Boolean isCache);
}
