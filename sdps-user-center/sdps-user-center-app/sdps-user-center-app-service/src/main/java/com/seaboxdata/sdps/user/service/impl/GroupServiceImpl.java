package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SysGroup;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IGroupService;
import com.seaboxdata.sdps.user.mybatis.dto.role.RoleDto;
import com.seaboxdata.sdps.user.mybatis.mapper.RoleGroupItemMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysGroupMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserGroupMapper;
import com.seaboxdata.sdps.user.mybatis.model.RoleGroupItem;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;
import com.seaboxdata.sdps.user.mybatis.vo.group.GroupRequest;
import com.seaboxdata.sdps.user.vo.group.PageGroupRequest;
import com.seaboxdata.sdps.user.vo.role.RoleVo;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Slf4j
@Service
public class GroupServiceImpl extends
		SuperServiceImpl<SysGroupMapper, SysGroup> implements IGroupService {
	@Autowired
	private RoleGroupItemMapper roleGroupItemMapper;

	@Autowired
	private UserGroupMapper userGroupMapper;

	@Override
	public PageResult<SysGroup> selectGroup(PageGroupRequest request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SysGroup> groups = this.baseMapper.selectGroupByExample(request
				.getParam());
		if (CollUtil.isEmpty(groups.getResult())) {
			return PageResult.<SysGroup> builder().data(groups.getResult())
					.msg("操作成功").code(0).count(groups.getTotal()).build();
		}
		List<Long> groupIds = groups.getResult().stream().map(SysGroup::getId)
				.collect(Collectors.toList());
		List<UserGroup> userGroups = userGroupMapper
				.selectList(new QueryWrapper<UserGroup>().select(
						"distinct group_id", "user_id")
						.in("group_id", groupIds));
		Map<Long, List<UserGroup>> userGroupMap = userGroups.stream().collect(
				Collectors.groupingBy(UserGroup::getGroupId));
		groups.forEach(group -> group.setMembers(CollUtil.isEmpty(userGroupMap
				.get(group.getId())) ? 0 : userGroupMap.get(group.getId())
				.size()));
		return PageResult.<SysGroup> builder().data(groups.getResult())
				.msg("操作成功").code(0).count(groups.getTotal()).build();
	}

	@Override
	@Transactional
	public boolean deleteGroupInfoByIds(List<Long> groupIds) {
		removeByIds(groupIds);
		roleGroupItemMapper.delete(new UpdateWrapper<RoleGroupItem>().in(
				"group_id", groupIds));
		userGroupMapper.delete(new UpdateWrapper<UserGroup>().in("group_id",
				groupIds));
		return true;
	}

	@Override
	public List<GroupRequest> findGroupByUserIds(List<Long> userIds) {
		List<GroupRequest> groupRequests = this.baseMapper
				.findGroupByUserId(userIds);
		List<Long> ids = groupRequests.stream().map(GroupRequest::getId)
				.collect(Collectors.toList());
		if (CollUtil.isNotEmpty(ids)) {

			GroupRequest request = new GroupRequest();
			request.setIds(ids);
			List<UserGroup> userGroups = userGroupMapper
					.selectList(new QueryWrapper<UserGroup>().select(
							"distinct group_id", "user_id").in("group_id", ids));
			Map<Long, List<UserGroup>> userGroupMap = userGroups.stream()
					.collect(Collectors.groupingBy(UserGroup::getGroupId));
			groupRequests.forEach(group -> group.setMembers(CollUtil
					.isEmpty(userGroupMap.get(group.getId())) ? 0
					: userGroupMap.get(group.getId()).size()));
		}
		return groupRequests;
	}

	@Override
	public RoleVo findRoleByGroupId(PageGroupRequest request) {
		List<SysRole> systemRoles = this.baseMapper
				.findSysRoleByGroupId(request.getParam().getId());
		PageHelper.startPage(request.getPage(), request.getSize());
		List<RoleDto> itemRoles = this.baseMapper.findItemRoleByGroupId(request
				.getParam().getId());
		itemRoles.forEach(obj -> {
			String roleIds = obj.getRoleIds();
			String roleNames = obj.getRoleNames();
			List<SysRole> roles = CollUtil.newArrayList();
			String[] roleIdArr = roleIds.split(",");
			String[] roleNameArr = roleNames.split(",");
			for (int i = 0; i < roleIdArr.length; i++) {
				SysRole sysRole = new SysRole();
				sysRole.setId(Long.valueOf(roleIdArr[i]));
				sysRole.setName(roleNameArr[i]);
				roles.add(sysRole);
			}
			obj.setRoleIds(null);
			obj.setRoleNames(null);
			// obj.setRoles(roles);
			});
		PageInfo<RoleDto> pageItemRoles = new PageInfo<RoleDto>(itemRoles);
		RoleVo result = new RoleVo();
		// result.setSystemRoles(systemRoles);
		result.setPageItemRoles(pageItemRoles);
		return result;
	}

	@Override
	public PageResult<SysUser> findUserByGroupId(PageGroupRequest request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SysUser> results = baseMapper.findUserByGroupIds(CollUtil
				.newArrayList(request.getParam().getId()));
		return PageResult.<SysUser> builder().data(results.getResult())
				.msg("操作成功").code(0).count(results.getTotal()).build();
	}

	@Override
	@Transactional
	public void updateUserGroupMember(GroupRequest groupRequest) {
		// 先删除之前的映射
		userGroupMapper.delete(new QueryWrapper<UserGroup>().eq("group_id",
				groupRequest.getId()));
		if (CollUtil.isNotEmpty(groupRequest.getIds())) {
			List<UserGroup> userGroups = groupRequest.getIds().stream()
					.distinct().map(userId -> {
						UserGroup userGroup = new UserGroup();
						userGroup.setGroupId(groupRequest.getId());
						userGroup.setUserId(userId);
						return userGroup;
					}).collect(Collectors.toList());
			userGroupMapper.insertBatch(userGroups);
		}
	}

	@Override
	@Transactional
	public void insertItemRoleGroup(GroupRequest groupRequest) {
		long count = roleGroupItemMapper
				.selectCount(new QueryWrapper<RoleGroupItem>().eq("group_id",
						groupRequest.getId()).eq("item_id",
						groupRequest.getItemId()));
		if (count != 0L) {
			return;
		}
		List<RoleGroupItem> results = groupRequest.getIds().stream()
				.map(id -> {
					RoleGroupItem obj = new RoleGroupItem();
					obj.setGroupId(groupRequest.getId());
					obj.setItemId(groupRequest.getItemId());
					obj.setRoleId(id);
					return obj;
				}).collect(Collectors.toList());
		roleGroupItemMapper.insertBatch(results);
	}

	@Override
	public List<SysGroup> selectGroupByExample(GroupRequest request) {
		return this.baseMapper.selectGroupByExample(request);
	}

	@Override
	public List<SysUser> findExistMemberByGroup(GroupRequest request) {
		List<SysUser> results = this.baseMapper.findExistMemberByGroup(request);
		return results;
	}

}