package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IRoleMenuService;
import com.seaboxdata.sdps.user.mybatis.mapper.RoleMenuMapper;
import com.seaboxdata.sdps.user.mybatis.model.RoleMenu;

/**
 * @author zlt
 */
@Slf4j
@Service
public class RoleMenuServiceImpl extends SuperServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService  {
 	@Resource
	private RoleMenuMapper roleMenuMapper;

	@Override
	public int save(Long roleId, Long menuId) {
		return roleMenuMapper.save(roleId, menuId);
	}

	@Override
	public int delete(Long roleId, Long menuId) {
		return roleMenuMapper.delete(roleId, menuId);
	}

	@Override
	public List<SysMenu> findMenusByRoleIds(Set<Long> roleIds, Integer type) {
		return roleMenuMapper.findMenusByRoleIds(roleIds, type);
	}

	@Override
	public List<SysMenu> findMenusByRoleCodes(Set<String> roleCodes, Integer type) {
		return roleMenuMapper.findMenusByRoleCodes(roleCodes, type);
	}

	@Override
	public Integer insertBatch(List<RoleMenu> list) {
		return this.baseMapper.insertBatchSomeColumn(list);
	}
}
