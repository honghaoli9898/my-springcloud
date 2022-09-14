package com.seaboxdata.sdps.item.model;

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
@TableName(value = "role_group_item")
public class RoleGroupItem extends Model<RoleGroupItem> implements
		Serializable {
	@TableField(value = "role_id")
	private Long roleId;

	@TableField(value = "group_id")
	private Long groupId;
	
	@TableField(value = "item_id")
	private Long itemId;
	private static final long serialVersionUID = 1L;

}