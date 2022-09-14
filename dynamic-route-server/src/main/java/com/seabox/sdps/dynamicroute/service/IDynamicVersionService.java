package com.seabox.sdps.dynamicroute.service;

import com.seabox.sdps.dynamicroute.entity.DynamicVersion;

import java.util.List;

public interface IDynamicVersionService {

	int add(DynamicVersion version);

	int update(DynamicVersion version);

	int delete(Long id);

	/**
	 * 获取最后一次发布的版本号
	 * 
	 * @return
	 */
	Long getLastVersion();

	// 获取所有的版本发布信息
	List<DynamicVersion> listAll();
}
