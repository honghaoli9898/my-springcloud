package com.seaboxdata.sdps.common.framework.bean.analysis;

import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import lombok.Data;

/**
 * 数据库表信息对象
 */
@Data
public class DBTableInfo {
    /**
     * 集群
     */
    private String cluster;
    /**
     * 集群ID
     */
    private String clusterId;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 表名
     */

    private String tableName = "";
    private String type;
    /**
     * 类型
     */
    private String category;
    /**
     * 路径
     */
    private String path;

    public DirFileType getDirType() {
        if ("hive".equalsIgnoreCase(this.category)) {
            return "db".equalsIgnoreCase(this.type) ? DirFileType.DATABASE_HIVE : DirFileType.TABLE_HIVE;
        } else if ("hive_external".equalsIgnoreCase(this.category)) {
            return "db".equalsIgnoreCase(this.type) ? DirFileType.DATABASE_EXTERNAL_HIVE : DirFileType.TABLE_EXTERNAL_HIVE;
        } else if ("hbase".equalsIgnoreCase(this.category)) {
            return "db".equalsIgnoreCase(this.type) ? DirFileType.DATABASE_HBASE : DirFileType.TABLE_HBASE;
        } else {
            return DirFileType.UNKOWN;
        }
    }

    public String getDirTypeValue() {
        return "db".equalsIgnoreCase(this.type) ? this.dbName : this.dbName + "." + this.tableName;
    }
}
