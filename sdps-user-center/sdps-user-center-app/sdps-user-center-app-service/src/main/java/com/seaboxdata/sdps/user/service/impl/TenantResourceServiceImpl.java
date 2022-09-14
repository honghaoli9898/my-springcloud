package com.seaboxdata.sdps.user.service.impl;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.ITenantResourceService;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsTenantResourceMapper;

@Service
public class TenantResourceServiceImpl extends
		SuperServiceImpl<SdpsTenantResourceMapper, SdpsTenantResource> implements
		ITenantResourceService {

}
