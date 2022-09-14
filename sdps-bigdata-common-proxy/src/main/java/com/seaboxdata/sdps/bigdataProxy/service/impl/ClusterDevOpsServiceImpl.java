package com.seaboxdata.sdps.bigdataProxy.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsClusterType;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsServerInfoDto;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterStatusMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterTypeMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.thread.GetWarningCntCallable;
import com.seaboxdata.sdps.bigdataProxy.thread.GetYarnAndHdfsMetricsCallable;
import com.seaboxdata.sdps.bigdataProxy.util.DictionaryUtil;
import com.seaboxdata.sdps.bigdataProxy.vo.DevOpsRequest;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterStatus;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;

@Service
public class ClusterDevOpsServiceImpl extends
		SuperServiceImpl<SdpsClusterMapper, SdpsCluster> implements
		IClusterDevOpsService {
	private static final long timeout = 5000;
	private static final long sleep = 100;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private SysGlobalArgsMapper sysGlobalArgsMapper;
	@Autowired
	private CommonBigData commonBigData;
	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;
	@Autowired
	DictionaryUtil dictionaryUtil;
	@Autowired
	private SdpsClusterMapper clusterMapper;
	@Autowired
	private SdpsClusterStatusMapper clusterStatusMapper;
	@Autowired
	private SdpsClusterTypeMapper clusterTypeMapper;

	@Override
	public List<SdpsCluster> getEnableClusterList(List<Integer> clusterIds) {
		QueryWrapper<SdpsCluster> queryWrapper = new QueryWrapper<SdpsCluster>()
				.select("cluster_id", "cluster_show_name")
				.isNotNull("server_id").eq("is_use", 1);
		if (CollUtil.isNotEmpty(clusterIds)) {
			queryWrapper.in("cluster_id", clusterIds);
		}
		List<SdpsCluster> clusterList = list(queryWrapper
				.orderByDesc("create_time"));
		if (CollUtil.isEmpty(clusterList)) {
			throw new BusinessException("查询的可用集群列表为空");
		}
		return clusterList;
	}

	@Override
	public JSONObject warningCnt(List<Integer> clusterIds) {
		List<SdpsCluster> clusterList = getEnableClusterList(clusterIds);
		Map<SdpsCluster, Future<Integer>> futureMap = MapUtil.newHashMap();
		GetWarningCntCallable getWarningCntCallable;
		for (SdpsCluster sdpsCluster : clusterList) {
			getWarningCntCallable = new GetWarningCntCallable(
					sdpsCluster.getClusterId(), commonBigData);
			futureMap.put(sdpsCluster,
					taskExecutor.submit(getWarningCntCallable));
		}
		Long startTime = System.currentTimeMillis();
		final JSONArray result = new JSONArray();
		while (true) {
			boolean isNotDone = futureMap.entrySet().stream()
					.anyMatch(entry -> !entry.getValue().isDone());
			if (!isNotDone) {
				break;
			}
			Long currTime = System.currentTimeMillis();
			if ((currTime - startTime) > timeout) {
				futureMap.forEach((k, v) -> {
					if (!v.isDone()) {
						v.cancel(true);
					}
				});
				break;
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				log.error("休息50ms报错");
			}
		}

		futureMap
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue().isDone())
				.forEach(
						entry -> {
							Integer cnt;
							try {
								cnt = entry.getValue().get();
							} catch (Exception e) {
								cnt = -1;
							}
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("clusteId", entry.getKey()
									.getClusterId());
							jsonObject.put("clusterShowName", entry.getKey()
									.getClusterShowName());
							jsonObject.put("cnt", cnt);
							result.add(jsonObject);
						});
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("count", clusterList.size());
		jsonObject.put("result", result);
		return jsonObject;
	}

	@Override
	public SysGlobalArgs selectGlobalArgs(String type, String key) {
		QueryWrapper<SysGlobalArgs> queryWrapper = new QueryWrapper<SysGlobalArgs>()
				.eq("arg_type", type);
		if (StrUtil.isNotBlank(key)) {
			queryWrapper.eq("arg_key", key);
		}
		return sysGlobalArgsMapper.selectOne(queryWrapper);
	}

	@Override
	public List<SysGlobalArgs> getGlobalParams(String type) {
		return sysGlobalArgsMapper.selectList(new QueryWrapper<SysGlobalArgs>()
				.eq("arg_type", type));
	}

	@Override
	public JSONObject getHdfsAndYarnMetrics(Integer page, Integer size,
			List<Integer> clusterIds) {
		PageHelper.startPage(page, size);
		Page<SdpsCluster> clusterList = (Page<SdpsCluster>) getEnableClusterList(clusterIds);
		Map<SdpsCluster, Future<JSONObject>> futureMap = MapUtil.newHashMap();
		GetYarnAndHdfsMetricsCallable getYarnAndHdfsMetricsCallable;
		for (SdpsCluster sdpsCluster : clusterList) {
			getYarnAndHdfsMetricsCallable = new GetYarnAndHdfsMetricsCallable(
					sdpsCluster.getClusterId(), commonBigData);
			futureMap.put(sdpsCluster,
					taskExecutor.submit(getYarnAndHdfsMetricsCallable));
		}
		Long startTime = System.currentTimeMillis();
		final JSONArray result = new JSONArray();
		while (true) {
			boolean isNotDone = futureMap.entrySet().stream()
					.anyMatch(entry -> !entry.getValue().isDone());
			if (!isNotDone) {
				break;
			}
			Long currTime = System.currentTimeMillis();
			if ((currTime - startTime) > timeout) {
				futureMap.forEach((k, v) -> {
					if (!v.isDone()) {
						v.cancel(true);
					}
				});
				break;
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				log.error("休息50ms报错");
			}
		}

		futureMap
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue().isDone())
				.forEach(
						entry -> {
							JSONObject jsonObj;
							try {
								jsonObj = entry.getValue().get();
							} catch (Exception e) {
								jsonObj = new JSONObject();
							}
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("clusteId", entry.getKey()
									.getClusterId());
							jsonObject.put("clusterShowName", entry.getKey()
									.getClusterShowName());
							jsonObject.put("metrics", jsonObj);
							result.add(jsonObject);
						});
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("count", clusterList.getTotal());
		jsonObject.put("result", result);
		return jsonObject;

	}

	@Override
	public JSONObject getClusterStackAndVersions(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData
				.getClusterStackAndVersions(clusterList.get(0).getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterStackAndVersionsNew(Integer clusterId,
			String reqRrepositoryVersion) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterStackAndVersionsNew(
				clusterList.get(0).getClusterId(), reqRrepositoryVersion);
		return result;
	}

	@Override
	public JSONObject getServiceUsersAndGroups(Integer clusterId,
			String services) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getServiceUsersAndGroups(clusterList
				.get(0).getClusterId(), services);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterHostInfo(Integer clusterId, String query) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterHostInfo(clusterList.get(0)
				.getClusterId(), query);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterHostDiskInfo(Integer clusterId, String query) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterHostDiskInfo(clusterList
				.get(0).getClusterId(), query);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterServiceAutoStart(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData
				.getClusterServiceAutoStart(clusterList.get(0).getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getServiceDisplayName(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getServiceDisplayName(clusterList
				.get(0).getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getServiceInstalled(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getServiceInstalled(clusterList
				.get(0).getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getWarningInfo(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getWarningInfo(clusterList.get(0)
				.getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getServiceWarningInfo(Integer clusterId,
			Integer definitionId, Integer from, Integer size) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getServiceWarningInfo(clusterList
				.get(0).getClusterId(), definitionId, from, size);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public Result updateServiceAutoStart(AmbariServiceAutoStartObj obj) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(obj.getClusterId()));
		return commonBigData.updateServiceAutoStart(obj);
	}

	@Override
	public JSONObject getClusterIp(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterIp(clusterList.get(0)
				.getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterHost(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterHost(clusterList.get(0)
				.getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject getClusterName(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getClusterName(clusterList.get(0)
				.getClusterId());
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject startOrStopService(AmbariStartOrStopServiceObj obj) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(obj.getClusterId()));
		JSONObject result = commonBigData.startOrStopService(obj);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", obj.getClusterId());
		return result;
	}

	@Override
	public JSONObject getComponentInfo(Integer clusterId) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(clusterId));
		JSONObject result = commonBigData.getComponentInfo(clusterId);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", clusterId);
		return result;
	}

	@Override
	public JSONObject restartAllComponent(JSONObject data) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(CollUtil.newArrayList(data
						.getInteger("clusterId"))));
		JSONObject result = commonBigData.restartAllComponent(data);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", data.getInteger("clusterId"));
		return result;
	}

	@Override
	public JSONObject startOrStopComponent(JSONObject data) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(CollUtil.newArrayList(data
						.getInteger("clusterId"))));
		JSONObject result = commonBigData.startOrStopComponent(data);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", data.getInteger("clusterId"));
		return result;
	}

	@Override
	public JSONObject restartComponent(JSONObject data) {
		List<SdpsCluster> clusterList = getEnableClusterList(CollUtil
				.newArrayList(CollUtil.newArrayList(data
						.getInteger("clusterId"))));
		JSONObject result = commonBigData.restartComponent(data);
		result.put("clusterShowName", clusterList.get(0).getClusterShowName());
		result.put("clusteId", data.getInteger("clusterId"));
		return result;
	}

	@Override
	public void saveServerInfo(SdpsServerInfo serverInfo) {
		SysGlobalArgs sysGlobalArgs = selectGlobalArgs("password", "publicKey");
		String pass = serverInfo.getPasswd();
		if (StrUtil.isNotBlank(pass)) {
			pass = RsaUtil.encrypt(pass, sysGlobalArgs.getArgValue());
		}
		serverInfo.setPasswd(pass);
		serverInfoMapper.insert(serverInfo);
	}

	@Override
	public PageResult getServerInfoPage(PageRequest<DevOpsRequest> request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SdpsServerInfoDto> result = serverInfoMapper
				.selectServerInfoPage(request.getParam());
		return PageResult.<SdpsServerInfoDto> builder().code(0)
				.count(result.getTotal()).data(result.getResult()).msg("操作成功")
				.build();
	}

	@Override
	public void updateServerInfo(SdpsServerInfo serverInfo) {
		SdpsServerInfo sdpsServerInfo = serverInfoMapper.selectById(serverInfo
				.getId());
		if (!StrUtil.equalsIgnoreCase(sdpsServerInfo.getPasswd(),
				serverInfo.getPasswd())) {
			String publicKey = sysGlobalArgsMapper.selectOne(
					new QueryWrapper<SysGlobalArgs>()
							.eq("arg_type", "password").eq("arg_key",
									"publicKey")).getArgValue();
			serverInfo.setPasswd(RsaUtil.encrypt(serverInfo.getPasswd(),
					publicKey));
		}
		serverInfoMapper.updateById(serverInfo);
	}

	@Override
	public void deleteServerInfoById(Long id) {
		serverInfoMapper.deleteById(id);
	}

	@Override
	@Transactional
	public void addClusterInfo(SdpsCluster sdpsCluster) {
		clusterMapper.insert(sdpsCluster);
		sdpsCluster.setServerId(sdpsCluster.getClusterId().toString());
		clusterMapper.update(
				sdpsCluster,
				new UpdateWrapper<SdpsCluster>().eq("cluster_id",
						sdpsCluster.getClusterId()));
	}

	@Override
	public List<SdpsClusterType> getClusterTypeList() {
		return clusterTypeMapper.selectList(null);
	}

	@Override
	public List<SdpsClusterStatus> getClusterStatusList() {
		return clusterStatusMapper.selectList(null);
	}

	@Override
	public void updateClusterInfo(SdpsCluster sdpsCluster) {
		clusterMapper.update(
				sdpsCluster,
				new UpdateWrapper<SdpsCluster>().eq("cluster_id",
						sdpsCluster.getClusterId()));
	}

	@Override
	public void removeCluster(Integer clusterId) {
		clusterMapper.delete(new UpdateWrapper<SdpsCluster>().eq("cluster_id",
				clusterId));
	}

}
