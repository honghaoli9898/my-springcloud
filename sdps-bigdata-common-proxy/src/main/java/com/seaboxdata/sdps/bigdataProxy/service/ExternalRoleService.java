package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.ExternalRole;
import java.util.List;

/**
 * @author pengsong
 */
public interface ExternalRoleService{

    /**
     * 获取外部角色
     * @param role
     * @return
     */
    List<ExternalRole> getExternalRoles(ExternalRole role);

    /**
     * 获取外部系统名称
     * @return
     */
    List<String> getExternalSystem();
}
