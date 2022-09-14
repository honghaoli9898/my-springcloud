package com.seaboxdata.sdps.common.framework.bean.analysis.tenant;

import lombok.Data;

@Data
public class TableInfo {
    /**
     * 表ID
     */
    private Long id;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表描述
     */
    private String des;
    /**
     * 表类型
     */
    private String tableType;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 项目标识
     */
    private String projectIdent;
    /**
     * 创建者ID
     */
    private String creatorId;
    /**
     * 创建者名称
     */
    private String creatorName;
    /**
     * 所属ID(责任人ID)
     */
    private String ownerId;
    /**
     * 所有者名称(责任人)
     */
    private String ownerName;
    /**
     * 表存储路径
     */
    private String location;
    /**
     * 集群ID
     */
    private Long clusterId;
}
