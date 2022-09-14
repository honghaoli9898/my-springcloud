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
@TableName(value = "sdps_merge_data_info")
public class SdpsMergeDataInfo implements Serializable {
	private static final long serialVersionUID = 7440203767211961321L;

	/**
     * 合并文件数据ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 集群ID
     */
    @TableField(value = "cluster_id")
    private Integer clusterId;

    /**
     * 提交任务ID
     */
    @TableField(value = "submit_id")
    private Long submitId;

    /**
     * 任务ID
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 提交合并路径
     */
    @TableField(value = "source_path")
    private String sourcePath;

    /**
     * 路径
     */
    @TableField(value = "path")
    private String path;

    /**
     * 合并前文件总数
     */
    @TableField(value = "merge_before_total_file_num")
    private Long mergeBeforeTotalFileNum;

    /**
     * 合并前文件总大小(单位:Byte)
     */
    @TableField(value = "merge_before_total_file_size")
    private Long mergeBeforeTotalFileSize;

    /**
     * 合并前文件块总大小(单位:Byte)
     */
    @TableField(value = "merge_before_total_file_block_size")
    private Long mergeBeforeTotalFileBlockSize;

    /**
     * 合并前小文件总数(单位:个)
     */
    @TableField(value = "merge_before_total_small_file_num")
    private Long mergeBeforeTotalSmallFileNum;

    /**
     * 合并后文件总数
     */
    @TableField(value = "merge_after_total_file_num")
    private Long mergeAfterTotalFileNum;

    /**
     * 合并后文件总大小
     */
    @TableField(value = "merge_after_total_file_size")
    private Long mergeAfterTotalFileSize;

    /**
     * 合并后文件块总大小
     */
    @TableField(value = "merge_after_total_file_block_size")
    private Long mergeAfterTotalFileBlockSize;

    /**
     * 合并后小文件总数
     */
    @TableField(value = "merge_after_total_small_file_num")
    private Long mergeAfterTotalSmallFileNum;

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