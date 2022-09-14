package com.seaboxdata.sdps.common.framework.bean.ranger;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper = true)
public class RangerPageUser extends RangerPage {
    /**
     * Ranger用户实体列表
     */
    private List<VXUsers> vXUsers;
}
