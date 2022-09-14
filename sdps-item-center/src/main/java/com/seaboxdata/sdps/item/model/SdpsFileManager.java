package com.seaboxdata.sdps.item.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@TableName(value = "sdps_file_manager")
public class SdpsFileManager implements Serializable {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 创建时间
	 */
	@TableField(value = "create_time")
	private Date createTime;

	/**
	 * 创建人id
	 */
	@TableField(value = "create_user")
	private Long createUser;

	/**
	 * 创建人名字
	 */
	@TableField(value = "create_username")
	private String createUsername;

	/**
	 * 脚本名称
	 */
	private String name;

	/**
	 * 文件类型
	 */
	private String type;

	/**
	 * 文件路径
	 */
	private String path;

	/**
	 * 文件归属,P代表项目,O代表个人
	 */
	private String belong;

	/**
	 * 集群id
	 */
	@TableField(value = "cluster_id")
	private Long clusterId;

	/**
	 * 项目id
	 */
	@TableField(value = "item_id")
	private Long itemId;
	
	/**
	 * 服务类型
	 */
	@TableField(value = "server_type")
	private String serverType;

	private static final long serialVersionUID = 1L;

}