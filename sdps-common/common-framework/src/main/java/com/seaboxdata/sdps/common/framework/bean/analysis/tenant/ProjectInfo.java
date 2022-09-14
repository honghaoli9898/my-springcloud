package com.seaboxdata.sdps.common.framework.bean.analysis.tenant;

import lombok.Data;

@Data
public class ProjectInfo {
    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 项目标识
     */
    private String identification;
    /**
     * 项目名称
     */
    private String projectName;
}
