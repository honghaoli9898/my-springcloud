package com.seaboxdata.sdps.bigdataProxy.bean;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SdpsServerInfoDto extends SdpsServerInfo {
	private static final long serialVersionUID = 6843617412269631210L;
	
	private Long clusterId;
	
	private String clusterName;
	
	private String typeName;

}
