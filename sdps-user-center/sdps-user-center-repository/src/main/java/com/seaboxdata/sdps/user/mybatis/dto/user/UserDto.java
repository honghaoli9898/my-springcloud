package com.seaboxdata.sdps.user.mybatis.dto.user;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.common.core.model.SysRole;

@Getter
@Setter
@ToString
public class UserDto implements Serializable {
	private static final long serialVersionUID = 5908490075137410059L;
	private Long id;
	private String username;
	private String nickname;
	private Date createTime;
	private List<SysRole> roles;
	private String roleIds;
	private String roleNames;
	private String type;
	private List<UserDto> users;
	private List<Long> roleIdList;
	private String principalName;
	private String keyTabPath;
	private String keyTabName;
	private String roleName;
	private String itemName;
	private Long tenantId;
	private Long itemId;
	private String tenantName;
	private Long roleId;

}
