package com.seaboxdata.sdps.item.mapper;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.model.SdpsFileManager;
import com.seaboxdata.sdps.item.vo.script.ScriptRequest;

public interface SdpsFileManagerMapper extends SuperMapper<SdpsFileManager> {
	public Page<SdpsFileManager> pageList(
			@Param("request") ScriptRequest request);
}