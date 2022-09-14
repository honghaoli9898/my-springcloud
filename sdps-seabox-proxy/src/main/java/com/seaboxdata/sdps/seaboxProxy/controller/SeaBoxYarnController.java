package com.seaboxdata.sdps.seaboxProxy.controller;

import java.util.List;
import java.util.Map;

import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationPage;
import com.seaboxdata.sdps.common.framework.bean.request.ApplicationRequest;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.YarnUtil;

/**
 * @author: Denny
 * @date: 2021/10/19 14:21
 * @desc:
 */
@Slf4j
@RestController
@RequestMapping("/yarn")
public class SeaBoxYarnController {
	@Autowired
	BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	BigdataVirtualHost bigdataVirtualHost;

	@Autowired
	private YarnUtil yarnUtil;

	/**
	 * 查询集群队列信息列表
	 *
	 * @return 集群返回的信息
	 */
	@GetMapping("/listScheduler")
	public JSONObject listScheduler(@RequestParam("clusterId") Integer clusterId) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listScheduler(clusterId);// 进阶实现
	}

    /**
     * 作业分析->任务列表
     * @param request
     * @return
     */
    @GetMapping("/listApps")
    public JSONObject listApps(ApplicationRequest request) {
        bigdataVirtualHost.setVirtualHost(request.getClusterId());
        yarnUtil.init(request.getClusterId());
        return yarnUtil.listApps(request.getClusterId());//优化实现
    }

	/**
	 * 查询集群资源配置信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 集群资源配置信息。
	 */
	@GetMapping("/listMetrics")
	public JSONObject listMetrics(@RequestParam("clusterId") Integer clusterId) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listMetrics(clusterId);// 优化实现
	}

	/**
	 * 查询集群各个节点信息
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/listNodes")
	public JSONObject listNodes(@RequestParam("clusterId") Integer clusterId) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listNodes(clusterId);// 优化实现
	}

	/**
	 * 修改集群队列信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @param jsonObject
	 *            待修改的队列参数
	 * @return 是否修改成功
	 */
	@PutMapping("/modifyQueue")
	public JSONObject modifyQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject jsonObject) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		yarnUtil.initAmbari(clusterId);
		JSONObject modifyQueue = yarnUtil.modifyQueue(clusterId, jsonObject);
		return modifyQueue;
	}

	/**
	 * 保存和刷新队列
	 *
	 * @return 新的队列json信息
	 */
	@PutMapping("/saveAndRefresh")
	public JSONObject saveAndRefresh(
			@RequestParam("clusterId") Integer clusterId) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		yarnUtil.initAmbariWithStartAndStop(clusterId);
		JSONObject responseJson = yarnUtil.saveAndRefresh(clusterId);
		return responseJson;
	}

	/**
	 * 保存和重启队列
	 *
	 * @return 新的队列json信息
	 */
	@PutMapping("/saveAndRestart")
	public JSONObject saveAndRestart(
			@RequestParam("clusterId") Integer clusterId) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		yarnUtil.initAmbariWithStartAndStop(clusterId);
		JSONObject responseJson = yarnUtil.saveAndRestart(clusterId);
		return responseJson;
	}

	/**
	 * 队列启停接口
	 *
	 * @param clusterId
	 *            集群ID
	 * @param jsonObject
	 *            传入参数
	 * @return 返回数据
	 */
	@PutMapping("/queueStartAndStop")
	public JSONObject queueStartAndStop(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject jsonObject) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		yarnUtil.initAmbari(clusterId);
		// JSONObject queueStateJson = yarnUtil.modifyQueue(clusterId,
		// jsonObject);
		JSONObject queueStateJson = yarnUtil.queueStartAndStop(clusterId,
				jsonObject);
		return queueStateJson;
	}

	/**
	 * 实时显示使用的核数和内存
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 集群使用的核数和内存数
	 */
	@GetMapping("/usedMemoryInfo")
	public List<ApplicationDTO> usedMemoryInfo(
			@RequestParam("clusterId") Integer clusterId, @RequestParam("topN") Integer topN, @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.usedMemoryInfo(clusterId, topN, startTime, endTime);
	}

	/**
	 * 根据用户筛选任务列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @return 该用户下的任务列表
	 */
	@GetMapping("/listAppsByUser")
	public JSONObject listAppsByUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listAppsByUser(clusterId, user);
	}

	/**
	 * 根据任务状态筛选任务列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @param states
	 *            任务状态
	 * @return 该状态下的任务列表
	 */
	@GetMapping("/listAppsByState")
	public JSONObject listAppsByState(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("states") String[] states) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listAppsByState(clusterId, states);
	}

	/**
	 * 根据用户和任务状态筛选任务列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @param states
	 *            任务状态
	 * @return 该综合状态下的任务列表
	 */
	@GetMapping("/listAppsByUserAndState")
	public JSONObject listAppsByUserAndState(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user,
			@RequestParam("states") String[] states) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		yarnUtil.init(clusterId);
		return yarnUtil.listAppsByUserAndState(clusterId, user, states);
	}

	/**
	 * 获取yarn的job状态统计
	 *
	 * @return
	 */
	@GetMapping("/getJobStatusCount")
	public JSONObject getJobStatusCount(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			String confJson = ambariUtil.getAmbariServerConfByConfName("YARN",
					CollUtil.newArrayList("yarn-site"));
			Map confMap = JSON.parseObject(confJson, Map.class);
			String resourceManagerHost = confMap.get(
					"yarn.resourcemanager.webapp.address").toString();
			ambariUtil.setUrl("http://".concat(resourceManagerHost));
			bigdataVirtualHost.setVirtualHost(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"yarn", "getJobStatueCount");
			JSONObject sourceResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			return sourceResult;
		} catch (Exception e) {
			return new JSONObject();
		}
	}

	/**
	 * 获取yarn的job状态统计
	 *
	 * @return
	 */
	@GetMapping("/getJobs")
	public JSONObject getJobs(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("status") String status,
			@RequestParam("queueName") String queueName) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			String confJson = ambariUtil.getAmbariServerConfByConfName("YARN",
					CollUtil.newArrayList("yarn-site"));
			Map confMap = JSON.parseObject(confJson, Map.class);
			String resourceManagerHost = confMap.get(
					"yarn.resourcemanager.webapp.address").toString();
			ambariUtil.setUrl("http://".concat(resourceManagerHost));
			bigdataVirtualHost.setVirtualHost(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"yarn", "getJobInfo");
			Map<Integer,Object> param = MapUtil.newHashMap();
			param.put(1, status);
			param.put(2, queueName);
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),param);
			return result;
		} catch (Exception e) {
			log.error("查询yarn队列运行任务报错",e);
			return new JSONObject();
		}
	}
}
