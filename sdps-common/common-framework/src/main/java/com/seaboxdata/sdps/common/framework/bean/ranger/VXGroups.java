package com.seaboxdata.sdps.common.framework.bean.ranger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ranger用户组实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VXGroups {
    /**
     * Ranger用户组ID
     */
    private Integer id;
    /**
     * Ranger用户组名
     */
    private String name;
    /**
     * 用户组描述
     */
    private String description;
    /**
     * 用户组类型(0:内部用户组,1:外部用户组)
     */
    private String groupType;
    /**
     * 用户组来源(0:内部用户组,1:外部用户组)
     */
    private String groupSource;
    /**
     * 是否可见(0:不可见,1:可见)
     */
    private Integer isVisible;
    /**
     * 所属者(创建者)
     */
    private String owner;
    /**
     * 最后修改用户组信息的用户
     */
    private String updatedBy;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 更新时间
     */
    private String updateDate;
}
