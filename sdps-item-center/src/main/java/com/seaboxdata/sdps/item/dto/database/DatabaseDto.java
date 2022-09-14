package com.seaboxdata.sdps.item.dto.database;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DatabaseDto implements Serializable {
	private static final long serialVersionUID = 2581381379676352885L;
	private String name;
	private Long id;
	private Long typeId;
	private String type;
	private String assItemName;
	private Long assItemId;
	private String createUser;
	private Date updateTime;
	private String desc;
	private Long clusterId;
	private String clusterShowName;
	private String assItemIden;
}
