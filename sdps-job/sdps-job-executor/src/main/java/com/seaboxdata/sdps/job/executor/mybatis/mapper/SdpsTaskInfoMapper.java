package com.seaboxdata.sdps.job.executor.mybatis.mapper;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.job.executor.mybatis.dto.TaskInfoDto;
import com.seaboxdata.sdps.job.executor.vo.TaskInfoRequest;

public interface SdpsTaskInfoMapper extends SuperMapper<SdpsTaskInfo> {
	public Page<TaskInfoDto> pageList(@Param("request") TaskInfoRequest request);
}