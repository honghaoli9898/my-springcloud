package com.seaboxdata.sdps.usersync.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

@Getter
@Setter
@ToString
@TableName(value = "user_group")
public class UserGroup extends Model<UserGroup> implements
		Serializable {
	@TableField(value = "user_id")
	private Long userId;

	@TableField(value = "group_id")
	private Long groupId;

	@TableField(exist = false)
	private String type;

	private static final long serialVersionUID = 1L;

}