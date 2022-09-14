package com.seaboxdata.sdps.common.framework.bean.merge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "sdps_merge_submit_info")
public class SdpsMergeSubmitInfo implements Serializable {
	private static final long serialVersionUID = 5601900300496196245L;

	/**
     * 合并请求提交任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 集群ID
     */
    @TableField(value = "cluster_id")
    private Integer clusterId;

    /**
     * 任务ID
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 合并文件类型(HDFS,HIVE)
     */
    private String type;

    /**
     * 合并路径
     */
    @TableField(value = "source_path")
    private String sourcePath;

    /**
     * 文件类型
     */
    @TableField(value = "format_type")
    private String formatType;

    /**
     * 文件压缩格式
     */
    @TableField(value = "codec_type")
    private String codecType;

    /**
     * 合并文件对应(HIVE数据库名称)
     */
    @TableField(value = "db_name")
    private String dbName;

    /**
     * 合并文件对应(HIVE数据表名称)
     */
    @TableField(value = "table_name")
    private String tableName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

}