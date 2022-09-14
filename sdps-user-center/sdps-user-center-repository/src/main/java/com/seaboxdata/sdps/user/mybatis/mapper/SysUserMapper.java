package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SysGroup;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserDto;
import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;

public interface SysUserMapper extends SuperMapper<SysUser> {
	public Page<SysUser> findUsersInfo(@Param("user") UserRequest user);

	public Page<SysUser> findUsersByExample(@Param("user") UserRequest user);

	public List<SysUser> findUserInfo(@Param("username") String username);

	public List<SysGroup> findGroupByUser(@Param("id") Long id,
			@Param("groupName") String groupName);

	public List<SysUser> findUserRolesByUserIds(
			@Param("userIds") List<Long> userIds);

	public List<UserDto> selectRolesByUserId(@Param("userId") Long userId);
}