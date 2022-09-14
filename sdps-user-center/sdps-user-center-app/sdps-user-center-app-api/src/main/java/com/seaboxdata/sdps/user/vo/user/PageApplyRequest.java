package com.seaboxdata.sdps.user.vo.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class PageApplyRequest {

	@NotNull(message = "每页大小不能为空")
	private Integer pageSize;

	@NotNull(message = "页数不能为空")
	private Integer pageNum;

	private Integer status;

	private String userName;

	private String applyContent;

	private Integer spStatus;

}
