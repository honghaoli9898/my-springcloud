package com.seaboxdata.sdps.item.utils;

import java.sql.DriverManager;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.item.enums.DbTypeEnum;

public class DbUtil {
	// 测试数据源连接是否有效
	public static boolean testDatasource(String driveClass, String url,
			String username, String password) {
		try {
			Class.forName(driveClass);
			DriverManager.getConnection(url, username, password);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getUrl(String address, Integer port, String dbName,
			String databaseType, String param) {
		String url = null;
		StringBuffer sb = new StringBuffer();
		if (Objects.equals(databaseType, DbTypeEnum.M.name())) {
			sb.append("jdbc:mysql://").append(address).append(":").append(port)
					.append("/").append(dbName);
			if (StrUtil.isNotBlank(param)) {
				sb.append(param);
			} else {
				sb.append("?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&allowMultiQueries=true");
			}
			url = sb.toString();
		} else if (Objects.equals(databaseType, DbTypeEnum.O.name())) {
			url = sb.append("jdbc:oracle:thin:@").append(address).append(":")
					.append(port).append("/").append(dbName).append(param).toString();
		} else if (Objects.equals(databaseType, DbTypeEnum.P.name())) {
			url = sb.append("jdbc:postgresql://").append(address).append(":")
					.append(port).append("/").append(dbName).append(param).toString();
		} else if (Objects.equals(databaseType, DbTypeEnum.S.name())) {
			url = sb.append("jdbc:sqlserver://").append(address).append(":")
					.append(port).append(";DatabaseName=").append(dbName).append(param)
					.toString();
		} else if (Objects.equals(databaseType, DbTypeEnum.X.name())) {
			url = sb.append("jdbc:sqlserver://").append(address).append(":")
					.append(port).append(";DatabaseName=").append(dbName).append(param)
					.toString();
		} else if (Objects.equals(databaseType, DbTypeEnum.H.name())) {
			url = sb.append("jdbc:hive2://").append(address).append(":")
					.append(port).append("/").append(dbName).append(param).toString();
		}
		return url;
	}

	public static String getDriverClassNameByType(String databaseType) {
		String driverClassName = null;
		if (Objects.equals(databaseType, DbTypeEnum.M.name())) {
			driverClassName = DbTypeEnum.M.getMessage();
		} else if (Objects.equals(databaseType, DbTypeEnum.O.name())) {
			driverClassName = DbTypeEnum.O.getMessage();
		} else if (Objects.equals(databaseType, DbTypeEnum.P.name())) {
			driverClassName = DbTypeEnum.P.getMessage();
		} else if (Objects.equals(databaseType, DbTypeEnum.S.name())) {
			driverClassName = DbTypeEnum.S.getMessage();
		} else if (Objects.equals(databaseType, DbTypeEnum.X.name())) {
			driverClassName = DbTypeEnum.X.getMessage();
		} else if (Objects.equals(databaseType, DbTypeEnum.H.name())) {
			driverClassName = DbTypeEnum.H.getMessage();
		}
		return driverClassName;
	}
}
