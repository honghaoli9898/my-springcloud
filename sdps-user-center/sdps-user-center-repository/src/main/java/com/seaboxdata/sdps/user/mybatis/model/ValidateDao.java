package com.seaboxdata.sdps.user.mybatis.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sdps_pm_validate")
public class ValidateDao {
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	@TableField(value = "user_id")
	private Long userId;

	@TableField(value = "email")
	private String email;

	@TableField(value = "reset_token")
	private String resetToken;

	@TableField(value = "type")
	private String type;

	@TableField(value = "gmt_create")
	private Date gmtCreate;

	@TableField(value = "gmt_modified")
	private Date gmtModified;
}