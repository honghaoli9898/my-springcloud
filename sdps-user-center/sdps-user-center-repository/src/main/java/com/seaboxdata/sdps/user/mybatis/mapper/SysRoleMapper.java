package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.vo.role.RoleRequest;


public interface SysRoleMapper extends SuperMapper<SysRole> {
	public Page<SysRole> findRolesByExample(@Param("role") RoleRequest sysRole);

	public List<SysRole> findRoleByUserIds(@Param("userIds") List<Long> userIds,@Param("type")String type);

	public List<SysUser> findUserByRoleId(@Param("roleId") Long roleId,
			@Param("username") String username);
}