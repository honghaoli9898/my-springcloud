package com.seaboxdata.sdps.user.feign;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.model.ExternalRole;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;

@FeignClient(value = "bigdataCommon-proxy-server")
public interface BigdataCommonFegin {
	@PostMapping("/bigdataCommon/addRangerUser")
	public Result addRangerUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<VXUsers> rangerObjList);

	@GetMapping("/bigdataCommon/getRangerUserByName")
	public Result getRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	@PutMapping("/bigdataCommon/updateRangerUserByName")
	public Result updateRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXUsers rangerUserObj);

	@DeleteMapping("/bigdataCommon/deleteRangerUserByName")
	public Result deleteRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName);

	/**
	 * 创建url role关系
	 * 
	 * @param role
	 * @return
	 */
	@PostMapping("/url/addRoleRelation")
	public Result addRoleRelation(@RequestBody RoleExternal role);

	@GetMapping("/externalRole/getExternalRoles")
	public Result getExternalRoles(@RequestBody ExternalRole role);

	/**
	 * 通过urlid获取关联用户
	 * 
	 * @param urlId
	 * @return
	 */
	@PostMapping("/url/getUsersByUrlId")
	public Set<String> getUsersByUrlId(@RequestParam("urlId") Long urlId);

	@PostMapping("/bigdataCommon/addAmbariUser")
	public Result addAmbariUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);

	@DeleteMapping("/bigdataCommon/deleteAmbariUser")
	public Result deleteAmbariUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("username") String username);

	@PutMapping("/bigdataCommon/updateAmbariUserPassword")
	public Result updateAmbariUserPassword(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser);

	@GetMapping("/bigdataCommon/getServerConfByConfName")
	String getServerConfByConfName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serverName") String serverName,
			@RequestParam("confStrs") ArrayList<String> confStrs);
	
	@PostMapping("/bigdataCommon/upateYarnQueueConfigurate")
	public Result updateYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) ;
	
	@GetMapping("/bigdataCommon/getYarnQueueConfigurate")
	public Result getYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId);
	@PostMapping("/bigdataCommon/deleteYarnQueueConfigurate")
	public Result deleteYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

	/**
	 * 新增yarn queue配置信息
	 */
	@PostMapping("/bigdataCommon/insertYarnQueueConfigurate")
	public Result insertYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);
}
