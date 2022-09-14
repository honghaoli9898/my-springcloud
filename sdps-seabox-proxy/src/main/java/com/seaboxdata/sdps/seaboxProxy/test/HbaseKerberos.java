package com.seaboxdata.sdps.seaboxProxy.test;

import io.leopard.javahost.JavaHost;

import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.TokenIdentifier;

@Slf4j
public class HbaseKerberos {
	public static void main(String[] args) throws SQLException {
		init("10.1.3.24",
				"hbase-seabox4@HADOOP.COM",
				"C:/data/SDP7.1/sdps/sdp7.1/keytab/1.hbase.headless.keytab");


//		System.out.println("====================");
//		Properties props = new Properties();
//		props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
//		props.setProperty("phoenix.schema.mapSystemTablesToNamespace", "true");
//		props.setProperty(
//				CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
//				"true");
//		JavaHost.updateVirtualDns("master", "10.1.2.15");
//		JavaHost.updateVirtualDns("node16", "10.1.2.16");
//		JavaHost.updateVirtualDns("node17", "10.1.2.17");
//		JavaHost.updateVirtualDns("node18", "10.1.2.18");
//		Connection conn = DriverManager
//				.getConnection("jdbc:phoenix:10.1.2.15,10.1.2.16,10.1.2.17:2181:/hbase-unsecure",props);
//		conn.createStatement().executeQuery(
//				"select * from HDFS_NM.TB_DIR_INFO limit 10");

	}

	public static void init(String kerberosIp, String user, String keytabPath) {
		if ((StringUtils.isBlank(kerberosIp)) || (StringUtils.isBlank(user))
				|| (StringUtils.isBlank(keytabPath))) {
			log.error("please input the kerberos IpAddr、user and keytabPath");
			throw new RuntimeException(
					"please input the kerberos IpAddr、user and keytabPath");
		}
		synchronized (HbaseKerberos.class) {
			System.setProperty("java.security.krb5.conf", "C:/data/SDP7.1/sdps/sdp7.1/keytab/krb5.conf");
			final Configuration conf = HBaseConfiguration.create();
			conf.setBoolean(CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,true);
			conf.setBoolean("phoenix.schema.isNamespaceMappingEnabled", true);
			conf.set("hadoop.security.authentication", "Kerberos");
			conf.set("hbase.security.authentication", "Kerberos");
			conf.set("hbase.zookeeper.quorum", "master,slave1,slave2");
			conf.set("hbase.zookeeper.property.clientport", "2181");
			conf.set("hbase.master.kerberos.principal","hbase/master@HADOOP.COM");
			conf.set("hbase.regionserver.kerberos.principal","hbase/master@HADOOP.COM");

			Properties properties = new Properties();
			properties.setProperty("hbase.zookeeper.quorum", "10.1.3.24,10.1.3.25,10.1.3.26");
			properties.setProperty("hbase.master.kerberos.principal","hbase/_HOST@HADOOP.COM");
			properties.setProperty("hbase.master.keytab.file", "C:/data/SDP7.1/sdps/sdp7.1/keytab/1.hbase.service.keytab");
			properties.setProperty("hbase.regionserver.kerberos.principal","hbase/_HOST@HADOOP.COM");
			properties.setProperty("hbase.regionserver.keytab.file", "C:/data/SDP7.1/sdps/sdp7.1/keytab/master.1.hbase.service.keytab");
			properties.setProperty("phoenix.queryserver.kerberos.principal","HTTP/_HOST@HADOOP.COM");
			properties.setProperty("phoenix.queryserver.keytab.file", "C:/data/SDP7.1/sdps/sdp7.1/keytab/master.1.spnego.service.keytab");
			properties.setProperty("hbase.security.authentication", "kerberos");
			properties.setProperty("hadoop.security.authentication", "kerberos");
			properties.setProperty("zookeeper.znode.parent", "/hbase-secure");
			properties.setProperty("phoenix.schema.isNamespaceMappingEnabled","true");
			properties.setProperty("phoenix.schema.mapSystemTablesToNamespace","true");

			try {
				UserGroupInformation.setConfiguration(conf);
//				InstanceResolver.getSingleton(ConfigurationFactory.class,
//						new ConfigurationFactory() {
//							public Configuration getConfiguration() {
//								return conf;
//							}
//
//							public Configuration getConfiguration(
//									Configuration confToClone) {
//								Configuration copy = new Configuration(conf);
//								copy.addResource(confToClone);
//								return copy;
//							}
//						});
				Iterator<TokenIdentifier> iterator = UserGroupInformation.getCurrentUser().getTokenIdentifiers().iterator();

				UserGroupInformation userGroupInformation = UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, keytabPath);

				userGroupInformation.doAs(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						try {
							Connection conn = DriverManager
									.getConnection("jdbc:phoenix:10.1.3.24,10.1.3.25,10.1.3.26:2181:/hbase-secure",properties);
							ResultSet resultSet = conn.createStatement()
									.executeQuery(
											"select * from HDFS_NM.TB_DIR_INFO limit 10");
							while (resultSet.next()){
								String str = resultSet.getString(1);
								System.out.println(str);
							}
						} catch (Exception e) {
							log.error("添加hbase kerberos报错", e);
						}
						return "aaaaa";
					}
				});


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}