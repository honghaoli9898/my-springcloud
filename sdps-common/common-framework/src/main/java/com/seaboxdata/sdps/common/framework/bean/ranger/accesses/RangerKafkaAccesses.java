package com.seaboxdata.sdps.common.framework.bean.ranger.accesses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RangerKafkaAccesses {
    PUBLISH(1,"publish"),
    CONSUME(2,"consume"),
    CONFIGURE(3,"configure"),
    DESCRIBE(4,"describe"),
    CREATE(5,"create"),
    DELETE(6,"delete"),
    DESCRIBE_CONFIGS(7,"describe_configs"),
    ALTER_CONFIGS(8,"alter_configs");
    private Integer id;
    private String accessesName;
}
