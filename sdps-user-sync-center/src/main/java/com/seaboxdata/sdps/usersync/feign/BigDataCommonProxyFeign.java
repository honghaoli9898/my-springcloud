package com.seaboxdata.sdps.usersync.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONArray;
import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.usersync.feign.fallback.BigDataProxyServiceFallbackFactory;

@FeignClient(name = ServiceNameConstants.BIGDATA_PROXY_SERVICE, fallbackFactory = BigDataProxyServiceFallbackFactory.class, decode404 = true, configuration = MultipartSupportConfig.class)
public interface BigDataCommonProxyFeign {
	@PostMapping("/bigdataCommon/addRangerUser")
	public Result addRangerUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<VXUsers> rangerObjList);

	@PostMapping("/bigdataCommon/createItemResource")
	public Result createItemResource(@RequestParam("itemIden") String itemIden,
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@DeleteMapping("/bigdataCommon/deleteRangerGroupByName")
	public Result deleteRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName);

	@PostMapping("/bigdataCommon/addUsersToGroup")
	public Result addUsersToGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers);

	@DeleteMapping("/bigdataCommon/deleteUsersToGroup")
	public Result deleteUsersToGroup(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers);

	@PostMapping("/bigdataCommon/addRangerPolicy")
	public Result addRangerPolicy(@RequestBody RangerPolicyObj rangerPolicyObj);

	@DeleteMapping("/bigdataCommon/deleteRangerPolicy")
	public Result deleteRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName);

	@PostMapping("/bigdataCommon/addAmbariUser")
	public Result addAmbariUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);

	@DeleteMapping("/bigdataCommon/deleteAmbariUser")
	public Result deleteAmbariUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("username") String username);

	@DeleteMapping("/bigdataCommon/deleteRangerUserByName")
	public Result deleteRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	@GetMapping("/bigdataCommon/getRangerUserByName")
	public Result getRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	@PutMapping("/bigdataCommon/updateRangerUserByName")
	public Result updateRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXUsers rangerUserObj);

	@PutMapping("/bigdataCommon/updateAmbariUserPassword")
	public Result updateAmbariUserPassword(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);
	
	@GetMapping("/bigdataCommon/getAmbariUsers")
	public Result<JSONArray> getAmbariUsers(
			@RequestParam("clusterId") Integer clusterId);

	@GetMapping("/config/getComponentAndHost")
	public Result getComponentAndHost(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName);

}
