package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.RoleMenu;

public interface RoleMenuMapper extends SuperMapper<RoleMenu> {
	@Insert("insert into sys_role_menu(role_id, menu_id) values(#{roleId}, #{menuId})")
	int save(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

	int delete(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

	List<SysMenu> findMenusByRoleIds(@Param("roleIds") Set<Long> roleIds,
			@Param("type") Integer type);

	List<SysMenu> findMenusByRoleCodes(
			@Param("roleCodes") Set<String> roleCodes,
			@Param("type") Integer type);
}