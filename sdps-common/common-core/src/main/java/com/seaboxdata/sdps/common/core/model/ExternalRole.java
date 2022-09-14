package com.seaboxdata.sdps.common.core.model;

import lombok.Data;

/**
 * 外部角色实体
 * @author pengsong
 */
@Data
public class ExternalRole {
    private Long id;
    private String systemName;
    private Long roleId;
    private String roleName;
}
