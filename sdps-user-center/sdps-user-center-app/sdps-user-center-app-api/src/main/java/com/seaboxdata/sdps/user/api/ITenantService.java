package com.seaboxdata.sdps.user.api;

import java.util.List;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SdpsTenant;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.mybatis.dto.tenant.TenantDto;
import com.seaboxdata.sdps.user.vo.tenant.TenantResourceVo;

public interface ITenantService extends ISuperService<SdpsTenant> {

	List<TenantDto> selectTenants(Long currTenantId);

	SdpsTenant insertTenant(SdpsTenant sdpsTenant);

	TenantDto selectTenantById(Long tenantId);

	PageResult<SysUser> selectMembersByTenantId(PageRequest<SdpsTenant> request);

	void createResource(TenantResourceVo tenantResource);

	TenantDto getHdfsByTenantId(Long tenantId);

	TenantDto getYarnByTenantId(Long tenantId);

	boolean deleteTenantById(Long tenantId, TenantResourceVo resourceVo);

	boolean updateYarnResource(TenantResourceVo tenantResourceVo);

	List<SdpsTenant> findNoHasTenantsByUserId(Long userId);

	boolean removeTenantUsers(TenantResourceVo tenantResourceVo);

	boolean updateHdfsResource(TenantResourceVo tenantResourceVo);

	List<SdpsTenant> findHasTenantsByUserId(Long userId);

}
