package com.seaboxdata.sdps.bigdataProxy.service.impl;

import org.springframework.stereotype.Service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsHdfsDbTableMapper;
import com.seaboxdata.sdps.bigdataProxy.service.ISdpsHdfsDbTableService;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

@Service
public class SdpsHdfsDbTableServiceImpl extends
		SuperServiceImpl<SdpsHdfsDbTableMapper, SdpsHdfsDbTable> implements
		ISdpsHdfsDbTableService {

	@Override
	public PageResult<SdpsHdfsDbTable> getDbOrTableList(
			StorgeRequest storgeRequest) {
		// DateTime startDay = DateUtil.offsetDay(DateUtil.parse(
		// storgeRequest.getEndDay(), CommonConstant.SIMPLE_DATE_FORMAT),
		// 1);
		// DateTime endDay = DateUtil.offsetDay(startDay, 1);
		String type = storgeRequest.getStorageType().name().toLowerCase();
		storgeRequest.setType(type);
		PageHelper.startPage(storgeRequest.getPage(), storgeRequest.getSize());
		Page<SdpsHdfsDbTable> result = this.baseMapper
				.selectDbOrTablePage(storgeRequest);
		return PageResult.<SdpsHdfsDbTable> builder().code(0).msg("操作成功")
				.count(result.getTotal()).data(result.getResult()).build();
	}
}
