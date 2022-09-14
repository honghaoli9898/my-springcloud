package com.seaboxdata.sdps.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IOrganizationService;
import com.seaboxdata.sdps.user.mybatis.mapper.SysOrganizationMapper;
import com.seaboxdata.sdps.user.mybatis.model.SysOrganization;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Slf4j
@Service
public class OrganizationServiceImpl extends
		SuperServiceImpl<SysOrganizationMapper, SysOrganization> implements
		IOrganizationService {
	@Override
	public List<SysOrganization> findOrganizations(
			SysOrganization organization) {
		return this.baseMapper.findOrganizationsByExample(organization);
	}

	@Override
	public List<SysOrganization> findOrganizationById(Long id) {
		List<SysOrganization> organs = CollUtil.newArrayList();
		return findOrganizationById(id, organs);
	}

	private List<SysOrganization> findOrganizationById(Long id,
			List<SysOrganization> result) {
		SysOrganization organ = this.baseMapper.selectById(id);
		result.add(organ);
		if (Objects.isNull(organ) || Objects.equals(-1L, organ.getParentId())) {
			return result;
		} else {
			return findOrganizationById(organ.getParentId(), result);
		}
	}

	public static List<SysOrganization> treeBuilder(
			List<SysOrganization> sysOrganizations) {
		List<SysOrganization> organs = new ArrayList<>();
		if (CollUtil.isEmpty(sysOrganizations))
			return organs;
		for (SysOrganization sysOrgan : sysOrganizations) {
			if (ObjectUtils.equals(-1L, sysOrgan.getParentId())) {
				organs.add(sysOrgan);
			}
			for (SysOrganization organ : sysOrganizations) {
				if (organ.getParentId().equals(sysOrgan.getId())) {
					if (sysOrgan.getSubOrgan() == null) {
						sysOrgan.setSubOrgan(new ArrayList<>());
					}
					sysOrgan.getSubOrgan().add(organ);
				}
			}
		}
		return organs;
	}
}