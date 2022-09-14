package com.seaboxdata.sdps.user.mybatis.vo.role;

import java.util.List;

import lombok.Data;



@Data
public class RoleRequest {
	private String name;
	private Long id;
	private Boolean enabled; 
	private String type;
	private List<String> types;
	private String code;
}
