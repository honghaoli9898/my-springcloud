package com.seaboxdata.sdps.user.vo.user;

import lombok.Data;
import cn.hutool.core.annotation.Alias;

@Data
public class ImportUserCsvVo {
	@Alias("登录名")
	private String username;

	@Alias("用户名称")
	private String nickname;

	@Alias("性别")
	private Integer sex;

	@Alias("联系邮箱")
	private String email;

	@Alias("描述")
	private String desc;

	@Alias("角色")
	private String roles;
	
	@Alias("集群ID")
	private String clusterIds;
}
