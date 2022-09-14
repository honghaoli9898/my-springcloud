package com.seaboxdata.sdps.item.dto.datasource;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.seaboxdata.sdps.item.model.SdpsDatasourceParam;

import lombok.Data;

@Data
public class DataSourceDto implements Serializable {

	private static final long serialVersionUID = -3872382639694108110L;

	private String name;

	private Long id;

	private Long typeId;

	private String type;

	private String assItemId;

	private String assItemName;

	private Date createTime;

	private String createUser;

	private String desc;

	private String properties;

	private List<SdpsDatasourceParam> params;

	private Long clusterId;

	private String clusterShowName;

	private Boolean isVisible;
	
	private Boolean isValid;
	
	private String category;
	
	private String config;
	
	private String value;
}
