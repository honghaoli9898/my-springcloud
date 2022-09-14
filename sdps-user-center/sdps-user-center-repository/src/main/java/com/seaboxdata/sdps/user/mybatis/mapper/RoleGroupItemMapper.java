package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.role.RoleDto;
import com.seaboxdata.sdps.user.mybatis.model.RoleGroupItem;

public interface RoleGroupItemMapper extends SuperMapper<RoleGroupItem> {
	public List<RoleDto> findUsersGroupByRoleIds(
			@Param("roleIds") Collection<Long> collection);

	public void insertBatch(@Param("items") Collection<RoleGroupItem> items);
}