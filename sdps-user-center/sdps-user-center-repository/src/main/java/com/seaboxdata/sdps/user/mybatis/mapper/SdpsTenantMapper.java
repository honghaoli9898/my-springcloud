package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SdpsTenant;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.tenant.TenantDto;

public interface SdpsTenantMapper extends SuperMapper<SdpsTenant> {

	List<TenantDto> selectTenants(@Param("currTenantId") Long currTenantId);

	Page<SysUser> selectMembersByTenantId(@Param("tenantId") Long tenantId);

	List<TenantDto> selectHdfsByTenantId(@Param("tenantId") Long tenantId,
			@Param("level") Integer level);

	List<TenantDto> selectItemsByTenantId(@Param("id") Long id);

	List<TenantDto> selectYarnByTenantId(@Param("id") Long id);

}