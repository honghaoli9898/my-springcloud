package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;

public interface IUserRoleItemService extends ISuperService<UserRoleItem> {

	public List<SysRole> findRolesByUserId(Long id, String type);

	public int deleteUserRole(Long userId, Long roleId);
	
	public int insertBatch(List<UserRoleItem> list);

}
