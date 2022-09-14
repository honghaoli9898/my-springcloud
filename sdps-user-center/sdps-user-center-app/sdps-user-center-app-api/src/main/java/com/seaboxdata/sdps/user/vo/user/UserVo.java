package com.seaboxdata.sdps.user.vo.user;

import java.io.Serializable;
import java.util.List;

import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserVo implements Serializable {
	private static final long serialVersionUID = -5109945016577210038L;
	private List<UserRoleItem> roles;
	private Long userId;
	private Long itemId;
	private List<Long> userIds;
	private String type;
	private Long tenantId;
	private List<Long> roleIds;
}
