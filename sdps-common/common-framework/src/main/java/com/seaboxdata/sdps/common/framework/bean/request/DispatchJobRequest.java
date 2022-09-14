package com.seaboxdata.sdps.common.framework.bean.request;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchJobRequest implements Serializable {
	private static final long serialVersionUID = -3969860582545811509L;
	/**
     * 是否是SDPS任务
     */
    private String isSdpsJob;
    /**
     * 是否更新日志ID
     */
    private String isUpdateLogId;

    /**
     * 是否创建TaskInfo表信息
     */
    private Boolean createTaskInfo;

    /**
     * 更新日志的具体ID
     */
    private String sdpsJobId;
    /**
     * 集群ID 接口必传
     */
    private Integer clusterId;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 合并文件路径 接口必传
     */
    private String sourcePath;
    /**
     * 文件压缩格式 接口必传
     */
    private String codec;
    /**
     * 文件格式 接口必传
     */
    private String format;
    /**
     * 合并文件的任务类型  接口必传
     */
    private String type;
    /**
     * 合并文件的用户
     */
    private String userName;

    private Long submitId;

    private Map<String,String> mergeDataIdPathMap;

    /**
     * 数据库名称
     */
    private String databaseName;
    /**
     * 数据表名称
     */
    private String tableName;


}
