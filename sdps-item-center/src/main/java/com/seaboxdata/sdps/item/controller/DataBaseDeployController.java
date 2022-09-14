package com.seaboxdata.sdps.item.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.dto.database.DatabaseDto;
import com.seaboxdata.sdps.item.mapper.SdpsDatabaseMapper;
import com.seaboxdata.sdps.item.model.SdpsDatabase;
import com.seaboxdata.sdps.item.service.IDatabaseService;
import com.seaboxdata.sdps.item.vo.database.DatabaseRequest;

@RestController
@RequestMapping("/MC20")
public class DataBaseDeployController {
	@Autowired
	private IDatabaseService databaseService;

	@Autowired
	private SdpsDatabaseMapper sdpsDatabaseMapper;

	@PostMapping("/MC2001")
	public Result saveAndCreateDatabase(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody SdpsDatabase database) {
		if (StrUtil.isBlank(database.getName())
				|| Objects.isNull(database.getTypeId())
				|| Objects.isNull(database.getClusterId())
				|| Objects.isNull(database.getItemId())) {
			return Result.failed("数据库名称、库类型、关联项目、关联集群不能为空");
		}
		database.setCreateUser(sysUser.getUsername());
		database.setUpdateUser(sysUser.getUsername());
		database.setCreateUserId(sysUser.getId());
		database.setUpdateUserId(sysUser.getId());
		databaseService.saveAndCreateDatabase(sysUser.getId(), database);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC2002")
	public Result deleteDatabaseByIds(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody DatabaseRequest request) {
		if (CollUtil.isEmpty(request.getIds())) {
			return Result.succeed("操作成功");
		}
		databaseService.deleteDatabaseByIds(sysUser.getId(), request);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC2003")
	public PageResult<DatabaseDto> selectDatabasePage(
			@Validated @RequestBody PageRequest<DatabaseRequest> request) {
		PageResult<DatabaseDto> results = databaseService
				.findDatabases(request.getPage(),request.getSize(),request.getParam());
		return results;
	}

	@GetMapping("/selectDatabaseList")
	public String selectDatabaseList() {
		PageRequest<DatabaseRequest> pageDatabaseRequest = new PageRequest<DatabaseRequest>();
		pageDatabaseRequest.setPage(0);
		pageDatabaseRequest.setSize(Integer.MAX_VALUE);
		Page<DatabaseDto> databaseDtos = sdpsDatabaseMapper
				.findDatabasesByExample(pageDatabaseRequest.getParam());
		return JSON.toJSONString(databaseDtos);
	}

	@PostMapping("/MC2004")
	public Result selectDatabase(@RequestBody DatabaseRequest request) {
		List<DatabaseDto> results = databaseService.selectDatabase(request);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 根据数据库名查询项目信息
	 *
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/MC2005")
	public Result getItemInfoByDatabaseName(
			@RequestParam(required = false) String names) {
		Set<String> nameSet = null;
		if (StrUtil.isNotBlank(names)) {
			nameSet = (Set<String>) Convert.toCollection(HashSet.class,
					String.class, names);
		}
		Map<String, String> result = databaseService
				.getItemInfoByDatabaseName(nameSet);
		return Result.succeed(result, "操作成功");
	}
}
