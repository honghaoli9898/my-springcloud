package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.ExternalRoleMapper;
import com.seaboxdata.sdps.bigdataProxy.service.ExternalRoleService;
import com.seaboxdata.sdps.common.core.model.ExternalRole;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author pengsong
 */
@Service
public class ExternalRoleServiceImpl implements ExternalRoleService{
    @Autowired
    private ExternalRoleMapper externalRoleMapper;

    @Override
    public List<ExternalRole> getExternalRoles(ExternalRole role) {
        QueryWrapper<ExternalRole> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(role.getSystemName())) {
            queryWrapper.eq("system_name",role.getSystemName());
        }
        if (StringUtils.isNotBlank(role.getRoleName())) {
            queryWrapper.eq("role_name",role.getSystemName());
        }
        List<ExternalRole> externalRoles = externalRoleMapper.selectList(queryWrapper);
        return externalRoles;
    }

    @Override
    public List<String> getExternalSystem() {
        QueryWrapper<ExternalRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT system_name");
        List<ExternalRole> externalRoles = externalRoleMapper.selectList(queryWrapper);
        List<String> res = new ArrayList<>();
        externalRoles.forEach(t->{
            res.add(t.getSystemName());
        });
        return res;
    }
}
