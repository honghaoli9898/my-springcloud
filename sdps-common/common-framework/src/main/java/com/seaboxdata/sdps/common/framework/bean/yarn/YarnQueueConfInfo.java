package com.seaboxdata.sdps.common.framework.bean.yarn;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YarnQueueConfInfo implements Serializable{

	private static final long serialVersionUID = -8291910041139128836L;
	
	private String parentQueueFullName;
	
	private String queueName;
	
	private String queueFullName;
	
	private String capacity;
	
	private List<String> queues;
	
	private String state;
	
	private String maximumCapacity;
	
	private String priority;
	
	private String maximumAllocationMb;
	
	private String maximumAllocationVcores;
	
	private String userLimitFactor;
	
	private String minimumUserLimitPercent;
	
	private String maximumApplications;
	
	private String maximumAmResourcePercent;
	
	private String orderingPolicy;
	
	private List<YarnQueueConfInfo> subQueues;
	
	private Boolean isNew;
	
	private Boolean isDelete;
}
