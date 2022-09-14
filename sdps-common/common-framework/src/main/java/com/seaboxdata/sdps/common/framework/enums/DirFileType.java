package com.seaboxdata.sdps.common.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DirFileType {
    DATABASE_HIVE(101, "DATABASE_HIVE"),
    DATABASE_EXTERNAL_HIVE(102, "DATABASE_EXTERNAL_HIVE"),
    DATABASE_HBASE(103, "DATABASE_HBASE"),
    TABLE_HIVE(201, "TABLE_HIVE"),
    TABLE_EXTERNAL_HIVE(202, "TABLE_EXTERNAL_HIVE"),
    TABLE_HBASE(203, "TABLE_HBASE"),
    PROJECT(301, "PROJECT"),
    UNKOWN(901, "UNKNOWN");

    private Integer index;
    private String name;
}
