package com.seaboxdata.sdps.common.framework.bean.ambari;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AmbariStartOrStopServiceObj implements Serializable{
	private static final long serialVersionUID = 5817944189898292027L;
	
	private Integer clusterId;
	
	private String serviceName;
	
	private String state;
	
	private String clusterName;

}
