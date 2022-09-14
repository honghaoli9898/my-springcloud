package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.SysOrganization;

public interface SysOrganizationMapper extends SuperMapper<SysOrganization> {
	public List<SysOrganization> findOrganizationsByExample(
			@Param("organization") SysOrganization organization);
}