package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;

public interface IUserGroupService extends ISuperService<UserGroup> {
	public boolean deletUserGroupItems(List<UserGroup> userGroups);
	
	public int insertBatch(List<UserGroup> list);

}
