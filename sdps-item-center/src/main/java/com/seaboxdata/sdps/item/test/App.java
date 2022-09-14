package com.seaboxdata.sdps.item.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.security.UserGroupInformation;

import com.alibaba.fastjson.JSONObject;

import io.leopard.javahost.JavaHost;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class App {
	private static String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";
	// private static String CONNECTION_URL
	// ="jdbc:hive2://10.23.13.196:10000/ods;principal=hive/tw-manager@TDH";
	private static String CONNECTION_URL = "jdbc:hive2://master:2181,slave1:2181,slave2:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";
	private static String URL = "jdbc:hive2://10.1.2.16:2181,10.1.2.17:2181,10.1.2.15:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";
	private static String URL1 = "jdbc:hive2://10.1.3.113:2181,10.1.3.114:2181,10.1.3.115:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";
	
	static {
		try {
			Class.forName(JDBC_DRIVER);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Class.forName(JDBC_DRIVER);

		// 登录Kerberos账号
		System.setProperty("java.security.krb5.conf", "krb5.conf");

		Configuration configuration = new Configuration();
		configuration.set("hadoop.security.authentication", "Kerberos");
		configuration
				.setBoolean(
						CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
						true);
		UserGroupInformation.setConfiguration(configuration);
		UserGroupInformation.loginUserFromKeytab("hive/slave2@HADOOP.COM",
				"E:/kerberos/hive.service.2.keytab");
//		UserGroupInformation.loginUserFromKeytab("hdfs-seabox4@HADOOP.COM",
//				"E:/kerberos/hdfs.headless.keytab");
		Connection connection = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			connection = DriverManager.getConnection(CONNECTION_URL);
			ps = connection.prepareStatement("show tables");
			rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			JavaHost.updateVirtualDns("node16", "10.1.2.16");
			// 打印虚拟host
			JavaHost.printAllVirtualDns();
//			Properties properties = new Properties();
//			properties.setProperty(String.format(HIVE_JDBC_PASSWD_AUTH_PREFIX, "");
			connection = DriverManager.getConnection(URL,"hive","hive");
			ps = connection.prepareStatement("show tables");
			rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			JavaHost.updateVirtualDns("slave1", "10.1.3.114");
			// 打印虚拟host
			JavaHost.printAllVirtualDns();
//			Properties properties = new Properties();
//			properties.setProperty(String.format(HIVE_JDBC_PASSWD_AUTH_PREFIX, "");
			connection = DriverManager.getConnection(URL1,"hive","hive");
			ps = connection.prepareStatement("show tables");
			rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
