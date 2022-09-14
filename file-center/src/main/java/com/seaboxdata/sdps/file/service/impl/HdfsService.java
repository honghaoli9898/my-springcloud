package com.seaboxdata.sdps.file.service.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.oss.model.ObjectInfo;
import com.seaboxdata.sdps.oss.properties.FileServerProperties;
import com.seaboxdata.sdps.oss.template.HdfsTemplate;

/**
 * @author zlt
 * @date 2021/2/13
 *       <p>
 *       Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = FileServerProperties.PREFIX, name = "type", havingValue = FileServerProperties.TYPE_HDFS)
public class HdfsService extends AbstractIFileService {
	@Resource
	private HdfsTemplate hdfsTemplate;

	@Override
	protected String fileType() {
		return FileServerProperties.TYPE_FDFS;
	}

	@Override
	protected ObjectInfo uploadFile(MultipartFile file, String... args) {
		return hdfsTemplate.upload(file, args[0]);
	}

	@Override
	protected void deleteFile(String objectPath) {
		hdfsTemplate.delete(objectPath);
	}

	@Override
	public void out(String id, OutputStream os) {
		try {
			hdfsTemplate.download(id, os);
		} catch (IOException e) {
			log.error("下载文件:" + id + "失败", e);
		}
	}
}
