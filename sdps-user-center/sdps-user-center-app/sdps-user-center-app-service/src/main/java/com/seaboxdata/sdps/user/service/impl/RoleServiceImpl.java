package com.seaboxdata.sdps.user.service.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IRoleService;
import com.seaboxdata.sdps.user.mybatis.mapper.RoleGroupItemMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysRoleMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.user.mybatis.model.RoleGroupItem;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;
import com.seaboxdata.sdps.user.mybatis.vo.role.RoleRequest;
import com.seaboxdata.sdps.user.vo.role.PageRoleRequest;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Slf4j
@Service
public class RoleServiceImpl extends SuperServiceImpl<SysRoleMapper, SysRole>
		implements IRoleService {
	@Autowired
	private RoleGroupItemMapper roleGroupItemMapper;
	@Autowired
	private UserRoleItemMapper userRoleItemMapper;

	@Override
	public PageResult<SysRole> findRoles(PageRoleRequest request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SysRole> roles = baseMapper.findRolesByExample(request.getParam());
		return PageResult.<SysRole> builder().data(roles.getResult())
				.msg("操作成功").code(0).count(roles.getTotal()).build();
	}

	@Override
	public List<SysRole> findRoleByUserIds(List<Long> userIds, String type) {
		return this.baseMapper.findRoleByUserIds(userIds, type);
	}

	@Override
	@Transactional
	public boolean deleteRoleInfoByIds(List<Long> roleIds) {
		removeByIds(roleIds);
		roleGroupItemMapper.delete(new UpdateWrapper<RoleGroupItem>().in(
				"role_id", roleIds));
		userRoleItemMapper.delete(new UpdateWrapper<UserRoleItem>().in(
				"role_id", roleIds));
		return true;
	}

	@Override
	public PageInfo<SysUser> findUserByRoleId(PageRoleRequest request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		return new PageInfo<SysUser>(baseMapper.findUserByRoleId(request
				.getParam().getId(), request.getParam().getName()));
	}

	@Override
	public List<SysRole> findRoleList(RoleRequest request) {
		return baseMapper.findRolesByExample(request);
	}

}