package com.seaboxdata.sdps.job.executor.service;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.job.executor.mybatis.dto.TaskInfoDto;
import com.seaboxdata.sdps.job.executor.vo.PageTaskInfoRequest;

public interface SdpsJobService extends ISuperService<SdpsTaskInfo> {
	Page<TaskInfoDto> getPageList(PageTaskInfoRequest request);
}
