package com.seaboxdata.sdps.item.service;

import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;

public interface FileService {
	Object upload(MultipartFile file, SdpsServerInfo sdpsServerInfo,
			Integer clusterId, String... args);

	void download(String path, SdpsServerInfo sdpsServerInfo,
			Integer clusterId, String serverType);

	void delete(String path, SdpsServerInfo sdpsServerInfo, Integer clusterId,
			String serverType, String username);
}
