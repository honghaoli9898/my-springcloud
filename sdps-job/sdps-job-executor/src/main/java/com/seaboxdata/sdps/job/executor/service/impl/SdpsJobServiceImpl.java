package com.seaboxdata.sdps.job.executor.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskConfig;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.job.executor.constant.SdpsJobConstant;
import com.seaboxdata.sdps.job.executor.mybatis.dto.TaskInfoDto;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsTaskConfigMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsTaskInfoMapper;
import com.seaboxdata.sdps.job.executor.service.SdpsJobService;
import com.seaboxdata.sdps.job.executor.vo.PageTaskInfoRequest;
import com.seaboxdata.sdps.job.executor.vo.TaskInfoRequest;

@Service
public class SdpsJobServiceImpl extends
		SuperServiceImpl<SdpsTaskInfoMapper, SdpsTaskInfo> implements
		SdpsJobService {
	@Autowired
	private SdpsTaskConfigMapper taskConfigMapper;
	@Autowired
	private SdpsClusterMapper clusterMapper;

	@Override
	public Page<TaskInfoDto> getPageList(PageTaskInfoRequest request) {
		TaskInfoRequest taskInfoRequest = request.getParam();
		List<Integer> typeList = null;
		if (Objects.isNull(taskInfoRequest.getClusterId())) {
			List<SdpsCluster> sdpsClusters = clusterMapper
					.selectList(new QueryWrapper<SdpsCluster>()
							.select("distinct cluster_type_id"));
			typeList = sdpsClusters.stream().map(SdpsCluster::getClusterTypeId)
					.collect(Collectors.toList());
		} else {
			SdpsCluster sdpsCluster = clusterMapper
					.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
							taskInfoRequest.getClusterId()));
			typeList = CollUtil.newArrayList(sdpsCluster.getClusterTypeId());

		}
		QueryWrapper<SdpsTaskConfig> queryWrapper = new QueryWrapper<SdpsTaskConfig>()
				.select("arg_value").in("cluster_type_id", typeList)
				.eq("arg_key", "xxl-job-map-id");
		switch (taskInfoRequest.getType()) {
		case 1:
			queryWrapper.eq("task_type",
					SdpsJobConstant.HDFS_META_DATA_ANALYSIS_JOB);
			break;
		case 2:
			queryWrapper.eq("task_type", SdpsJobConstant.HDFS_MERGE_FILE_JOB);
			break;
		default:
			throw new BusinessException("该类型暂不支持");
		}
		List<SdpsTaskConfig> sdpsTaskConfigs = taskConfigMapper
				.selectList(queryWrapper);
		List<Integer> xxlJobIds = sdpsTaskConfigs.stream()
				.map(SdpsTaskConfig::getArgValue)
				.map(data -> Integer.valueOf(data))
				.collect(Collectors.toList());
		taskInfoRequest.setXxlJobIds(xxlJobIds);
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<TaskInfoDto> result = this.baseMapper.pageList(taskInfoRequest);
		result.forEach(data -> {
			Integer handleCode = data.getHandleCode();
			if (Objects.nonNull(handleCode)) {
				if (handleCode == 200) {
					data.setStatus(0);
				} else if (handleCode == 0) {
					data.setStatus(3);
				} else {
					String handleMsg = StrUtil.isBlank(data.getHandleMsg()) ? "返回为空"
							: data.getHandleMsg();
					if (StrUtil.containsIgnoreCase(handleMsg.trim(),"人为操作，主动终止:")) {
						data.setStatus(2);
					} else {
						data.setStatus(1);
					}
				}

			} else {
				Integer triggerCode = data.getTriggerCode();
				if (Objects.nonNull(triggerCode)) {
					if (triggerCode == 200) {
						data.setStatus(3);
					} else {
						data.setStatus(1);
					}
				} else {
					data.setStatus(4);
				}
			}
		});
		return result;
	}
}
