package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SysGroup;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.role.RoleDto;
import com.seaboxdata.sdps.user.mybatis.vo.group.GroupRequest;

public interface SysGroupMapper extends SuperMapper<SysGroup> {
	public Page<SysGroup> selectGroupByExample(@Param("group") GroupRequest group);

	public boolean updateGroupByExample(@Param("group") SysGroup group);

	public List<GroupRequest> findGroupByUserId(
			@Param("userIds") List<Long> userIds);

	public List<SysRole> findSysRoleByGroupId(@Param("groupId") Long groupId);

	public List<RoleDto> findItemRoleByGroupId(@Param("groupId") Long groupId);

	public Page<SysUser> findUserByGroupIds(
			@Param("groupIds") List<Long> groupIds);

	public List<SysUser> findExistMemberByGroup(@Param("group") GroupRequest group);
}