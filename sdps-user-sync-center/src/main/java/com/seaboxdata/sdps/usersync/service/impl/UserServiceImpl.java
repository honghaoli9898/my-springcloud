package com.seaboxdata.sdps.usersync.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysUserMapper;
import com.seaboxdata.sdps.usersync.mapper.UserGroupMapper;
import com.seaboxdata.sdps.usersync.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.usersync.model.UserGroup;
import com.seaboxdata.sdps.usersync.model.UserRoleItem;
import com.seaboxdata.sdps.usersync.service.IUserService;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Service
public class UserServiceImpl extends SuperServiceImpl<SysUserMapper, SysUser>
		implements IUserService {
	@Autowired
	private UserRoleItemMapper userRoleItemMapper;

	@Autowired
	private UserGroupMapper userGroupMapper;

	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	private SysUserMapper sysUserMapper;

	@Override
	@Transactional
	public void deleteUserAllInfo(Long id) {
		List<SysUser> sysUsers = this.list(new QueryWrapper<SysUser>().select(
				"username", "id").eq("id", id));
		List<String> usernames = sysUsers.stream().map(SysUser::getUsername)
				.collect(Collectors.toList());
		removeById(id);
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
				"user_id", id));
		userGroupMapper.delete(new QueryWrapper<UserGroup>().eq("user_id", id));
		serverInfoMapper.delete(new UpdateWrapper<SdpsServerInfo>()
				.eq("server_id", 0).eq("type", ServerTypeEnum.C.name())
				.in("user", usernames));
	}

	@Override
	@Transactional
	public void updateUser(Long userId, Boolean enabled, String status) {
		SysUser sysUser = new SysUser();
		sysUser.setEnabled(enabled);
		sysUser.setSyncUserStatus(status);
		sysUserMapper.update(sysUser,
				new UpdateWrapper<SysUser>().eq("id", userId));
	}

	@Override
	public void updateUsers(List<Long> userIds, Boolean enabled, String status) {
		SysUser sysUser = new SysUser();
		sysUser.setEnabled(enabled);
		sysUser.setSyncUserStatus(status);
		sysUserMapper.update(sysUser,
				new UpdateWrapper<SysUser>().in("id", userIds));
	}
}