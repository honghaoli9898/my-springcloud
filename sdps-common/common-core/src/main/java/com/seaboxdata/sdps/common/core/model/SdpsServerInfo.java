package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;

import lombok.*;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName(value = "sdps_server_info")
@AllArgsConstructor
@Builder
public class SdpsServerInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	/**
	 * Ranger配置ID
	 */
	@TableField(value = "server_id")
	private Integer serverId;
	/**
	 * Ranger节点IP
	 */
	@TableField(value = "host")
	private String host;
	/**
	 * Ranger端口
	 */
	@TableField(value = "port")
	private String port;
	/**
	 * Ranger管理员用户名
	 */
	@TableField(value = "user")
	private String user;
	/**
	 * Ranger管理员密码
	 */
	@TableField(value = "passwd")
	private String passwd;
	@TableField
	private String type;
	/**
	 * 登录url
	 */
	@TableField(value = "login_url")
	private String loginUrl;
	@TableField(value = "cluster_type", exist = false)
	private String clusterType;
	@TableField(value = "domain")
	private String domain;

	public SdpsServerInfo(Integer serverId, String host, String port,
			String user, String passwd, String type) {
		this.serverId = serverId;
		this.host = host;
		this.port = port;
		this.user = user;
		this.passwd = passwd;
		this.type = type;
	}
}