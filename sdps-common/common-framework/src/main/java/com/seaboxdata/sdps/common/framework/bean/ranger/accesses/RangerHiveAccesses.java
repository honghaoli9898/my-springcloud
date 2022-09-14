package com.seaboxdata.sdps.common.framework.bean.ranger.accesses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RangerHiveAccesses {
    SELECT(1,"select"),
    UPDATE(2,"update"),
    CREATE(3,"create"),
    DROP(4,"drop"),
    ALTER(5,"alter"),
    INDEX(6,"index"),
    LOCK(7,"lock"),
    READ(8,"read"),
    WRITE(9,"write"),
    REPLADMIN(10,"repladmin"),
    REFRESH(11,"refresh"),
    SERVICEADMIN(12,"serviceadmin"),
    TEMPUDFADMIN(13,"tempudfadmin"),
    ALL(14,"all");
    private Integer id;
    private String accessesName;
}
