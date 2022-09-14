package com.seaboxdata.sdps.usersync.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_role_item")
public class UserRoleItem extends Model<UserRoleItem> implements Serializable {
	@TableField(value = "user_id")
	private Long userId;

	@TableField(value = "role_id")
	private Long roleId;

	@TableField(value = "item_id")
	private Long itemId;

	@TableField(exist = false)
	private String username;

	// @TableField(exist = false)
	// private String type;
	//
	// @TableField(exist = false)
	// private String userIds;

	private static final long serialVersionUID = 1L;

}