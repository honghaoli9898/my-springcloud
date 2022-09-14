package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;

public interface SysUserMapper extends SuperMapper<SysUser> {
	public List<SysUser> findExistMemberByGroup(@Param("ids") List<Long> ids);
}