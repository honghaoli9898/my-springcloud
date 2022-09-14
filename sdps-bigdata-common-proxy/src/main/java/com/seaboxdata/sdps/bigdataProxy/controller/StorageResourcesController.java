package com.seaboxdata.sdps.bigdataProxy.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.bigdataProxy.service.ISdpsHdfsDbTableService;
import com.seaboxdata.sdps.bigdataProxy.service.IStroageResourcesService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

@RestController
@RequestMapping("/storageResource")
public class StorageResourcesController {
	@Autowired
	private IStroageResourcesService stroageResourcesService;
	@Autowired
	private ISdpsHdfsDbTableService hdfsDbTableService;

	@GetMapping("/getItemStorage")
	public PageResult<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		if (Objects.isNull(storgeRequest.getPage())
				|| Objects.isNull(storgeRequest.getClusterId())
				|| Objects.isNull(storgeRequest.getSize())
				|| Objects.isNull(storgeRequest.getPeriod())) {
			return PageResult.<StorgeDirInfo> builder().code(1).msg("参数上送错误")
					.build();
		}
		return stroageResourcesService.getItemStorage(storgeRequest);
	}

	@GetMapping("/getFileStorageByTenant")
	public PageResult<StorgeDirInfo> getFileStorageByTenant(
			StorgeRequest storgeRequest) {
		if (Objects.isNull(storgeRequest.getPage())
				|| Objects.isNull(storgeRequest.getSize())
				|| Objects.isNull(storgeRequest.getPeriod())
				|| Objects.isNull(storgeRequest.getClusterId())
				|| Objects.isNull(storgeRequest.getTenant())) {
			return PageResult.<StorgeDirInfo> builder().code(1).msg("参数上送错误")
					.build();
		}
		return stroageResourcesService.getFileStorageByTenant(storgeRequest);
	}

	@GetMapping("/subStorgeTrend")
	public PageResult<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		if (Objects.isNull(storgeRequest.getPage())
				|| Objects.isNull(storgeRequest.getSize())
				|| StrUtil.isBlank(storgeRequest.getEndDay())
				|| StrUtil.isBlank(storgeRequest.getStartDay())
				|| Objects.isNull(storgeRequest.getStorageType())) {
			return PageResult.<StorgeDirInfo> builder().code(1).msg("参数上送错误")
					.build();
		}
		return stroageResourcesService.subStorgeTrend(storgeRequest);
	}

	@GetMapping("/subStorgeRank")
	public PageResult<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		if (Objects.isNull(storgeRequest.getPage())
				|| Objects.isNull(storgeRequest.getClusterId())
				|| Objects.isNull(storgeRequest.getSize())
				|| Objects.isNull(storgeRequest.getStorageType())) {
			return PageResult.<StorgeDirInfo> builder().code(1).msg("参数上送错误")
					.build();
		}
		return stroageResourcesService.subStorgeRank(storgeRequest);
	}

	@PostMapping("/getDbOrTableList")
	public PageResult<SdpsHdfsDbTable> getDbOrTableList(
			@RequestBody StorgeRequest storgeRequest) {
		if (StrUtil.isBlank(storgeRequest.getEndDay())
				|| Objects.isNull(storgeRequest.getPage())
				|| Objects.isNull(storgeRequest.getClusterId())
				|| Objects.isNull(storgeRequest.getSize())
				|| Objects.isNull(storgeRequest.getStorageType())) {
			return PageResult.<SdpsHdfsDbTable> builder().code(1).msg("参数上送错误")
					.build();
		}
		return hdfsDbTableService.getDbOrTableList(storgeRequest);
	}
}
