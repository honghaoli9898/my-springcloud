package com.seaboxdata.sdps.user.mybatis.dto.role;

import java.io.Serializable;
import java.util.List;

import com.seaboxdata.sdps.common.core.model.SysRole;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class RoleDto implements Serializable {
	private static final long serialVersionUID = -5448068605626504564L;
	private Long userId;
	private Long roleId;
	private Long itemId;
	private String itemName;
	private String roleIds;
	private String roleNames;
	private List<SysRole> roles;
}
