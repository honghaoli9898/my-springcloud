package com.seaboxdata.sdps.job.executor.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeDataInfo;
import com.seaboxdata.sdps.job.executor.mybatis.dto.TaskInfoDto;
import com.seaboxdata.sdps.job.executor.vo.TaskInfoRequest;
import org.apache.ibatis.annotations.Param;

public interface SdpsMergeDataInfoMapper extends BaseMapper<SdpsMergeDataInfo> {
}
