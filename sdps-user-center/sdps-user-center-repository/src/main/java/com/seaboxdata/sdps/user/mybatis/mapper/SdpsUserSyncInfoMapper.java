package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.SdpsUserSyncInfo;

public interface SdpsUserSyncInfoMapper extends SuperMapper<SdpsUserSyncInfo> {
	List<SdpsUserSyncInfo> selectUserSyncInfo(@Param("userId") Long userId);
}