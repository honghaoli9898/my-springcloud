package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.SysOrganization;

public interface IOrganizationService extends
		ISuperService<SysOrganization> {
	public List<SysOrganization> findOrganizations(
			SysOrganization organization);

	public List<SysOrganization> findOrganizationById(Long id);
}
