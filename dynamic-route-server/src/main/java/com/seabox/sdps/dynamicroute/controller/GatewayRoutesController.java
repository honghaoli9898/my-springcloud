package com.seabox.sdps.dynamicroute.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.seabox.sdps.dynamicroute.config.RedisConfig;
import com.seabox.sdps.dynamicroute.entity.GatewayRoutes;
import com.seabox.sdps.dynamicroute.service.IRoutesService;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;

@Slf4j
@RestController
@RequestMapping("/gateway-routes")
public class GatewayRoutesController {

	@Autowired
	private IRoutesService routesService;
	@Autowired
	private StringRedisTemplate redisTemplate;

	/**
	 * 获取所有动态路由信息
	 * 
	 * @return
	 */
	@RequestMapping("/routes")
	public Result<String> getRouteDefinitions() {
		try {
			String result = redisTemplate.opsForValue().get(
					RedisConfig.routeKey);
			if (!StringUtils.isEmpty(result)) {
				log.info("返回 redis 中的路由信息......");
			} else {
				log.info("返回 mysql 中的路由信息......");
				result = JSON.toJSONString(routesService.getRouteDefinitions());
				// 再set到redis
				redisTemplate.opsForValue().set(RedisConfig.routeKey, result);
			}
			log.info("路由信息：" + result);
			return Result.succeed(result, "操作成功");
		} catch (Exception e) {
			log.error("获取路由信息失败", e);
			throw new BusinessException("获取路由失败");
		}
	}

	// 添加路由信息
	@PostMapping(value = "/add")
	public Result add(@LoginUser SysUser sysUser,
			@RequestBody GatewayRoutes route) {
		try {
			if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
					CommonConstant.ADMIN_USER_NAME)) {
				return Result.failed("您没有该接口权限");
			}
			return routesService.add(route) > 0 ? Result.succeed("操作成功")
					: Result.failed("操作失败");
		} catch (Exception e) {
			log.error("添加路由失败", e);
			throw new BusinessException("添加路由失败");
		}

	}

	// 添加路由信息
	@PostMapping(value = "/edit")
	public Result edit(@LoginUser SysUser sysUser,
			@RequestBody GatewayRoutes route) {
		try {
			if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
					CommonConstant.ADMIN_USER_NAME)) {
				return Result.failed("您没有该接口权限");
			}
			return routesService.update(route) > 0 ? Result.succeed("操作成功")
					: Result.failed("操作失败");
		} catch (Exception e) {
			log.error("更新路由失败", e);
			throw new BusinessException("更新路由失败");
		}
	}

	// 打开路由列表
	@GetMapping("/list")
	public Result list(@LoginUser SysUser sysUser) {
		if (StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			GatewayRoutes route = new GatewayRoutes();
			route.setIsDel(false);
			return Result.succeed(routesService.getRoutes(route), "操作成功");
		}
		return Result.succeed(CollUtil.newArrayList(), "操作成功");
	}

	@PostMapping("/delete/{id}")
	public Result delete(@LoginUser SysUser sysUser, @PathVariable("id") Long id) {
		if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			return Result.failed("您没有该接口权限");
		}
		routesService.delete(id, true);
		return Result.succeed("操作成功");
	}
}
