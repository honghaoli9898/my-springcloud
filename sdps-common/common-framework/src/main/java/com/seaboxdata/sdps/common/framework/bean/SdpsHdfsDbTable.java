package com.seaboxdata.sdps.common.framework.bean;

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
@TableName(value = "sdps_hdfs_db_table")
public class SdpsHdfsDbTable implements Serializable {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	@TableField(value = "cluster_id")
	private Long clusterId;

	private String cluster;

	@TableField(value = "db_name")
	private String dbName;

	@TableField(value = "table_name")
	private String tableName;

	private String type;
	
	@TableField(value = "category")
	private String category;

	private String path;

	@TableField(value = "create_time")
	private Date createTime;

	@TableField(value = "update_time")
	private Date updateTime;

	private static final long serialVersionUID = 1L;
}