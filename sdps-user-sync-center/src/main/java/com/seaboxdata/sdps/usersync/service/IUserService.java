package com.seaboxdata.sdps.usersync.service;

import java.util.List;

import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;

public interface IUserService extends ISuperService<SysUser> {
	public void deleteUserAllInfo(Long id);
	
	public void updateUser(Long userId,Boolean enabled,String status);
	
	public void updateUsers(List<Long> userIds,Boolean enabled,String status);
}
