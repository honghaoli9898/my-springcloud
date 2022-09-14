package com.seabox.sdps.dynamicroute.controller;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.seabox.sdps.dynamicroute.config.RedisConfig;
import com.seabox.sdps.dynamicroute.entity.DynamicVersion;
import com.seabox.sdps.dynamicroute.service.IDynamicVersionService;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;

@Slf4j
@RestController
@RequestMapping("/version")
public class DynamicVersionController {

	@Autowired
	private IDynamicVersionService dynamicVersionService;
	@Autowired
	private StringRedisTemplate redisTemplate;

	@RequestMapping(value = "/add")
	public Result add() {
		try {
			DynamicVersion version = new DynamicVersion();
			dynamicVersionService.add(version);
			return Result.succeed("操作成功");
		} catch (Exception e) {
			log.error("发布路由版本失败",e);
			throw new BusinessException("发布路由失败");
		}
	}

	// 获取最后一次发布的版本号
	@RequestMapping(value = "/lastVersion", method = RequestMethod.GET)
	public Long getLastVersion() {
		Long versionId = 0L;
		String result = redisTemplate.opsForValue().get(RedisConfig.versionKey);
		if (Objects.nonNull(result)||!StringUtils.isEmpty(result)) {
			log.info("返回 redis 中的版本信息......");
			versionId = Long.valueOf(result);
		} else {
			log.info("返回 mysql 中的版本信息......");
			versionId = dynamicVersionService.getLastVersion();
			redisTemplate.opsForValue().set(RedisConfig.versionKey,
					String.valueOf(versionId));
		}
		return versionId;
	}

	// 打开发布版本列表页面
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result listAll(ModelMap map) {
		return Result.succeed(dynamicVersionService.listAll(), "操作成功");
	}
}
