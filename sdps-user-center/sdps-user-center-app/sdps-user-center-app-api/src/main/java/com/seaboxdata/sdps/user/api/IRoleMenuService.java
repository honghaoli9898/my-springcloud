package com.seaboxdata.sdps.user.api;

import java.util.List;
import java.util.Set;

import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.RoleMenu;

/**
 * @author zlt
 */
public interface IRoleMenuService extends ISuperService<RoleMenu> {
	int save(Long roleId, Long menuId);

	int delete(Long roleId, Long menuId);

	List<SysMenu> findMenusByRoleIds(Set<Long> roleIds, Integer type);

	List<SysMenu> findMenusByRoleCodes(Set<String> roleCodes, Integer type);

	Integer insertBatch(List<RoleMenu> list);
}
