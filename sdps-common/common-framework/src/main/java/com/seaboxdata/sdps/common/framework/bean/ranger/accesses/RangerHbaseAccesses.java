package com.seaboxdata.sdps.common.framework.bean.ranger.accesses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RangerHbaseAccesses {
    READ(1,"read"),
    WRITE(2,"write"),
    CREATE(3,"create"),
    ADMIN(4,"admin");
    private Integer id;
    private String accessesName;
}
