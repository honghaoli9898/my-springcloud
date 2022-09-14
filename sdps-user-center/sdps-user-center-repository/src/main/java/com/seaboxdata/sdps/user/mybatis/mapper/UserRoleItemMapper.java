package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;

public interface UserRoleItemMapper extends SuperMapper<UserRoleItem> {

	public List<SysRole> findRolesByUserId(@Param("userId") Long userId,
			@Param("type") String type);

	public int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

}