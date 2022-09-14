package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SysGroup;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.vo.group.GroupRequest;
import com.seaboxdata.sdps.user.vo.group.PageGroupRequest;
import com.seaboxdata.sdps.user.vo.role.RoleVo;

public interface IGroupService extends ISuperService<SysGroup> {
	public PageResult<SysGroup> selectGroup(PageGroupRequest request);

	public List<SysGroup> selectGroupByExample(GroupRequest group);

	public boolean deleteGroupInfoByIds(List<Long> groupIds);

	public List<GroupRequest> findGroupByUserIds(List<Long> userId);

	public RoleVo findRoleByGroupId(PageGroupRequest request);

	public PageResult<SysUser> findUserByGroupId(PageGroupRequest request);

	public void updateUserGroupMember(GroupRequest groupRequest);

	public void insertItemRoleGroup(GroupRequest groupRequest);

	public List<SysUser> findExistMemberByGroup(GroupRequest group);

}
