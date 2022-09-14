package com.seaboxdata.sdps.common.framework.bean.request;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class UserSyncRequest implements Serializable {
	private static final long serialVersionUID = 9203100707731718410L;
	
	private List<Long> userIds;
	
	private Long userId;

	private List<String> syncTypes;
	
	/**
	 * 1-增加，2-删除，3-更新
	 */
	private String operation;
	
	private String beanName;
	
	private List<Integer> clusterIds;
	
}
