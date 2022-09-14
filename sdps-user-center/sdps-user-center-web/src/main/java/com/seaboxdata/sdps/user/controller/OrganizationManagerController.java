package com.seaboxdata.sdps.user.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.user.api.IOrganizationService;
import com.seaboxdata.sdps.user.mybatis.model.SysOrganization;
import com.seaboxdata.sdps.user.service.impl.OrganizationServiceImpl;

@Slf4j
@RestController
@RequestMapping("/UC80")
public class OrganizationManagerController {
	@Autowired
	private IOrganizationService organizationService;

	/**
	 * 查询组织树
	 * 
	 * @return
	 */
	@GetMapping("/UC8001")
	public Result findAllOrgan() {
		List<SysOrganization> results = organizationService
				.findOrganizations(new SysOrganization());
		return Result.succeed(OrganizationServiceImpl.treeBuilder(results),
				"操作成功");
	}

	/**
	 * 增加组织
	 * 
	 * @param organization
	 * @return
	 */
	@PostMapping("/UC8002")
	public Result insertOrgan(@RequestBody SysOrganization organization) {
		organizationService.save(organization);
		return Result.succeed("操作成功");
	}

	/**
	 * 删除组织
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/UC8003/{id}")
	public Result deleteOrganById(@PathVariable Long id) {
		organizationService.remove(new QueryWrapper<SysOrganization>()
				.eq("id", id).or().eq("parent_id", id));
		return Result.succeed("操作成功");
	}

	@PostMapping("/UC8004")
	public Result updateOrganById(@RequestBody SysOrganization organization) {
		organizationService.updateById(organization);
		return Result.succeed("操作成功");
	}

	/**
	 * 根据id查找组织数
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/UC8005/{id}")
	public Result findOrganTreeById(@PathVariable Long id) {
		return Result.succeed(OrganizationServiceImpl
				.treeBuilder(organizationService.findOrganizationById(id)),
				"操作成功");
	}
}
