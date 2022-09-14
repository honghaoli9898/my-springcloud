package com.seaboxdata.sdps.common.core.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "security.kerberos.login")
public class KerberosProperties {
	
	private Boolean enable = false;
	/**
	 * sdp服务中keytab文件位置
	 */
	private String keytabPath;

	/**
	 * sdp服务中keytab文件位置
	 */
	private String seaboxKeytabPath;
	
	private String userSyncKeytabPath;
	
	private String kdcKeytabPath;
	
	/**
	 * sdp服务中keytab文件位置
	 */
	private String itemKeytabPath;
	
	private String userSuffix = "@HADOOP.COM";
	
	private String krb5;
	
	private String adminPrincipal;
	
	private String adminKeytab;

	private String getKeytabUrl;
}
