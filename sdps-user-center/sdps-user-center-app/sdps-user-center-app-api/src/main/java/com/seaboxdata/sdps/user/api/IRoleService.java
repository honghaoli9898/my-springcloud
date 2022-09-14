package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.vo.role.RoleRequest;
import com.seaboxdata.sdps.user.vo.role.PageRoleRequest;

public interface IRoleService extends ISuperService<SysRole> {
	public PageResult<SysRole> findRoles(PageRoleRequest request);

	public List<SysRole> findRoleByUserIds(List<Long> userIds, String type);

	public boolean deleteRoleInfoByIds(List<Long> roleIds);

	public PageInfo<SysUser> findUserByRoleId(PageRoleRequest request);

	public List<SysRole> findRoleList(RoleRequest request);
}
