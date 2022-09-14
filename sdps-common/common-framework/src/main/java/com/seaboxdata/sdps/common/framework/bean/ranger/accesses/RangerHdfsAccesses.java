package com.seaboxdata.sdps.common.framework.bean.ranger.accesses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RangerHdfsAccesses {
    READ(1,"read"),
    WRITE(2,"write"),
    EXECUTE(3,"execute");
    private Integer id;
    private String accessesName;
}
