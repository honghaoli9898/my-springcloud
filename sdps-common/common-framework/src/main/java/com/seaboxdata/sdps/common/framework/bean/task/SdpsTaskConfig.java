package com.seaboxdata.sdps.common.framework.bean.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "sdps_task_config")
public class SdpsTaskConfig {
    /**
     * 任务配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 集群类型ID
     */
    @TableField(value = "cluster_type_id")
    private Integer clusterTypeId;

    /**
     * 任务类型
     */
    @TableField(value = "task_type")
    private String taskType;

    /**
     * 任务参数key
     */
    @TableField(value = "arg_key")
    private String argKey;

    /**
     * 任务参数value
     */
    @TableField(value = "arg_value")
    private String argValue;

    /**
     * 任务参数说明
     */
    @TableField(value = "arg_desc")
    private String argDesc;
}
