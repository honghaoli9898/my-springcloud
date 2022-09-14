package com.seaboxdata.sdps.provider.gateway.feign.fallback;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import cn.hutool.core.collection.CollectionUtil;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;

import feign.hystrix.FallbackFactory;

/**
 * menuService降级工场
 */
@Slf4j
@Component
public class MenuServiceFallbackFactory implements FallbackFactory<MenuService> {
	@Override
	public MenuService create(Throwable throwable) {
		return new MenuService() {

			@Override
			public List<SysMenu> findByRoleCodes(String roleCodes) {
				log.error("调用findByRoleCodes异常：{}", roleCodes, throwable);
				return CollectionUtil.newArrayList();
			}

			@Override
			public Result<Map<String, String>> serverLogin(String clusterId,
					String type, String username, Boolean isCache) {
				return Result.failed("操作失败");
			}

		};

	}
}
