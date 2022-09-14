package com.seaboxdata.sdps.user.vo.user;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;

@Getter
@Setter
@ToString
public class CreateUserVo implements Serializable {
	private static final long serialVersionUID = 4027907084653280335L;
	@NotBlank(message = "登录名不能为空")
	private String username;
	@NotBlank(message = "密码不能为空")
	private String password;
	@NotBlank(message = "用户名不能为空")
	private String nickname;
	private String headImgUrl;
	private Integer sex;
	private Boolean enabled;
	@Email(message = "不满足邮箱格式")
	private String email;
	@TableField(value = "`desc`")
	private String desc;
	@NotNull
	private List<Long> ids;

	private Boolean syncUser;

	private List<Integer> clusterIds;

	private List<Long> tenantIds;
}
