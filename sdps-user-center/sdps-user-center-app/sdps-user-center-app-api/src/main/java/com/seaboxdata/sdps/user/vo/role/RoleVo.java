package com.seaboxdata.sdps.user.vo.role;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.user.mybatis.dto.role.RoleDto;
@Getter
@Setter
@ToString
public class RoleVo implements Serializable{
	private static final long serialVersionUID = -7194914135394861148L;

	private PageInfo<RoleDto> pageItemRoles;

	private List<SysRole> systemRoles;
	
	
}
