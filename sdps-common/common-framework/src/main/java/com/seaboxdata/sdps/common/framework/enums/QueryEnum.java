package com.seaboxdata.sdps.common.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 存储资源趋势查询类型
 */
@Getter
@AllArgsConstructor
public enum QueryEnum {
    PATH,
    DB,
    TABLE;

    public static List<Integer> getDBIndex() {
        return Arrays.asList(DirFileType.DATABASE_HIVE.getIndex(), DirFileType.DATABASE_EXTERNAL_HIVE.getIndex(), DirFileType.DATABASE_HBASE.getIndex());
    }

    public static List<Integer> getTableIndex() {
        return Arrays.asList(DirFileType.TABLE_HIVE.getIndex(), DirFileType.TABLE_EXTERNAL_HIVE.getIndex(), DirFileType.TABLE_HBASE.getIndex());
    }

    public static List<Integer> getTypeIndex(QueryEnum type, String category) {
        DirFileType fileType = null;
        if ("hive".equalsIgnoreCase(category)) {
            fileType = type == DB ? DirFileType.DATABASE_HIVE : DirFileType.TABLE_HIVE;
        }

        if ("hive_external".equalsIgnoreCase(category)) {
            fileType = type == DB ? DirFileType.DATABASE_EXTERNAL_HIVE : DirFileType.TABLE_EXTERNAL_HIVE;
        }

        if ("hbase".equalsIgnoreCase(category)) {
            fileType = type == DB ? DirFileType.DATABASE_HBASE : DirFileType.TABLE_HBASE;
        }

        if (fileType != null) {
            return Collections.singletonList(fileType.getIndex());
        } else {
            return type == DB ? getDBIndex() : getTableIndex();
        }
    }

    public static List<Integer> getAllTypeIndex() {
        return Arrays.asList(DirFileType.DATABASE_HIVE.getIndex(), DirFileType.DATABASE_EXTERNAL_HIVE.getIndex(), DirFileType.DATABASE_HBASE.getIndex(), DirFileType.PROJECT.getIndex());
    }
}
