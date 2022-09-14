package com.seaboxdata.sdps.common.framework.bean.ranger;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
public class RangerGroupUser {
    private VXGroups xgroupInfo;
    private List<VXUsers> xuserInfo;
}
