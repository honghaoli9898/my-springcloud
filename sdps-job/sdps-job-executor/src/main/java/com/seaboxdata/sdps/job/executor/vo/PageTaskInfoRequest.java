package com.seaboxdata.sdps.job.executor.vo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PageTaskInfoRequest implements Serializable {
	private static final long serialVersionUID = 5061276822763679016L;
	@NotNull(message = "页数不能为空")
	private Integer page;
	@NotNull(message = "每页大小不能为空")
	private Integer size;

	private TaskInfoRequest param;

}
