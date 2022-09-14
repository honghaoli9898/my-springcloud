package com.seaboxdata.sdps.user.vo.user;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;

@Getter
@Setter
@ToString
public class PageUserRequest {
	private UserRequest param;
	@NotNull(message = "每页大小不能为空")
	private Integer size;
	@NotNull(message = "页数不能为空")
	private Integer page;

}
