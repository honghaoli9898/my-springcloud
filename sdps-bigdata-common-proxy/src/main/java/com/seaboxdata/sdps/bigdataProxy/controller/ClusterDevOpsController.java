package com.seaboxdata.sdps.bigdataProxy.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsClusterType;
import com.seaboxdata.sdps.bigdataProxy.bean.SdpsServerInfoDto;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.vo.DevOpsRequest;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterStatus;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;

@RestController
@RequestMapping("/devops")
public class ClusterDevOpsController {
	@Autowired
	private IClusterDevOpsService clusterevOpsService;

	@PostMapping("/getServerInfoPage")
	public PageResult getServerInfoPage(@LoginUser SysUser sysUser,
			@RequestBody PageRequest<DevOpsRequest> request) {
		if (StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			return clusterevOpsService.getServerInfoPage(request);
		}
		return PageResult.<SdpsServerInfoDto> builder().code(0).count(0L)
				.data(CollUtil.newArrayList()).msg("????????????").build();

	}

	@PostMapping("/updateServerInfo")
	public Result updateServerInfo(@LoginUser SysUser sysUser,
			@RequestBody SdpsServerInfo serverInfo) {
		if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			return Result.failed("????????????????????????");
		}
		if (Objects.isNull(serverInfo.getServerId())
				|| Objects.isNull(serverInfo.getId())
				|| Objects.isNull(serverInfo.getType())) {
			Result.failed("???????????????????????????");
		}
		clusterevOpsService.updateServerInfo(serverInfo);
		return Result.succeed("????????????");
	}

	@DeleteMapping("/deleteServerInfo/{id}")
	public Result deleteServerInfo(@LoginUser SysUser sysUser,
			@PathVariable("id") Long id) {
		if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			return Result.failed("????????????????????????");
		}
		clusterevOpsService.deleteServerInfoById(id);
		return Result.succeed("????????????");
	}

	@PostMapping("/saveServerInfo")
	public Result saveServerInfo(@LoginUser SysUser sysUser,
			@RequestBody SdpsServerInfo serverInfo) {
		if (!StrUtil.equalsIgnoreCase(sysUser.getUsername(),
				CommonConstant.ADMIN_USER_NAME)) {
			return Result.failed("????????????????????????");
		}
		if (Objects.isNull(serverInfo.getServerId())
				|| Objects.isNull(serverInfo.getClusterType())
				|| Objects.isNull(serverInfo.getHost())
				|| Objects.isNull(serverInfo.getUser())
				|| Objects.isNull(serverInfo.getPasswd())) {
			Result.failed("?????????????????????");
		}
		clusterevOpsService.saveServerInfo(serverInfo);
		return Result.succeed("????????????");
	}

	private static boolean isAdmin(SysUser sysUser) {
		List<String> roleList = sysUser.getRoles().stream()
				.map(SysRole::getCode).collect(Collectors.toList());
		if (roleList.contains("admin") || roleList.contains("sysOperation")) {
			return true;
		}
		return false;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	@GetMapping("/warning/count")
	public Result<JSONObject> getWarningCnt(@LoginUser SysUser sysUser) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.warningCnt(null);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	@PostMapping("/warning/count")
	public Result<JSONObject> getWarningCnt(
			@RequestBody List<Integer> clsuterIds) {
		JSONObject result = clusterevOpsService.warningCnt(clsuterIds);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@GetMapping("/param")
	public Result<SysGlobalArgs> getGlobalParam(
			@RequestParam("type") String type, @RequestParam("key") String key) {
		SysGlobalArgs sysGlobalArgs = clusterevOpsService.selectGlobalArgs(
				type, key);
		return Result.succeed(sysGlobalArgs, "????????????");
	}

	/**
	 * ??????????????????
	 *
	 * @return
	 */
	@GetMapping("/params")
	public Result<List<SysGlobalArgs>> getGlobalParams(
			@RequestParam("type") String type) {
		List<SysGlobalArgs> sysGlobalArgs = clusterevOpsService
				.getGlobalParams(type);
		return Result.succeed(sysGlobalArgs, "????????????");
	}

	/**
	 * ????????????yarn???hdfs??????
	 */
	@GetMapping("/hdfsAndYarnMetrics")
	public Result<JSONObject> getHdfsAndYarnMetrics(@LoginUser SysUser sysUser,
			@Valid @NotNull @RequestParam("page") Integer page,
			@Valid @NotNull @RequestParam("size") Integer size) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getHdfsAndYarnMetrics(page,
				size, null);
		return Result.succeed(result, "????????????");
	}

	@PostMapping("/hdfsAndYarnMetrics")
	public Result<JSONObject> getHdfsAndYarnMetrics(
			@RequestBody PageRequest<DevOpsRequest> request) {
		JSONObject result = clusterevOpsService.getHdfsAndYarnMetrics(request
				.getPage(), request.getSize(), request.getParam()
				.getClusterIds());
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????????????????
	 */
	@GetMapping("/clusterStackAndVersions")
	public Result<JSONObject> getClusterStackAndVersions(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService
				.getClusterStackAndVersions(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????????????????
	 *
	 * @param sysUser
	 *            ????????????
	 * @param clusterId
	 *            ??????ID
	 */
	@GetMapping("/clusterStackAndVersionsNew")
	public Result<JSONObject> getClusterStackAndVersionsNew(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("repositoryVersion") String repositoryVersion) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getClusterStackAndVersionsNew(
				clusterId, repositoryVersion);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????service???users???groups
	 */
	@GetMapping("/serviceUsersAndGroups")
	public Result<JSONObject> getServiceUsersAndGroups(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("services") String services) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getServiceUsersAndGroups(
				clusterId, services);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????host??????
	 */
	@GetMapping("/getClusterHostInfo")
	public Result<JSONObject> getClusterHostInfo(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getClusterHostInfo(clusterId,
				query);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????????????????
	 */
	@GetMapping("/getClusterHostDiskInfo")
	public Result<JSONObject> getClusterHostDiskInfo(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getClusterHostDiskInfo(
				clusterId, query);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ???????????????????????????
	 */
	@GetMapping("/getClusterServiceAutoStart")
	public Result<JSONObject> getClusterServiceAutoStart(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService
				.getClusterServiceAutoStart(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????service?????????display??????
	 */
	@GetMapping("/getServiceDisplayName")
	public Result<JSONObject> getServiceDisplayName(
			@RequestParam("clusterId") Integer clusterId) {
		JSONObject result = clusterevOpsService
				.getServiceDisplayName(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceInstalled")
	public Result<JSONObject> getServiceInstalled(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getServiceInstalled(clusterId);
		return Result.succeed(result, "????????????");

	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getWarningInfo")
	public Result<JSONObject> getWarningInfo(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getWarningInfo(clusterId);
		return Result.succeed(result, "????????????");

	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceWarningInfo")
	public Result<JSONObject> getServiceWarningInfo(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("definition_id") Integer definitionId,
			@RequestParam("from") Integer from,
			@RequestParam("page_size") Integer pageSize) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getServiceWarningInfo(
				clusterId, definitionId, from, from);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ?????????????????????
	 * 
	 * @param sysUser
	 * @param obj
	 * @return
	 */
	@PostMapping("/updateServiceAutoStart")
	public Result updateServiceAutoStart(@LoginUser SysUser sysUser,
			@RequestBody AmbariServiceAutoStartObj obj) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		return clusterevOpsService.updateServiceAutoStart(obj);
	}

	/**
	 * ??????????????????ip
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getIpInfo")
	public Result getClusterIp(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getClusterIp(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????host
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getHostInfo")
	public Result getClusterHost(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getClusterHost(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterName")
	public Result<JSONObject> getClusterName(
			@RequestParam("clusterId") Integer clusterId) {
		JSONObject result = clusterevOpsService.getClusterName(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ?????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/startOrStopService")
	public Result<JSONObject> startOrStopService(@LoginUser SysUser sysUser,
			@RequestBody AmbariStartOrStopServiceObj obj) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		if (!(StrUtil.equalsIgnoreCase("STARTED", "obj.getState()") || StrUtil
				.equalsIgnoreCase("INSTALLED", "obj.getState()"))) {
			throw new BusinessException("?????????????????????");
		}
		JSONObject result = clusterevOpsService.startOrStopService(obj);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getComponentInfo")
	public Result<JSONObject> getComponentInfo(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.getComponentInfo(clusterId);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/restartAllComponent")
	public Result<JSONObject> restartAllComponent(@LoginUser SysUser sysUser,
			@RequestBody JSONObject data) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.restartAllComponent(data);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ?????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/startOrStopComponent")
	public Result<JSONObject> startOrStopComponent(@LoginUser SysUser sysUser,
			@RequestBody JSONObject data) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.startOrStopComponent(data);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/restartComponent")
	public Result<JSONObject> restartComponent(@LoginUser SysUser sysUser,
			@RequestBody JSONObject data) {
		if (!isAdmin(sysUser)) {
			throw new BusinessException("????????????????????????");
		}
		JSONObject result = clusterevOpsService.restartComponent(data);
		return Result.succeed(result, "????????????");
	}

	@PostMapping("/addClusterInfo")
	public Result addClusterInfo(@LoginUser SysUser sysUser,
			@RequestBody SdpsCluster sdpsCluster) {
		if (!StrUtil.equalsIgnoreCase(CommonConstant.ADMIN_USER_NAME,
				sysUser.getUsername())) {
			return Result.failed("????????????????????????,????????????????????????");
		}
		if (StrUtil.isBlank(sdpsCluster.getClusterName())
				|| StrUtil.isBlank(sdpsCluster.getClusterShowName())
				|| Objects.isNull(sdpsCluster.getClusterTypeId())
				|| Objects.isNull(sdpsCluster.getClusterStatusId())) {
			return Result.failed("?????????????????????");
		}
		clusterevOpsService.addClusterInfo(sdpsCluster);
		return Result.succeed("????????????");
	}

	@PostMapping("/updateClusterInfo")
	public Result updateClusterInfo(@LoginUser SysUser sysUser,
			@RequestBody SdpsCluster sdpsCluster) {
		if (!StrUtil.equalsIgnoreCase(CommonConstant.ADMIN_USER_NAME,
				sysUser.getUsername())) {
			return Result.failed("????????????????????????,????????????????????????");
		}
		if (Objects.isNull(sdpsCluster.getClusterId())) {
			return Result.failed("??????ID????????????");
		}
		clusterevOpsService.updateClusterInfo(sdpsCluster);
		return Result.succeed("????????????");
	}

	@GetMapping("/getClusterTypeList")
	public Result getClusterTypeList() {
		List<SdpsClusterType> result = clusterevOpsService.getClusterTypeList();
		return Result.succeed(result, "????????????");
	}

	@DeleteMapping("/removeCluster/{id}")
	public Result removeCluster(@PathVariable(name = "id") Integer clusterId) {
		clusterevOpsService.removeCluster(clusterId);
		return Result.succeed("????????????");
	}

	@GetMapping("/getClusterStatusList")
	public Result getClusterStatusList() {
		List<SdpsClusterStatus> result = clusterevOpsService
				.getClusterStatusList();
		return Result.succeed(result, "????????????");
	}
}
