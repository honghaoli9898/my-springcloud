package com.seaboxdata.sdps.oss.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * hdfs配置文件
 * 
 * @author 11302
 *
 */
@Getter
@Setter
public class HadoopProperties {
	/** fs uri地址 */
	private String nameNode = "hdfs://127.0.0.1:9000";
	/** 默认文件夹 */
	private String directoryPath = "/";
	private String replication = "3";
	private boolean delSrc = false;
	private boolean overwrite = false;
	private String defaultUploadPath = "/user/upload/";
	public String getDirectoryPath() {
		StringBuilder sb = new StringBuilder(directoryPath);
		if (!(directoryPath.indexOf("/") == directoryPath.length())) {
			sb.append("/");
		}
		return sb.toString();
	}

	public String getPath() {
		return this.nameNode + this.directoryPath;
	}
}
