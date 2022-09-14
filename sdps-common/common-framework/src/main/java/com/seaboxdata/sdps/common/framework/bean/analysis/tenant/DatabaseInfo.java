package com.seaboxdata.sdps.common.framework.bean.analysis.tenant;

import lombok.Data;

@Data
public class DatabaseInfo {
    /**
     * 数据库ID
     */
    private Long id;
    /**
     * 数据库类型
     */
    private String dbType;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 数据库描述
     */
    private String des;
    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 项目名称
     */
    private String projectName;
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
     * 集群ID
     */
    private Long clusterId;
}
