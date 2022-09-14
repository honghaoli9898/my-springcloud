package com.seaboxdata.sdps.job.executor.vo;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TaskInfoRequest implements Serializable {
	private static final long serialVersionUID = 2834865024533019443L;
	private Long jobId;
	private String path;
	private Integer clusterId;
	@NotNull
	private Integer type;
	private Integer status;
	private List<Integer> xxlJobIds;
	private String msg;
	private String fileType;
}
