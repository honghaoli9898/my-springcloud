package com.seaboxdata.sdps.common.framework.bean.ambari;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AmbariServiceAutoStartObj implements Serializable{
	private static final long serialVersionUID = -2497323581919739744L;
	
	private List<String> services;
	
	private Boolean isAuto;
	
	private Integer clusterId;

}
