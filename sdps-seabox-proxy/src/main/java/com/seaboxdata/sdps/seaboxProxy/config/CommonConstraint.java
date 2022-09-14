package com.seaboxdata.sdps.seaboxProxy.config;


public class CommonConstraint {
    /**
     * 存储资源趋势类型：路径
     */
    public static final String PATH = "PATH";
    /**
     * 数据源类型：phoenix
     */
    public static final String phoenix = "phoenix";
    /**
     * 存储资源趋势类型：库
     */
    public static final String DB = "DB";
    /**
     * 存储资源趋势类型：表
     */
    public static final String TABLE = "TABLE";

    public static final Integer HBASE_DB = 2;

    public static final Integer HIVE_DB = 3;

    public static final Integer HBASE_TABLE = 4;

    public static final Integer HIVE_TABLE = 5;

    public static final Integer PROJECT = 6;

    public static final Integer FILE = 1;
}
