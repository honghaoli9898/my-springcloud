package com.seaboxdata.sdps.job.executor.mybatis.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "xxl_job_info")
@Data
public class XxlJobInfo implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;				// 主键ID

    @TableField(value = "job_group")
    private int jobGroup;		// 执行器主键ID

    @TableField(value = "job_desc")
    private String jobDesc;

    @TableField(value = "add_time")
    private Date addTime;

    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(value = "author")
    private String author;		// 负责人

    @TableField(value = "alarm_email")
    private String alarmEmail;	// 报警邮件

    @TableField(value = "schedule_type")
    private String scheduleType;			// 调度类型

    @TableField(value = "schedule_conf")
    private String scheduleConf;			// 调度配置，值含义取决于调度类型

    @TableField(value = "misfire_strategy")
    private String misfireStrategy;			// 调度过期策略

    @TableField(value = "executor_route_strategy")
    private String executorRouteStrategy;	// 执行器路由策略

    @TableField(value = "executor_handler")
    private String executorHandler;		    // 执行器，任务Handler名称

    @TableField(value = "executor_param")
    private String executorParam;		    // 执行器，任务参数

    @TableField(value = "executor_block_strategy")
    private String executorBlockStrategy;	// 阻塞处理策略

    @TableField(value = "executor_timeout")
    private int executorTimeout;     		// 任务执行超时时间，单位秒

    @TableField(value = "executor_fail_retry_count")
    private int executorFailRetryCount;		// 失败重试次数

    @TableField(value = "glue_type")
    private String glueType;		// GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum

    @TableField(value = "glue_source")
    private String glueSource;		// GLUE源代码

    @TableField(value = "glue_remark")
    private String glueRemark;		// GLUE备注

    @TableField(value = "glue_updatetime")
    private Date glueUpdatetime;	// GLUE更新时间

    @TableField(value = "child_jobid")
    private String childJobId;		// 子任务ID，多个逗号分隔

    @TableField(value = "trigger_status")
    private int triggerStatus;		// 调度状态：0-停止，1-运行

    @TableField(value = "trigger_last_time")
    private long triggerLastTime;	// 上次调度时间

    @TableField(value = "trigger_next_time")
    private long triggerNextTime;	// 下次调度时间

}
