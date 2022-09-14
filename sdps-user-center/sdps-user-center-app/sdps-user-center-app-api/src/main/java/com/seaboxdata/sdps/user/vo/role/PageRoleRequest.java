package com.seaboxdata.sdps.user.vo.role;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.seaboxdata.sdps.user.mybatis.vo.role.RoleRequest;

import lombok.Data;

@Data
public class PageRoleRequest implements Serializable{
	private static final long serialVersionUID = 5890263475497384382L;
	@NotNull(message = "每页大小不能为空")
	private Integer size;
	@NotNull(message = "页数不能为空")
	private Integer page;
	
	private RoleRequest param;
}
