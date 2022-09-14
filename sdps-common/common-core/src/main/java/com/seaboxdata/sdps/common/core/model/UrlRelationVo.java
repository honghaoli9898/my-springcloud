package com.seaboxdata.sdps.common.core.model;

import java.util.Date;
import lombok.Data;

/**
 * 链接关系
 * @author pengsong
 */
@Data
public class UrlRelationVo{
    private Long id;
    private String username;
    private Long roleId;
    private String roleName;
    private String systemName;
    private String description;
    private Date createTime;
}
