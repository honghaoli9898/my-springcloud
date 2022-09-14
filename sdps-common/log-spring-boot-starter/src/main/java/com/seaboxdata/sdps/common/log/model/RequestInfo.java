package com.seaboxdata.sdps.common.log.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestInfo {
	private String serverIp;
	private String applicationName;
	private String startTime;
	private String userId;
	private String username;
	private String url;
	private String className;
	private String methodName;
	private Map requestParams;
	private String endTime;
	private Long timeCost;
	private Object result;
	private Exception exception;
	private String area;
	private String userIp;
}
