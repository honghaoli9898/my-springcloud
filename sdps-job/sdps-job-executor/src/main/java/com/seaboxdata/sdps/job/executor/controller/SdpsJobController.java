package com.seaboxdata.sdps.job.executor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.job.executor.mybatis.dto.TaskInfoDto;
import com.seaboxdata.sdps.job.executor.service.SdpsJobService;
import com.seaboxdata.sdps.job.executor.vo.PageTaskInfoRequest;

@RestController
@RequestMapping("/api")
public class SdpsJobController {
	@Autowired
	private SdpsJobService sdpsJobService;

	@PostMapping("/page")
	public PageResult<TaskInfoDto> getPageList(
			@RequestBody PageTaskInfoRequest request) {
		Page<TaskInfoDto> result = sdpsJobService.getPageList(request);
		return PageResult.<TaskInfoDto> builder().code(0).msg("操作成功")
				.data(result.getResult()).count(result.getTotal()).build();
	}
}
