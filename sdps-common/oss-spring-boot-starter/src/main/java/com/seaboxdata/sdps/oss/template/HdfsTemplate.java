package com.seaboxdata.sdps.oss.template;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.oss.model.HadoopClient;
import com.seaboxdata.sdps.oss.model.ObjectInfo;
import com.seaboxdata.sdps.oss.properties.FileServerProperties;
import com.seaboxdata.sdps.oss.properties.HadoopProperties;
import com.seaboxdata.sdps.oss.utils.FileUtil;

/**
 * FastDFS配置
 *
 * @author zlt
 * @date 2021/2/11
 *       <p>
 *       Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
@ConditionalOnClass(FileSystem.class)
@ConditionalOnProperty(prefix = FileServerProperties.PREFIX, name = "type", havingValue = FileServerProperties.TYPE_HDFS)
@Slf4j
public class HdfsTemplate {
	@Autowired
	private HadoopProperties hadoopProperties;

	/**
	 * 配置
	 */
	public org.apache.hadoop.conf.Configuration getConfiguration(
			HadoopProperties hadoopProperties) {
		// 读取配置文件
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		conf.set("dfs.replication", hadoopProperties.getReplication());
		conf.set("fs.defaultFS", hadoopProperties.getNameNode());
		conf.set("mapred.job.tracker", hadoopProperties.getNameNode());
		return conf;
	}

	@Bean
	public FileSystem fs() {
		// 文件系统
		FileSystem fs = null;
		try {
			URI uri = new URI(hadoopProperties.getDirectoryPath().trim());
			fs = FileSystem.get(uri, this.getConfiguration(hadoopProperties));
		} catch (Exception e) {
			log.error("【FileSystem配置初始化失败】", e);
		}
		return fs;
	}

	@Bean
	public HadoopClient hadoopClient(FileSystem fs,
			HadoopProperties hadoopProperties) {
		return new HadoopClient(fs, hadoopProperties);
	}

	@SneakyThrows
	public ObjectInfo upload(MultipartFile file, String destPath) {
		return upload(FileUtil.MultipartFileToFile(file).getPath(), destPath);
	}

	@Autowired
	private HadoopClient hadoopClient;

	/**
	 * 上传对象
	 * 
	 * @param objectName
	 *            对象名
	 * @param is
	 *            对象流
	 * @param size
	 *            大小
	 */
	private ObjectInfo upload(String srcFile, String destPath) {
		hadoopClient.copyFileToHDFS(hadoopProperties.isDelSrc(),
				hadoopProperties.isOverwrite(), srcFile, destPath);
		ObjectInfo obj = new ObjectInfo();
		obj.setObjectPath(destPath);
		obj.setObjectUrl(hadoopProperties.getNameNode().endsWith("/") ? hadoopProperties
				.getNameNode().substring(0,
						hadoopProperties.getNameNode().length() - 1)
				: hadoopProperties.getNameNode() + destPath);
		return obj;
	}

	/**
	 * 删除对象
	 * 
	 * @param objectPath
	 *            对象路径
	 */
	public boolean delete(String objectPath) {
		if (!StringUtils.isEmpty(objectPath)) {
			String[] fileNames = objectPath.split("/");
			String fileName = fileNames[fileNames.length - 1];
			String path = objectPath.substring(0, fileName.length());
			return hadoopClient.rmdir(path, fileName);
		}
		return false;
	}

	/**
	 * 下载对象
	 * 
	 * @param objectPath
	 *            对象路径
	 * @param callback
	 *            回调
	 * @throws IOException
	 */
	public void download(String objectPath, OutputStream outputStream)
			throws IOException {
		if (!StringUtils.isEmpty(objectPath)) {
			String[] fileNames = objectPath.split("/");
			String fileName = fileNames[fileNames.length - 1];
			String path = objectPath.substring(0, fileName.length());
			hadoopClient.download(path, fileName, outputStream);
		}
	}
}
