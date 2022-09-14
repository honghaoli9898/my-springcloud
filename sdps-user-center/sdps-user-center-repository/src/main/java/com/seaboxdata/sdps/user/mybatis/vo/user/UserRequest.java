package com.seaboxdata.sdps.user.mybatis.vo.user;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class UserRequest implements Serializable {
	private static final long serialVersionUID = -5889380650353477587L;
	private Long id;
	private String username;
	private List<Long> ids;
	private Long groupId;
	private String nickname;
	private String token;
	private String password;
	private String confirmPassword;
	private String email;
	private String validCode;
	private String deviceId;
	private Boolean syncUser = true;
}
