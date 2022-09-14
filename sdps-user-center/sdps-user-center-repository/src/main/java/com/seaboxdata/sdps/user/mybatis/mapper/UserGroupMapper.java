package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.Collection;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;

public interface UserGroupMapper extends SuperMapper<UserGroup> {
	public void insertBatch(
			@Param("userGroups") Collection<UserGroup> userGroups);
}