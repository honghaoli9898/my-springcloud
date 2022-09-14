package com.seaboxdata.sdps.seaboxProxy.bean;

import java.util.List;

import cn.hutool.core.collection.CollUtil;

public enum FormatType {
    TEXT(1, "TEXT", "text/plain"),
    ORC(2, "ORC", "ORC"),
    SEQ(3, "SEQ", "SEQ"),
    AVRO(4, "AVRO", "Obj"),
    PARQUET(5, "PARQUET", "PAR");

    private Integer index;
    private String name;
    private String magic;

    FormatType(Integer index, String name, String magic) {
        this.index = index;
        this.name = name;
        this.magic = magic;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public String getMagic() {
        return this.magic;
    }

    public static List<String> getByNames() {
        List<String> names = CollUtil.newArrayList();
        FormatType[] values = values();
        FormatType[] var2 = values;
        int var3 = values.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            FormatType type = var2[var4];
            names.add(type.getName());
        }

        return names;
    }

    public static FormatType getByName(String name) {
        FormatType retValue = null;
        FormatType[] values = values();
        FormatType[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            FormatType type = var3[var5];
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
        FormatType[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            FormatType type = var3[var5];
            if (type.getMagic().equalsIgnoreCase(magic)) {
                retValue = type;
                break;
            }
        }

        return retValue;
    }

}
