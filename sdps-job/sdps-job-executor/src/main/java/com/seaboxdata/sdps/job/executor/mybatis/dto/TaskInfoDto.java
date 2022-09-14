package com.seaboxdata.sdps.job.executor.mybatis.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
@Getter
@Setter
@ToString
public class TaskInfoDto extends SdpsTaskInfo{

	private static final long serialVersionUID = 1783429169778361231L;
	
	private Date triggerTime;
	
	private Integer triggerCode;
	
	private Date handleTime;
	
	private Integer handleCode;
	
	private String triggerMsg;
	
	private String handleMsg;
	
	private Integer status;
	
	private String executorAddress;
	
	private Long mergeBeforeTotalFileNum;

	private Long mergeBeforeTotalFileBlockSize;

	private BigDecimal avgMergeBeforeTotalFileBlockSize;
	
	private Long mergeAfterTotalFileNum;

	private Long mergeAfterTotalFileBlockSize;

	private BigDecimal avgMergeAfterTotalFileBlockSize;

	private String type;
	
	private String formatType;
	
	private String codecType;
	
	private String dbName;
	
	private String tableName;
}
