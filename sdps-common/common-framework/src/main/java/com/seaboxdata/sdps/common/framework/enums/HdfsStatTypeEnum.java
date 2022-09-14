package com.seaboxdata.sdps.common.framework.enums;

import com.seaboxdata.sdps.common.core.enums.EnumBase;

/**
 * HDFS统计类别
 */
public enum HdfsStatTypeEnum implements EnumBase {
    STORAGE("storage", "hdfs存储量"), FILE("file", "hdfs文件数"), SMALLFILE("smallFile", "hdfs小文件数");

    public static String getCodeByName(String name) {

        for (HdfsStatTypeEnum type : HdfsStatTypeEnum.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type.code;
            }
        }
        return null;
    }

    /**
     * code编码
     */
    public final String code;
    /**
     * 中文信息描述
     */
    final String message;

    HdfsStatTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
