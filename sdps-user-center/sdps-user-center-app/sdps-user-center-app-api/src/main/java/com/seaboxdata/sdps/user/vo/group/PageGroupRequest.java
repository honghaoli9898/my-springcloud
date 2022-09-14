package com.seaboxdata.sdps.user.vo.group;

import javax.validation.constraints.NotNull;

import lombok.Data;

import com.seaboxdata.sdps.user.mybatis.vo.group.GroupRequest;

@Data
public class PageGroupRequest {
	private GroupRequest param;
	@NotNull(message = "每页大小不能为空")
	private Integer size;
	@NotNull(message = "页数不能为空")
	private Integer page;
}
