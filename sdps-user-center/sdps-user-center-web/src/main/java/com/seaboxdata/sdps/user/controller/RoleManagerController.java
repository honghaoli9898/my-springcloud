package com.seaboxdata.sdps.user.controller;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.context.TenantContextHolder;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IRoleService;
import com.seaboxdata.sdps.user.enums.TypeEnum;
import com.seaboxdata.sdps.user.mybatis.vo.role.RoleRequest;
import com.seaboxdata.sdps.user.vo.role.PageRoleRequest;

@Slf4j
@RestController
@RequestMapping("/UC40")
public class RoleManagerController {
	@Autowired
	private IRoleService roleService;

	@PostMapping("/UC4001")
	public PageResult<SysRole> findRoles(
			@Validated @RequestBody PageRoleRequest request) {
		PageResult<SysRole> results = roleService.findRoles(request);
		return results;
	}

	@PostMapping("/UC4002")
	public Result insertRole(@RequestBody SysRole role) {
		if (Objects.isNull(role) || StrUtil.isBlank(role.getName())
				|| StrUtil.isBlank(role.getCode())) {
			return Result.failed("角色名称或code不能为空不能为空");
		}
		String tenant = TenantContextHolder.getTenant();
		if (roleService.count(new QueryWrapper<SysRole>().eq("tenant_id", tenant).eq("code",
				role.getCode())) > 0) {
			return Result.failed("已存在相同code的角色");
		}
		role.setTenantId(tenant);
		role.setType(TypeEnum.S.name());
		roleService.save(role);
		return Result.succeed("操作成功");
	}

	@PostMapping("/UC4003")
	public Result updateRole(@RequestBody SysRole role) {
		if (Objects.isNull(role) || Objects.isNull(role.getId())) {
			return Result.failed("id为必传项");
		}
		if (StrUtil.isNotBlank(role.getCode())) {
			return Result.failed("角色代码不能更新");
		}
		roleService.updateById(role);
		return Result.succeed("操作成功");
	}

	/**
	 * 根据用户ids查询角色
	 * 
	 * @param roleIds
	 * @return
	 */
	@PostMapping("/UC4004")
	public Result findRoleByUserId(@RequestBody List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return Result.succeed("操作成功");
		}
		List<SysRole> results = roleService.findRoleByUserIds(ids, null);
		return Result.succeed(results, "操作成功");
	}

	@PostMapping("/UC4005")
	public Result deleteRoleByIds(@RequestBody List<Long> roleIds) {
		if (CollUtil.isEmpty(roleIds)) {
			return Result.succeed("操作成功");
		}
		roleService.deleteRoleInfoByIds(roleIds);
		return Result.succeed("操作成功");
	}

	/**
	 * 根据角色id查询关联用户
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/UC4006")
	public Result findUserByRoleId(@RequestBody PageRoleRequest request) {
		try {
			PageInfo<SysUser> results = roleService.findUserByRoleId(request);
			return Result.succeed(results, "操作成功");
		} catch (Exception ex) {
			log.error("role-select-error", ex);
			return Result.failed("操作失败");
		}
	}

	/**
	 * 角色列表查询
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/UC4007")
	public Result findRoleList(@RequestBody RoleRequest request) {
		try {
			List<SysRole> results = roleService.findRoleList(request);
			return Result.succeed(results, "操作成功");
		} catch (Exception ex) {
			log.error("role-find-error", ex);
			return Result.failed("操作失败");
		}
	}

}
