package com.seaboxdata.sdps.job.executor.core.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XxlJobExecutorProperties {
	private String appname;
	private String address;
	private String ip;
	private Integer port;
	private String logpath;
	private Integer logretentiondays;
}
