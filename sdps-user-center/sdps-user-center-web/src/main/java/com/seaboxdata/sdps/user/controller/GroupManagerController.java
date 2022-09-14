package com.seaboxdata.sdps.user.controller;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysGroup;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IGroupService;
import com.seaboxdata.sdps.user.mybatis.vo.group.GroupRequest;
import com.seaboxdata.sdps.user.vo.group.PageGroupRequest;

@Slf4j
@RestController
@RequestMapping("/UC50")
public class GroupManagerController {
	@Autowired
	private IGroupService groupService;

	@PostMapping("/UC5001")
	public Result insertGroup(@RequestBody SysGroup group) {
		if (Objects.isNull(group) || StrUtil.isBlank(group.getName())
				|| StrUtil.isBlank(group.getCode())) {
			return Result.failed("组名称或组标识不能为空");
		}
		if(groupService.count(new QueryWrapper<SysGroup>().eq("code", group.getCode()))>0){
			return Result.failed("组标识已存在");
		}
		groupService.save(group);
		return Result.succeed("操作成功");
	}

	@PostMapping("/UC5002")
	public Result updateGroup(@RequestBody SysGroup group) {
		if (Objects.isNull(group) || Objects.isNull(group.getId())) {
			return Result.failed("id不能为空");
		}
		if (StrUtil.isNotBlank(group.getCode())) {
			return Result.failed("组标识不能被更新");
		}
		groupService.updateById(group);
		return Result.succeed("操作成功");
	}

	@PostMapping("/UC5003")
	public Result deleteGroupByIds(@RequestBody List<Long> groupIds) {
		if (CollUtil.isEmpty(groupIds)) {
			return Result.succeed("操作成功");
		}
		groupService.deleteGroupInfoByIds(groupIds);
		return Result.succeed("操作成功");
	}

	/**
	 * 查询用户组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/UC5004")
	public PageResult<SysGroup> selectGroup(
			@Validated @RequestBody PageGroupRequest request) {
		PageResult<SysGroup> results = groupService.selectGroup(request);
		return results;
	}

	/**
	 * 根据用户id查询用户组
	 * 
	 * @param userId
	 * @return
	 */

	@GetMapping("/UC5005/{userId}")
	public Result selectGroupByUserId(@PathVariable Long userId) {
		List<GroupRequest> results = groupService.findGroupByUserIds(CollUtil
				.newArrayList(userId));
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 根据用户组id查询关联用户
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/UC5007")
	public PageResult<SysUser> selectUserByGroupId(
			@Validated @RequestBody PageGroupRequest request) {
		if (Objects.isNull(request.getParam())
				|| Objects.isNull(request.getParam().getId())) {
			return PageResult.<SysUser> builder().msg("组id不能为空").code(1)
					.build();
		}
		PageResult<SysUser> results = groupService.findUserByGroupId(request);
		return results;
	}

	/**
	 * 更新组成员
	 * 
	 * @param groupRequest
	 * @return
	 */
	@PostMapping("/UC5008")
	public Result updateUserGroupMember(@RequestBody GroupRequest groupRequest) {
		if (Objects.isNull(groupRequest)
				|| Objects.isNull(groupRequest.getId())) {
			return Result.failed("组id必传");
		}
		groupService.updateUserGroupMember(groupRequest);
		return Result.succeed("操作成功");
	}

	/**
	 * 新增组项目角色
	 * 
	 * @param groupRequest
	 * @return
	 */
	@PostMapping("/UC5011")
	public Result insertItemRoleGroup(@RequestBody GroupRequest groupRequest) {
		try {
			groupService.insertItemRoleGroup(groupRequest);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("group-delete-error", ex);
			return Result.failed("操作失败");
		}
	}

	@PostMapping("/UC5012")
	public Result selectGroupByExample(@RequestBody(required=false) GroupRequest request) {
		List<SysGroup> result = groupService.selectGroupByExample(request);
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 查询用户组已选用户
	 */
	@PostMapping("/UC5014")
	public Result findExistMemberByGroup(@RequestBody GroupRequest request) {
		if (Objects.isNull(request) || Objects.isNull(request.getId())) {
			return Result.failed("id为必传项");
		}
		List<SysUser> result = groupService.findExistMemberByGroup(request);
		return Result.succeed(result, "操作成功");
	}
}
