package com.seaboxdata.sdps.common.framework.bean.ranger;


import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper = true)
public class RangerPageGroup extends RangerPage {
    /**
     * Ranger用户组实体列表
     */
    private List<VXGroups> vXGroups;
}
