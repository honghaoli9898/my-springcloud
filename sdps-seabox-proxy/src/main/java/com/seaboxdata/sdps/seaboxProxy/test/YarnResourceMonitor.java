package com.seaboxdata.sdps.seaboxProxy.test;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.api.YarnClient;

public class YarnResourceMonitor {

	private static String confPath = "E:/kerberos";

	public static void main(String[] args) throws Exception {
		Configuration configuration = initConfiguration(confPath);

		// 初始化Kerberos环境
		initKerberosENV(configuration);

		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(configuration);
		yarnClient.start();
		System.out.println(yarnClient.getAllQueues());
		
	}

	/**
	 * 初始化HDFS Configuration
	 * 
	 * @return configuration
	 */
	public static Configuration initConfiguration(String confPath) {
		Configuration configuration = new Configuration();
		System.out.println(confPath + File.separator + "core-site.xml");
		configuration.addResource(new Path(confPath + File.separator
				+ "core-site.xml"));
		configuration.addResource(new Path(confPath + File.separator
				+ "hdfs-site.xml"));
		configuration.addResource(new Path(confPath + File.separator
				+ "yarn-site.xml"));
		return configuration;
	}

	/**
	 * 初始化Kerberos环境
	 */
	public static void initKerberosENV(Configuration conf) {
		System.setProperty("java.security.krb5.conf",
				"E:/kerberos/krb5.conf");
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation
					.loginUserFromKeytab("hdfs-seabox4@HADOOP.COM",
							"E:/kerberos/hdfs.headless.keytab");
			System.out.println(UserGroupInformation.getCurrentUser());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}