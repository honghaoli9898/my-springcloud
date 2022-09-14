package com.seaboxdata.sdps.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IUserRoleItemService;
import com.seaboxdata.sdps.user.mybatis.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Service
public class UserRoleItemServiceImpl extends
		SuperServiceImpl<UserRoleItemMapper, UserRoleItem> implements
		IUserRoleItemService {
	@Override
	public List<SysRole> findRolesByUserId(Long userId, String type) {
		return this.baseMapper.findRolesByUserId(userId, type);
	}

	@Override
	public int deleteUserRole(Long userId, Long roleId) {
		return this.baseMapper.deleteUserRole(userId, roleId);
	}

	@Override
	public int insertBatch(List<UserRoleItem> list) {
		return this.baseMapper.insertBatchSomeColumn(list);
	}
}