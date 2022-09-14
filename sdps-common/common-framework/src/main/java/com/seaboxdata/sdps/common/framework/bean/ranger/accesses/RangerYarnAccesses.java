package com.seaboxdata.sdps.common.framework.bean.ranger.accesses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RangerYarnAccesses {
    SUBMIT_APP(1,"submit-app"),
    ADMIN_QUEUE(2,"admin-queue");
    private Integer id;
    private String accessesName;
}
