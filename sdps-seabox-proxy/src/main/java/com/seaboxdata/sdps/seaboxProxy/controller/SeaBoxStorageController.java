package com.seaboxdata.sdps.seaboxProxy.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.seaboxProxy.service.IStorageResourceService;

@Slf4j
@RestController
@RequestMapping("/seabox/storage/")
public class SeaBoxStorageController {

	@Autowired
	private IStorageResourceService storageResourceService;

	/**
	 * 得到项目存储信息
	 * 
	 * @param clusterId
	 * @param itemNames
	 * @return
	 */
	@PostMapping("/itemStorageResource")
	public PageResult<StorgeDirInfo> getItemStorage(
			@RequestBody StorgeRequest storgeRequest) {
		try {
			Page<StorgeDirInfo> result = storageResourceService
					.getItemStorage(storgeRequest);
			return PageResult.<StorgeDirInfo> builder().code(0)
					.count(result.getTotal()).data(result.getResult())
					.msg("操作成功").build();
		} catch (Exception e) {
			log.error("调用报错", e);
			return PageResult.<StorgeDirInfo> builder().code(1).msg("操作失败")
					.build();
		}
	}

	@PostMapping("/getFileStorageByTenant")
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			@RequestBody StorgeRequest storgeRequest) {
		try {
			Page<StorgeDirInfo> result = storageResourceService
					.getFileStorageByTenant(storgeRequest);
			return PageResult.<StorgeDirInfo> builder().code(0)
					.count(result.getTotal()).data(result.getResult())
					.msg("操作成功").build();
		} catch (Exception e) {
			log.error("根据tenant={},查询文件数据失败", storgeRequest.getTenant(), e);
			return PageResult.<StorgeDirInfo> builder().code(1).msg("操作失败")
					.build();
		}

	}

	@PostMapping("/subStorgeTrend")
	public PageResult<StorgeDirInfo> subStorgeTrend(
			@RequestBody StorgeRequest storgeRequest) {
		try {
			Page<StorgeDirInfo> result = storageResourceService
					.subStorgeTrend(storgeRequest);
			return PageResult.<StorgeDirInfo> builder().code(0)
					.count(result.getTotal()).data(result.getResult())
					.msg("操作成功").build();
 
		} catch (Exception e) {
			log.error("调用报错", e);
			return PageResult.<StorgeDirInfo> builder().code(1).msg("操作失败")
					.build();
		}
	}

	@PostMapping("/subStorgeRank")
	public PageResult<StorgeDirInfo> subStorgeRank(
			@RequestBody StorgeRequest storgeRequest) {
		try {
			Page<StorgeDirInfo> result = storageResourceService
					.subStorgeRank(storgeRequest);
			return PageResult.<StorgeDirInfo> builder().code(0)
					.count(result.getTotal()).data(result.getResult())
					.msg("操作成功").build();

		} catch (Exception e) {
			log.error("调用报错", e);
			return PageResult.<StorgeDirInfo> builder().code(1).msg("操作失败")
					.build();
		}
	}
}
