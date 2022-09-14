package com.seabox.sdps.dynamicroute.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;


@RestController
@RequestMapping("/api")
public class ServerController {
	@Autowired
	private DiscoveryClient discoveryClient;

	@RequestMapping("getServicesList")
	public Object getServicesList() {
		List<List<ServiceInstance>> servicesList = new ArrayList<>();
		// 获取服务名称
		List<String> serviceNames = discoveryClient.getServices();
		for (String serviceName : serviceNames) {
			// 获取服务中的实例列表
			List<ServiceInstance> serviceInstances = discoveryClient
					.getInstances(serviceName);
			servicesList.add(serviceInstances);
		}
		return servicesList;
	}

	@RequestMapping("getServices")
	public Object getServicesList(
			@RequestParam(name = "name", required = true) String name) {
		return getList(name);
	}

	public Object getList(String targetName) {
		if (!StrUtil.isBlank(targetName)) {
			targetName = targetName.trim();
		}
		List<Map<String, Object>> servicesList = new ArrayList<>();
		// 获取服务名称
		List<String> serviceNames = discoveryClient.getServices();
		for (String serviceName : serviceNames) {
			// 获取服务中的实例列表
			List<ServiceInstance> serviceInstances = discoveryClient
					.getInstances(serviceName);
			for (ServiceInstance serviceInstance : serviceInstances) {
				String serviceInstanceStr = JSON.toJSONString(serviceInstance);
				if (serviceInstanceStr != null) {
					Map<String, Object> serviceInstanceMap = (Map<String, Object>) JSON
							.parse(serviceInstanceStr);
					if (serviceInstanceMap != null) {
						Map<String, Object> instanceInfoMap = (Map<String, Object>) JSON
								.parse(serviceInstanceMap.get("instanceInfo")
										.toString());
						String appName = (String) instanceInfoMap
								.get("appName");
						if (targetName == null) {
							add(servicesList, appName, instanceInfoMap);
						} else if (StrUtil.containsAnyIgnoreCase(appName,
								targetName)) {
							add(servicesList, appName, instanceInfoMap);
						}
					}
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", 0);
		map.put("count", servicesList.size());
		map.put("data", servicesList);
		return map;
	}

	private void add(List<Map<String, Object>> servicesList, String appName,
			Map<String, Object> instanceInfoMap) {
		servicesList.add(instanceInfoMap);
	}
}