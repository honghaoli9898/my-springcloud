package com.seaboxdata.sdps.extendAnalysis.merge;

import lombok.Getter;

/**
 * HDFS文件类型
 */
@Getter
public enum FormatType {
    /**
     * 文本类型
     */
    TEXT(1, "TEXT", "text/plain"),
    /**
     * ORC类型
     */
    ORC(2, "ORC", "ORC"),
    /**
     * sequenceFile类型
     */
    SEQ(3, "SEQ", "SEQ"),
    /**
     * AVRO类型
     */
    AVRO(4, "AVRO", "Obj"),
    /**
     * PARQUET类型
     */
    PARQUET(5, "PARQUET", "PAR");
    private Integer index;
    private String name;
    private String magic;

    private FormatType(Integer index, String name, String magic) {
        this.index = index;
        this.name = name;
        this.magic = magic;
    }

    public static FormatType getByName(String name) {
        FormatType retValue = null;
        FormatType[] values = values();

        for (FormatType type : values) {
            if (type.getName().equalsIgnoreCase(name)) {
                retValue = type;
                break;
            }
        }
        return retValue;
    }

    public static FormatType getByMagic(String magic) {
        FormatType retValue = null;
        FormatType[] values = values();

        for (FormatType type : values) {
            if (type.getMagic().equalsIgnoreCase(magic)) {
                retValue = type;
                break;
            }
        }
        return retValue;
    }
}
