package com.seaboxdata.sdps.provider.gateway.thread;

import java.util.Map;
import java.util.concurrent.Callable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.map.MapUtil;

import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;
import com.seaboxdata.sdps.provider.gateway.utils.ServerLoginUtil;

@Slf4j
@Getter
@Setter
public class ServerLoginCallable implements Callable<Boolean> {
	private MenuService menuService;
	private String clusterId;
	private String serverType;
	private String username;
	private RedisRepository redisRepository;

	public ServerLoginCallable(MenuService menuService,
			RedisRepository redisRepository, String clusterId,
			String serverType, String username) {
		this.redisRepository = redisRepository;
		this.menuService = menuService;
		this.clusterId = clusterId;
		this.serverType = serverType;
		this.username = username;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			Map<String, String> certMap = ServerLoginUtil.login(menuService,
					clusterId, serverType, username);
			if (MapUtil.isEmpty(certMap)) {
				return false;
			}
			ServerLoginUtil.saveLoginCert(redisRepository, certMap, clusterId,
					serverType, username);

		} catch (Exception e) {
			log.error("组件[clusterId={},serverType={},username={}],登录失败",
					clusterId, serverType, username, e);
			return false;
		}
		return true;
	}

}
