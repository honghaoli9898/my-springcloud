package com.seabox.sdps.dynamicroute.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@TableName(value = "dynamic_version")
public class DynamicVersion {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	@TableField(value = "create_time")
	private Date createTime;
}