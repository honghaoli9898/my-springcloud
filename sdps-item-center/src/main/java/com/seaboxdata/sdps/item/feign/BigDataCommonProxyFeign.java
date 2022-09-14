package com.seaboxdata.sdps.item.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.item.feign.fallback.BigDataProxyServiceFallbackFactory;

@FeignClient(name = ServiceNameConstants.BIGDATA_PROXY_SERVICE, fallbackFactory = BigDataProxyServiceFallbackFactory.class, decode404 = true, configuration = MultipartSupportConfig.class)
public interface BigDataCommonProxyFeign {
	@PostMapping("/bigdataCommon/createHdfsPath")
	public Result createHdfsPath(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("createHdfsPath") String createHdfsPath);

	@PostMapping(value = "/bigdataCommon/updataHdfsQNAndSQNAndOwner")
	public Result updataHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@DeleteMapping("/bigdataCommon/deleteItemFile")
	public Result deleteItemFile(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPathList);

	@PostMapping("/bigdataCommon/createHdfsQNAndSQNAndOwner")
	public Result createHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@PostMapping("/bigdataCommon/createItemResource")
	public Result createItemResource(@RequestParam("itemIden") String itemIden,
			@RequestBody HdfsSetDirObj hdfsSetDirObj);

	@GetMapping("/bigdataCommon/selectHdfsQNAndSQN")
	public Result<HdfsDirObj> selectHdfsQNAndSQN(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath);

	@RequestMapping("/devops/param")
	public Result<SysGlobalArgs> getGlobalParam(
			@RequestParam("type") String type, @RequestParam("key") String key);

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

	@PostMapping(value = "/bigdataCommon/uploadScripFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result uploadScripFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam("isUserFile") boolean isUserFile);

	@DeleteMapping("/bigdataCommon/deleteScriptFile")
	public Result deleteScriptFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths,
			@RequestParam("flag") boolean flag);

	@GetMapping("/stat/getStatsByType")
	public Result<List<FileStatsDTO>> getStatsByType(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("type") String type);

	@PostMapping("/bigdataCommon/upateYarnQueueConfigurate")
	public Result updateYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos);

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
