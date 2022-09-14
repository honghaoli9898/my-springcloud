package com.seaboxdata.sdps.seaboxProxy.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServerKerberos {
	private String host;
	private String componentName;
	private String principalName;
	private String keytabName;
	private String ip;
}