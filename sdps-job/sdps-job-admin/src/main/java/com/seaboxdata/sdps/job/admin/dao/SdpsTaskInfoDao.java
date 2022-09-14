package com.seaboxdata.sdps.job.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.job.admin.core.model.SdpsTaskInfo;
import com.seaboxdata.sdps.job.admin.core.model.SdpsTaskInfoExample;

@Mapper
public interface SdpsTaskInfoDao {
	long countByExample(SdpsTaskInfoExample example);

	int deleteByExample(SdpsTaskInfoExample example);

	int deleteByPrimaryKey(Long id);

	int insert(SdpsTaskInfo record);

	int insertSelective(SdpsTaskInfo record);

	List<SdpsTaskInfo> selectByExample(SdpsTaskInfoExample example);

	SdpsTaskInfo selectByPrimaryKey(Long id);

	int updateByExampleSelective(@Param("record") SdpsTaskInfo record,
			@Param("example") SdpsTaskInfoExample example);

	int updateByExample(@Param("record") SdpsTaskInfo record,
			@Param("example") SdpsTaskInfoExample example);

	int updateByPrimaryKeySelective(SdpsTaskInfo record);

	int updateByPrimaryKey(SdpsTaskInfo record);
}