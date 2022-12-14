package com.seaboxdata.sdps.seaboxProxy.controller;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cloud.tianai.crypto.cipher.CryptoCipherBuilder;
import cloud.tianai.crypto.stream.CipherInputStream;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.properties.FileCryptoProperties;
import com.seaboxdata.sdps.common.core.utils.CommonInputStreamResource;
import com.seaboxdata.sdps.common.core.utils.GanymedUtil;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHost;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariServiceAutoStartObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariStartOrStopServiceObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerGroupUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXGroups;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.ranger.resource.HbaseOperateResource;
import com.seaboxdata.sdps.common.framework.bean.ranger.resource.HiveOperateResource;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.seaboxProxy.bean.ClusterHostConf;
import com.seaboxdata.sdps.seaboxProxy.bean.QueuesObj;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.feign.ItemCenterFegin;
import com.seaboxdata.sdps.seaboxProxy.feign.UserCenterFegin;
import com.seaboxdata.sdps.seaboxProxy.service.MetaDataExtract;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;
import com.seaboxdata.sdps.seaboxProxy.util.RangerUtil;
import com.seaboxdata.sdps.seaboxProxy.util.SpringBeanUtil;
import com.seaboxdata.sdps.seaboxProxy.util.YarnUtil;

@Slf4j
@RestController
@RequestMapping("/seabox")
public class SeaBoxPlatformController {
	@Autowired
	ItemCenterFegin itemCenterFegin;
	@Autowired
	BigdataVirtualHost bigdataVirtualHost;

	@Autowired
	RangerUtil rangerUtil;
	@Autowired
	UserCenterFegin userCenterFegin;
	@Autowired
	BigdataCommonFegin bigdataCommonFegin;
	@Autowired
	SeaBoxYarnController seaBoxYarnController;
	@Autowired
	FileCryptoProperties fileCryptoProperties;
	@Autowired
	MetaDataExtract metaDataExtract;

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 * @param username
	 */
	private void userHomeDirIFNoexistCreateIFExistDoNothing(Integer clusterId,
			String username) {
		String userHomePath = "/user/".concat(username);
		HdfsUtil hdfsUtilRoot = new HdfsUtil(clusterId, "hdfs");
		Boolean exist = hdfsUtilRoot.isExist(userHomePath);
		if (!exist) {
			hdfsUtilRoot.mkdirAndOwner(userHomePath, username, "hdfs");
		}
		hdfsUtilRoot.closeFs();
	}

	/**
	 * ??????HDFS??????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param hdfsPath
	 *            hdfs??????
	 * @return ?????????????????????
	 */
	@GetMapping("/makeHdfsPath")
	public Boolean makeHdfsPath(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId, username);
		boolean bool = hdfsUtil.mkdir(hdfsPath);
		hdfsUtil.setPermission(hdfsPath, "775");
		hdfsUtil.closeFs();
		return bool;
	}

	/**
	 * ??????????????????
	 * 
	 * @param clusterId
	 * @param hdfsPaths
	 * @return
	 */
	@DeleteMapping("/deleteItemFile")
	public Result deleteFile(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths) {
		JSONObject jsonObject = new JSONObject();
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		boolean bool = false;
		// flag???true?????????????????????????????????
		boolean isExist = true;
		for (String hdfsPath : hdfsPaths) {
			isExist = hdfsUtil.isExist(hdfsPath);
			if (!isExist) {
				jsonObject.put("success", isExist);
				jsonObject.put("message", hdfsPath.concat(" not exist"));
				return Result.succeed(jsonObject, "????????????");
			}
		}
		try {
			bool = hdfsUtil.deleteFile(hdfsPaths);
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("message", e.getMessage());
			return Result.succeed(jsonObject, "????????????");
		}
		hdfsUtil.closeFs();
		jsonObject.put("success", bool);
		jsonObject.put("message", "??????????????????");
		return Result.succeed(jsonObject, "????????????");
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param hdfsPaths
	 *            ????????????????????????
	 * @param flag
	 *            ????????????????????? //false??????????????????????????? true?????????????????????????????????
	 * @return ??????????????????
	 */
	@DeleteMapping("/deleteFile")
	public Result deleteFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths,
			@RequestParam("flag") Boolean flag) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		// System.out.println(System.getProperty("HADOOP_USER_NAME"));
		JSONObject jsonObject = new JSONObject();
		bigdataVirtualHost.setVirtualHost(clusterId);
		boolean isChangerUser = false;
		for (String hdfsPath : hdfsPaths) {
			if (!hdfsPath.matches("^/user/[\\s\\S]+/.Trash/[\\s\\S]*")) {
				isChangerUser = true;
				break;
			}
		}
		HdfsUtil hdfsUtil = null;
		if (isChangerUser) {
			hdfsUtil = new HdfsUtil(clusterId, username);
		} else {
			hdfsUtil = new HdfsUtil(clusterId);
		}

		boolean bool = false;
		// flag???true?????????????????????????????????
		boolean isExist = true;
		for (String hdfsPath : hdfsPaths) {
			isExist = hdfsUtil.isExist(hdfsPath);
			if (!isExist) {
				jsonObject.put("success", isExist);
				jsonObject.put("message", hdfsPath.concat(" not exist"));
				return Result.succeed(jsonObject, "????????????");
			}
		}
		if (flag) {
			// JSONArray array = new JSONArray();
			// for (int i = 0; i< hdfsPaths.size(); i++) {
			// JSONObject request = new JSONObject();
			// request.put("path",hdfsPaths.get(i));
			// request.put("recursive",true);
			// array.add(i,request);
			// }
			// JSONObject param = new JSONObject();
			// param.put("paths",array);
			// Result<SysGlobalArgs> globalParam =
			// bigdataCommonFegin.getGlobalParam("ambari", "moveToTrash");
			// AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			// JSONObject resultJson = ambariUtil.moveFile(username,
			// globalParam.getData().getArgValue(),
			// globalParam.getData().getArgValueDesc(), param);
			// log.info("??????????????????:{}", resultJson.toJSONString());

			String userHomePath = "/user/".concat(username);
			HdfsUtil hdfsUtilRoot = new HdfsUtil(clusterId, "hdfs");
			Boolean exist = hdfsUtilRoot.isExist(userHomePath);
			if (!exist) {
				hdfsUtilRoot.mkdirAndOwner(userHomePath, username, "hdfs");
			}
			hdfsUtilRoot.closeFs();

			try {
				for (String hdfsPath : hdfsPaths) {
					bool = hdfsUtil.moveToTrash(hdfsPath);
					if (!bool) {
						log.error("????????????????????????");
						jsonObject.put("success", false);
						jsonObject.put("message", "??????????????????????????????");
						return Result.succeed(jsonObject, "????????????");
					}
				}
			} catch (Exception e) {
				log.error("????????????????????????:", e);
				jsonObject.put("success", false);
				jsonObject.put("message", e.getMessage());
				return Result.succeed(jsonObject, "????????????");
			}
			jsonObject.put("success", true);
			return Result.succeed(jsonObject, "????????????");
		} else {
			try {
				bool = hdfsUtil.deleteFile(hdfsPaths);
			} catch (Exception e) {
				jsonObject.put("success", false);
				jsonObject.put("message", e.getMessage());
				return Result.succeed(jsonObject, "????????????");
			}
		}
		hdfsUtil.closeFs();
		jsonObject.put("success", bool);
		jsonObject.put("message", "??????????????????");
		return Result.succeed(jsonObject, "????????????");
	}

	/**
	 * ??????HDFS??????
	 * 
	 * @param clusterId
	 * @param hdfsPathList
	 * @return
	 */
	@DeleteMapping("/cleanHdfsDir")
	public Boolean cleanHdfsDir(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPathList") ArrayList<String> hdfsPathList) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		boolean bool = hdfsUtil.cleanDir(hdfsPathList);
		hdfsUtil.closeFs();
		return bool;
	}

	/**
	 * ??????HDFS???????????????????????????
	 * 
	 * @param hdfsSetDirObj
	 * @return
	 */
	@PostMapping("/updataHdfsQNAndSQNAndOwner")
	public Result<Boolean> updataQuotaNumAndSpaceQuotaNum(
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(hdfsSetDirObj.getClusterId());
		HdfsUtil hdfsUtil = new HdfsUtil(hdfsSetDirObj.getClusterId());
		if (StrUtil.isNotBlank(hdfsSetDirObj.getSourceHdfsPath())
				&& !StrUtil.equalsAnyIgnoreCase(
						hdfsSetDirObj.getSourceHdfsPath(),
						hdfsSetDirObj.getHdfsPath())) {
			hdfsUtil.rename(hdfsSetDirObj.getSourceHdfsPath(),
					hdfsSetDirObj.getHdfsPath());
		}
		if (StrUtil.isNotBlank(hdfsSetDirObj.getOwner())) {
			VXUsers vXUsers = getRangerUserByName(hdfsSetDirObj.getClusterId(),
					hdfsSetDirObj.getOwner());
			if (!StrUtil.equals(hdfsSetDirObj.getOwner(), vXUsers.getName(),
					true)) {
				return Result.failed(false, "??????????????????");
			}
		}
		if (StrUtil.isNotBlank(hdfsSetDirObj.getOwnergroup())) {
			VXGroups vXGroups = getRangerGroupByName(
					hdfsSetDirObj.getClusterId(), hdfsSetDirObj.getOwnergroup());
			if (!StrUtil.equals(hdfsSetDirObj.getOwnergroup(),
					vXGroups.getName(), true)) {
				return Result.failed(false, "??????????????????");
			}
		}
		Boolean result = hdfsUtil.setHdfsQNAndSQNAndOwner(
				hdfsSetDirObj.getHdfsPath(), hdfsSetDirObj.getQuotaNum(),
				hdfsSetDirObj.getSpaceQuotaNum(), hdfsSetDirObj.getOwner(),
				hdfsSetDirObj.getOwnergroup());
		hdfsUtil.closeFs();
		return Result.succeed(result, "????????????");
	}

	/**
	 * Hdfs ????????????????????? ????????????????????????????????????????????????????????????
	 *
	 * @return
	 */
	@PostMapping("/createHdfsQNAndSQNAndOwner")
	public Boolean createHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(hdfsSetDirObj.getClusterId());
		HdfsUtil hdfsUtil = new HdfsUtil(hdfsSetDirObj.getClusterId());
		boolean bool = hdfsUtil.mkdirSetQNAndSQNAndOwner(
				hdfsSetDirObj.getHdfsPath(), hdfsSetDirObj.getQuotaNum(),
				hdfsSetDirObj.getSpaceQuotaNum(), hdfsSetDirObj.getOwner(),
				hdfsSetDirObj.getOwnergroup());
		hdfsUtil.setPermission(hdfsSetDirObj.getHdfsPath(), "775");
		hdfsUtil.closeFs();
		return bool;
	}

	/**
	 * ??????????????????
	 * 
	 * @param itemIden
	 * @param hdfsSetDirObj
	 * @return
	 */
	@PostMapping("/createItemResource")
	public Result createItemResource(@RequestParam("itemIden") String itemIden,
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		String groupName = itemIden.concat("-ALL");
		Boolean bool = addRangerGroup(hdfsSetDirObj.getClusterId(), VXGroups
				.builder().name(groupName).description("create item group")
				.build());
		hdfsSetDirObj.setOwnergroup(groupName);
		if (bool)
			bool = createHdfsQNAndSQNAndOwner(hdfsSetDirObj);
		if (bool) {
			return Result.succeed("????????????");
		} else {
			return Result.failed("????????????");
		}

	}

	/**
	 * ??????hdfs????????????(??????????????????????????????????????????????????????(??????)??????????????????(??????)?????????????????????????????????)
	 *
	 * @param clusterId
	 * @param hdfsPath
	 * @return
	 */
	@GetMapping("/getHdfsSaveObjList")
	public ArrayList<HdfsDirObj> getHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		if (StrUtil.isBlankIfStr(clusterId) || StrUtil.isBlankIfStr(hdfsPath)) {
			log.error("???????????????????????????.clusterId&hdfsPath:{}", clusterId + "&"
					+ hdfsPath);
			return null;
		}
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		ArrayList<HdfsDirObj> saveObjsList = hdfsUtil
				.getDirOwnerGroupUsedQuotaNumSpaceQuotaNum(hdfsPath);
		hdfsUtil.closeFs();
		return saveObjsList;
	}

	/**
	 * ??????hdfs??????(????????????????????????(??????)??????????????????)
	 *
	 * @param clusterId
	 * @param hdfsPath
	 * @return
	 */
	@GetMapping("/selectHdfsQNAndSQN")
	public HdfsDirObj selectHdfsQNAndSQN(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		HdfsDirObj hdfsDirObj = hdfsUtil.getDirQNAndSQN(hdfsPath);
		hdfsUtil.closeFs();
		return hdfsDirObj;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/selectAllQueueTree")
	public QueuesObj selectQueueTree(Integer clusterId) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		bigdataVirtualHost.setVirtualHost(clusterId);
		YarnUtil yarnUtil = new YarnUtil(clusterId);
		QueuesObj queuesObj = yarnUtil.queueAllExtract();
		yarnUtil.closeYc();
		return queuesObj;
	}

	/**
	 * ??????Ranger???????????????????????????
	 * 
	 * @param clusterId
	 * @param userName
	 * @return
	 */
	@GetMapping("/getRangerUserByName")
	public VXUsers getRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		VXUsers vxUsers = rangerUtil.getUserByName(userName);
		VXUsers rangerUser = new VXUsers();
		if (Objects.nonNull(vxUsers.getId())) {
			rangerUser = rangerUtil.getUserById(vxUsers.getId());
		}
		return rangerUser;
	}

	/**
	 * ??????Ranger??????
	 * 
	 * @param clusterId
	 * @param rangerObjList
	 * @return
	 */
	@PostMapping("/addRangerUser")
	public Boolean addRangerUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody ArrayList<VXUsers> rangerObjList) {
		rangerObjList
				.forEach(obj -> {
					userHomeDirIFNoexistCreateIFExistDoNothing(clusterId,
							obj.getName());
				});
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		for (VXUsers vxUsers : rangerObjList) {
			Boolean bool = rangerUtil.addRangerUser(vxUsers);
			if (bool == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ?????????????????????Ranger??????
	 * 
	 * @param clusterId
	 * @param userName
	 * @return
	 */
	@DeleteMapping("/deleteRangerUserByName")
	public Boolean deleteRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName) {

		bigdataVirtualHost.setVirtualHost(clusterId);
		deleteFile(clusterId, CollUtil.newArrayList("/user/".concat(userName)));
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.deleteRangerUser(userName);
		return bool;
	}

	/**
	 * ?????????????????????ranger
	 * 
	 * @param clusterId
	 * @param rangerUserObj
	 * @return
	 */
	@PutMapping("/updateRangerUserByName")
	public Boolean updateRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXUsers rangerUserObj) {
		System.out.println(rangerUserObj.toString());
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.updateRangerUser(rangerUserObj);
		return bool;
	}

	/**
	 * ??????????????????Ranger?????????
	 * 
	 * @param clusterId
	 * @param groupName
	 * @return
	 */
	@GetMapping("/getRangerGroupByName")
	public VXGroups getRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		VXGroups rangerGroup = rangerUtil.getGroupByName(groupName);
		return rangerGroup;
	}

	/**
	 * ??????Ranger?????????
	 * 
	 * @param clusterId
	 * @param rangerGroupObj
	 * @return
	 */
	@PostMapping("/addRangerGroup")
	public Boolean addRangerGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj) {
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			rangerUtil.init(clusterId);
			Boolean bool = rangerUtil.addRangerGroup(rangerGroupObj);
			return bool;
		} catch (Exception e) {
			log.error("??????ranger?????????????????????", e);
			return false;
		}
	}

	/**
	 * ??????????????????Ranger?????????
	 * 
	 * @param clusterId
	 * @param groupName
	 * @return
	 */
	@DeleteMapping("/deleteRangerGroupByName")
	public Boolean deleteRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.deleteRangerGroup(groupName);
		return bool;
	}

	/**
	 * ??????????????????Ranger?????????
	 * 
	 * @param clusterId
	 * @param rangerGroupObj
	 * @return
	 */
	@PutMapping("/updateRangerGroupByName")
	public Boolean updateRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.updateRangerGroup(rangerGroupObj);
		return bool;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param clusterId
	 * @param groupName
	 * @return
	 */
	@GetMapping("/getUsersByGroupName")
	public RangerGroupUser getUsersByGroupName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		RangerGroupUser rangerGroupUser = rangerUtil
				.getUsersByGroupName(groupName);
		return rangerGroupUser;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 * @param groupName
	 * @param rangerUsers
	 * @return
	 */
	@PostMapping("/addUsersToGroup")
	public Result addUsersToGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.addUsersToGroup(groupName, rangerUsers);
		return Result.succeed(bool, "????????????");
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 * @param groupName
	 * @param rangerUsers
	 * @return
	 */
	@DeleteMapping("/deleteUsersToGroup")
	public Result deleteUsersToGroup(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		rangerUtil.init(clusterId);
		Boolean bool = rangerUtil.deleteUsersToGroup(groupName, rangerUsers);
		return Result.succeed(bool, "????????????");
	}

	/**
	 * ??????Ranger??????
	 * 
	 * @param rangerPolicyObj
	 * @return
	 */
	@PostMapping("/addRangerPolicy")
	public Result addRangerPolicy(@RequestBody RangerPolicyObj rangerPolicyObj) {

		Boolean bool = false;
		try {
			AmbariUtil ambariUtil = new AmbariUtil(
					rangerPolicyObj.getClusterId());
			String clusterName = ambariUtil.getClusterName();
			rangerUtil.init(rangerPolicyObj.getClusterId());
			// Ranger????????????
			RangerPolicy rangerPolicy = new RangerPolicy();
			// ?????????????????????
			String serviceType = rangerPolicyObj.getServiceType();
			if ("hdfs".equalsIgnoreCase(serviceType)) {
				rangerPolicy.setService(clusterName.concat("_").concat(
						BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HDFS));
				// ??????????????????
				List<String> resourcePaths = rangerPolicyObj.getResourcePaths();
				if (resourcePaths != null && !resourcePaths.isEmpty()) {
					Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
					RangerPolicy.RangerPolicyResource rangerPolicyResource = new RangerPolicy.RangerPolicyResource();
					// ??????????????????
					rangerPolicyResource.setIsRecursive(Boolean.TRUE);
					rangerPolicyResource.setValues(resourcePaths);
					resources.put("path", rangerPolicyResource);
					rangerPolicy.setResources(resources);
				}

			} else if ("hive".equalsIgnoreCase(serviceType)) {
				rangerPolicy.setService(clusterName.concat("_").concat(
						BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HIVE));
				// ??????Hive??????(???/???/???)
				HiveOperateResource hiveResource = rangerPolicyObj
						.getHiveOperateResource();
				if (hiveResource != null) {
					Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
					// ???????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceDataBase = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceDataBase.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceDataBase.setValues(hiveResource
							.getDatabase());
					resources.put("database", rangerPolicyResourceDataBase);
					// ???????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceTable = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceTable.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceTable
							.setValues(hiveResource.getTable());
					resources.put("table", rangerPolicyResourceTable);
					// ???????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceColumn = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceColumn.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceColumn.setValues(hiveResource
							.getColumn());
					resources.put("column", rangerPolicyResourceColumn);
					rangerPolicy.setResources(resources);
				}
			} else if ("hbase".equalsIgnoreCase(serviceType)) {
				rangerPolicy.setService(clusterName.concat("_").concat(
						BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HBASE));
				// ??????Hbase??????(???/??????/???)
				HbaseOperateResource hbaseResource = rangerPolicyObj
						.getHbaseOperateResource();
				if (hbaseResource != null) {
					Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
					// ???????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceTable = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceTable.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceTable.setValues(hbaseResource
							.getTable());
					resources.put("table", rangerPolicyResourceTable);
					// ??????????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceColumnFamily = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceColumnFamily
							.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceColumnFamily.setValues(hbaseResource
							.getTable());
					resources.put("column-family",
							rangerPolicyResourceColumnFamily);
					// ???????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceColumn = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceColumn.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceColumn.setValues(hbaseResource
							.getColumn());
					resources.put("column", rangerPolicyResourceColumn);
					rangerPolicy.setResources(resources);
				}
			} else if ("yarn".equalsIgnoreCase(serviceType)) {
				rangerPolicy.setService(clusterName.concat("_").concat(
						BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_YARN));
				// ??????Yarn??????(???/??????/???)
				List<String> yarnResource = rangerPolicyObj
						.getYarnOperateResource();
				if (yarnResource != null && !yarnResource.isEmpty()) {
					Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
					// ??????????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceQueue = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceQueue.setIsRecursive(Boolean.TRUE); // ??????????????????
					rangerPolicyResourceQueue.setValues(yarnResource);
					resources.put("queue", rangerPolicyResourceQueue);
					rangerPolicy.setResources(resources);
				}
			} else if ("kafka".equalsIgnoreCase(serviceType)) {
				rangerPolicy.setService(clusterName.concat("_").concat(
						BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_KAFKA));
				// ??????Kafka??????(topic)
				List<String> kafkaResource = rangerPolicyObj
						.getKafkaOperateResource();
				if (kafkaResource != null && !kafkaResource.isEmpty()) {
					Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
					// ??????????????????
					RangerPolicy.RangerPolicyResource rangerPolicyResourceQueue = new RangerPolicy.RangerPolicyResource();
					rangerPolicyResourceQueue.setIsRecursive(Boolean.FALSE); // ??????????????????
					rangerPolicyResourceQueue.setValues(kafkaResource);
					resources.put("topic", rangerPolicyResourceQueue);
					rangerPolicy.setResources(resources);
				}
			} else {
				return Result.failed("??????Ranger????????????[????????????serviceType????????????]");
			}
			// ??????????????????
			if (!rangerPolicyObj.getGroups().isEmpty()
					|| !rangerPolicyObj.getUsers().isEmpty()
					|| !rangerPolicyObj.getAccesses().isEmpty()) {
				List<RangerPolicy.RangerPolicyItem> rangerPolicyItems = new ArrayList<>();
				RangerPolicy.RangerPolicyItem rangerPolicyItem = new RangerPolicy.RangerPolicyItem();
				// ?????????????????????
				rangerPolicyItem.setGroups(rangerPolicyObj.getGroups());
				// ??????????????????
				rangerPolicyItem.setUsers(rangerPolicyObj.getUsers());
				// ??????????????????
				List<RangerPolicy.RangerPolicyItemAccess> accesses = new ArrayList<>();
				for (String hdfsAccess : rangerPolicyObj.getAccesses()) {
					accesses.add(new RangerPolicy.RangerPolicyItemAccess(
							hdfsAccess, Boolean.TRUE));
				}
				rangerPolicyItem.setAccesses(accesses);
				rangerPolicyItems.add(rangerPolicyItem);
				rangerPolicy.setPolicyItems(rangerPolicyItems);
			}
			// ??????????????????
			rangerPolicy.setName(rangerPolicyObj.getPolicyName());
			// ????????????(????????????)
			rangerPolicy.setIsEnabled(Boolean.TRUE);
			// ????????????(????????????)
			rangerPolicy.setIsAuditEnabled(Boolean.TRUE);

			bool = rangerUtil.addRangerPolicy(rangerPolicy);
		} catch (Exception e) {
			log.error("??????Ranger????????????:", e);
			return Result.failed("??????Ranger????????????:", e.getMessage());
		}
		if (bool) {
			return Result.succeed(bool, "??????Ranger????????????");
		} else {
			return Result.failed("??????Ranger????????????");
		}
	}

	/**
	 * ????????????Ranger??????
	 * 
	 * @param
	 * @return
	 */
	@GetMapping("/likeRangerPolicy")
	public Result likeRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		rangerUtil.init(clusterId);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		String clusterName = ambariUtil.getClusterName();
		String result = rangerUtil.queryRangerPolicy(clusterName, serviceType,
				policyName);
		if (StringUtils.isNotBlank(result)) {
			return Result.succeed(result, "????????????Ranger????????????");
		} else {
			return Result.failed("????????????Ranger????????????,?????????null??????");
		}
	}

	/**
	 * ??????Ranger??????
	 * 
	 * @param
	 * @return
	 */
	@GetMapping("/queryRangerPolicy")
	public Result queryRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		String result = null;
		Result resultStr = likeRangerPolicy(clusterId, serviceType, policyName);
		JSONArray jsonArray = JSON.parseArray((String) resultStr.getData());
		if (Objects.isNull(jsonArray)) {
			return Result.failed("??????Ranger????????????,?????????null??????");
		}
		for (Object obj : jsonArray) {
			JSONObject jsonObject = (JSONObject) obj;
			String name = jsonObject.getString("name");
			if (name.equals(policyName)) {
				result = JSON.toJSONString(obj);
			}
		}

		if (StringUtils.isNotBlank(result)) {
			return Result.succeed(result, "??????Ranger????????????");
		} else {
			return Result.failed("??????Ranger????????????,?????????null??????");
		}
	}

	/**
	 * ??????Ranger??????
	 * 
	 * @param
	 * @return
	 */
	@DeleteMapping("/deleteRangerPolicy")
	public Result deleteRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		Result resultStr = queryRangerPolicy(clusterId, serviceType, policyName);
		JSONObject jsonObject = JSON.parseObject((String) resultStr.getData());
		if (Objects.isNull(jsonObject)) {
			return resultStr;
		}
		String policyId = jsonObject.getString("id");
		Boolean bool = rangerUtil.deleteRangerPolicy(policyId);
		if (bool) {
			return Result.succeed(bool, "??????Ranger??????");
		} else {
			return Result.failed("??????Ranger????????????");
		}
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param queueName
	 *            ?????????
	 * @return true?????????????????? false??????????????????
	 */
	@DeleteMapping("/deleteQueue")
	public Boolean deleteQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("queueName") String queueName) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		YarnUtil yarnUtil = new YarnUtil(clusterId);
		return yarnUtil.deleteQueue(queueName);
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param queueNames
	 *            ?????????
	 * @return true?????????????????? false??????????????????
	 */
	@DeleteMapping("/deleteQueueByBatch")
	public Boolean deleteQueueByBatch(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("queueNames") String[] queueNames) {
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			YarnUtil yarnUtil = new YarnUtil(clusterId);
			for (String queueName : queueNames) {
				yarnUtil.deleteQueue(queueName);
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error("??????????????????.", e);
			return Boolean.FALSE;
		}
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ??????????????????
	 */
	@GetMapping("/selectQueue")
	public QueuesObj selectQueue(@RequestParam("clusterId") Integer clusterId) {
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			YarnUtil yarnUtil = new YarnUtil(clusterId);
			QueuesObj queuesObj = yarnUtil.queueAllExtract();
			yarnUtil.closeYc();
			return queuesObj;
		} catch (Exception e) {
			log.error("??????????????????.", e);
			return null;
		}
	}

	/**
	 * ????????????????????????New
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param repositoryVersion
	 *            ????????????
	 */
	@GetMapping("/getClusterStackAndVersionsNew")
	public JSONObject getClusterStackAndVersionsNew(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("repositoryVersion") String repositoryVersion) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			param.put(2, repositoryVersion);

			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "stackVersionsNew");
			SysGlobalArgs sysGlobalArgs = args.getData();
			return ambariUtil.getAmbariApi(sysGlobalArgs.getArgValue(),
					sysGlobalArgs.getArgValueDesc(), param);
		} catch (Exception e) {
			log.error("????????????????????????New??????", e);
			return new JSONObject();
		}
	}

	/**
	 * ???????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param stackName
	 *            ????????????
	 * @param repositoryVersion
	 *            ????????????
	 */
	@GetMapping("/clusterAndService")
	public JSONObject clusterAndService(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("repositoryVersion") String repositoryVersion) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = MapUtil.newHashMap();
			param.put(1, stackName);
			param.put(2, repositoryVersion);

			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "clusterAndService");
			SysGlobalArgs sysGlobalArgs = args.getData();
			return ambariUtil.getAmbariApi(sysGlobalArgs.getArgValue(),
					sysGlobalArgs.getArgValueDesc(), param);
		} catch (Exception e) {
			log.error("?????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param stackName
	 *            ????????????
	 * @param version
	 *            ??????
	 */
	@GetMapping("/resourceOSList")
	public JSONObject resourceOSList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("version") String version) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);

			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "resourceOSList");
			SysGlobalArgs sysGlobalArgs = args.getData();

			Map<Integer, Object> param = MapUtil.newHashMap();
			param.put(1, stackName);
			param.put(2, version);
			return ambariUtil.getAmbariApi(sysGlobalArgs.getArgValue(),
					sysGlobalArgs.getArgValueDesc(), param);
		} catch (Exception e) {
			log.error("???????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????gpl???license
	 *
	 * @param clusterId
	 *            ??????ID
	 */
	@GetMapping("/validateGPLLicense")
	public JSONObject validateGPLLicense(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "ambariServerGPLLicense");
			SysGlobalArgs sysGlobalArgs = args.getData();
			return ambariUtil.getAmbariApi(sysGlobalArgs.getArgValue(),
					sysGlobalArgs.getArgValueDesc());
		} catch (Exception e) {
			log.error("??????gpl???license??????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param id
	 *            ??????ID
	 */
	@DeleteMapping("/stackVersionDel/{id}")
	public JSONObject stackVersionDel(
			@RequestParam("clusterId") Integer clusterId,
			@PathVariable("id") Long id) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "stackVersionDel");
			SysGlobalArgs sysGlobalArgs = args.getData();
			Map<Integer, Object> param = MapUtil.newHashMap();
			param.put(1, id);

			return ambariUtil.getAmbariApi(sysGlobalArgs.getArgValue(),
					sysGlobalArgs.getArgValueDesc(), null, param, false);
		} catch (Exception e) {
			log.error("????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ?????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getWarningCnt")
	public Integer getWarningCnt(@RequestParam("clusterId") Integer clusterId) {
		try {

			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "warningCnt");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			Integer cnt = result.getJSONArray("items").size();
			return cnt;
		} catch (Exception e) {
			log.error("?????????????????????", e);
			return -1;
		}
	}

	/**
	 * ??????hdfs???yarn????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getHdfsAndYarnMetrics")
	public JSONObject getHdfsAndYarnMetrics(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "metrics");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return AmbariUtil.analysisYarnAndHdfsInfo(result);
		} catch (Exception e) {
			log.error("??????hdfs???yarn??????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterStackAndVersions")
	public JSONObject getClusterStackAndVersions(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "stackVersions");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), param);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"serviceInstalled");
			JSONObject installedResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), param);
			JSONObject statusJson = ambariUtil.queryInstalledService(param.get(
					1).toString());
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"componentCategory");
			JSONObject categoryResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), param);
			return AmbariUtil.analysisStackAndVersions(result, installedResult,
					statusJson, categoryResult);
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ????????????service???users???groups
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceUsersAndGroups")
	public JSONObject getServiceUsersAndGroups(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("services") String services) {
		try {
			Map<Integer, Object> param = MapUtil.newHashMap();
			param.put(1, services);
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "userAndGroup");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), param);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"userAndGroupAmbari");
			JSONObject ambariResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			return AmbariUtil.analysisServiceUsersAndGroups(result,
					ambariResult);
		} catch (Exception e) {
			log.error("????????????service???users???groups??????", e);
			return new JSONObject();
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param page_size
	 *            ????????????
	 * @param from
	 *            ????????????
	 * @param sortBy
	 *            ????????????
	 * @param service_name
	 *            ?????????
	 * @param createtime
	 *            ??????????????????
	 * @return
	 */
	@GetMapping("/getServiceConfigVersions")
	public JSONObject getServiceConfigVersions(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("page_size") Integer page_size,
			@RequestParam("from") Integer from,
			@RequestParam("sortBy") String sortBy,
			@RequestParam(value = "service_name", required = false) String service_name,
			@RequestParam(value = "createtime", required = false) String createtime) {
		log.info(
				"getServiceConfigVersions clusterId:{} page_size:{}    from:{} sortBy:{}   service_name:{} createtime:{}",
				clusterId, page_size, from, sortBy, service_name, createtime);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);

		return ambariUtil.getServiceConfigVersions(page_size, from, sortBy,
				service_name, createtime);
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/getComponentAndHost")
	public JSONObject getComponentAndHost(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName) {
		log.info("getComponentAndHost clusterId:{}  serviceName:{}", clusterId,
				serviceName);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.getComponentAndHost(serviceName);
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @return
	 */
	@GetMapping("/queryInstalledService")
	public JSONObject queryInstalledService(
			@RequestParam("clusterId") Integer clusterId) {
		log.info("queryInstalledService clusterId:{}	clusterName:{}", clusterId);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.queryInstalledService(ambariUtil.getClusterName());
	}

	/**
	 * ????????????host??????
	 *
	 * @return
	 */
	@GetMapping("/getClusterHostInfo")
	public JSONObject getClusterHostInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query) {
		try {
			JSONObject data = new JSONObject();
			JSONObject queryJSONObject = new JSONObject();
			queryJSONObject.put("query", StrUtil.isBlank(query) ? "" : query);
			data.put("RequestInfo", queryJSONObject);
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "clusterHostInfo");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("????????????host????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterHostDiskInfo")
	public JSONObject getClusterHostDiskInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("query") String query) {
		try {
			JSONObject data = new JSONObject();
			JSONObject queryJSONObject = new JSONObject();
			queryJSONObject.put("query", StrUtil.isBlank(query) ? "" : query);
			data.put("RequestInfo", queryJSONObject);
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "clusterHostDiskInfo");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ???????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterServiceAutoStart")
	public JSONObject getClusterServiceAutoStart(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "serviceAutoStart");
			Result<SysGlobalArgs> disPlayArgs = bigdataCommonFegin
					.getGlobalParam("ambari", "serviceDisplayName");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			JSONObject disPlayResult = ambariUtil.getAmbariApi(disPlayArgs
					.getData().getArgValue(), disPlayArgs.getData()
					.getArgValueDesc());
			return AmbariUtil.analysisServiceAutoStart(result, disPlayResult);
		} catch (Exception e) {
			log.error("???????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ????????????service?????????display??????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceDisplayName")
	public JSONObject getServiceDisplayName(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "serviceDisplayName");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			return AmbariUtil.analysisServiceDisplayName(result);
		} catch (Exception e) {
			log.error("????????????service?????????display??????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceInstalled")
	public JSONObject getServiceInstalled(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "serviceInstalled");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return AmbariUtil.analysisServiceInstalled(result);
		} catch (Exception e) {
			log.error("??????????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getWarningInfo")
	public JSONObject getWarningInfo(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "warningCnt");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getServiceWarningInfo")
	public JSONObject getServiceWarningInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("definition_id") Integer definitionId,
			@RequestParam("from") Integer from,
			@RequestParam("page_size") Integer pageSize) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			param.put(2, definitionId);
			param.put(3, from);
			param.put(4, pageSize);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "waringServiceId");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), param);
			return result;
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ?????????????????????
	 *
	 * @param obj
	 * @return
	 */
	@PostMapping("/updateServiceAutoStart")
	public Result updateServiceAutoStart(
			@RequestBody AmbariServiceAutoStartObj obj) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(obj.getClusterId());
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "updateServiceAutoStart");
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Requested-By", "X-Requested-By");
			StringBuffer sb = new StringBuffer();
			sb.append("ServiceComponentInfo/component_name.in(")
					.append(CollUtil.join(obj.getServices(), ",")).append(")");
			JSONObject data = new JSONObject();
			JSONObject queryJSONObject = new JSONObject();
			queryJSONObject.put("query", sb.toString());
			data.put("RequestInfo", queryJSONObject);
			JSONObject isAutoJSONObject = new JSONObject();
			isAutoJSONObject
					.put("recovery_enabled", obj.getIsAuto().toString());
			data.put("ServiceComponentInfo", isAutoJSONObject);
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), data.toJSONString(),
					getClusterNameParam(ambariUtil));
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("???????????????????????????", e);
			return Result.failed("????????????");
		}
	}

	/**
	 * ??????HDFS??????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param srcPath
	 *            ????????????????????????
	 * @param destPath
	 *            ??????????????????
	 * @return ??????????????????
	 */
	@PostMapping("/copyFileFromHDFS")
	public Result<JSONObject> copyFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath) {
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
			List<String> pathList = StrUtil.split(srcPath, '/');
			String finalPath = destPath.concat("/").concat(
					pathList.get(pathList.size() - 1));
			if (hdfsUtil.isExist(finalPath)) {
				return Result.failed("????????????????????????????????????");
			}
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "copyFile");
			JSONObject jsonObject = new JSONObject();
			ArrayList<String> arrayList = new ArrayList<>();
			arrayList.add(srcPath);
			jsonObject.put("sourcePaths", arrayList);
			jsonObject.put("destinationPath", destPath);
			JSONObject result = ambariUtil.fileOperation(true, username, args
					.getData().getArgValue(), args.getData().getArgValueDesc(),
					jsonObject);
			return Result.succeed(result, "????????????");
		} catch (Exception e) {
			if (e.getMessage().contains("Permission denied")) {
				return Result.failed("????????????????????????");
			}
			return Result.failed(e.getMessage());
		}

	}

	/**
	 * ??????HDFS??????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param srcPath
	 *            ????????????????????????
	 * @param destPath
	 *            ??????????????????
	 * @return ??????????????????
	 */
	@PostMapping("/moveFileFromHDFS")
	public Result<JSONObject> moveFileFromHDFS(
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath) {
		if (StringUtils.isBlank(srcPath)) {
			return Result.failed("srcPath??????.");
		}
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		List<String> pathList = StrUtil.split(srcPath, '/');
		String finalPath = destPath.concat("/").concat(
				pathList.get(pathList.size() - 1));
		if (hdfsUtil.isExist(finalPath)) {
			return Result.failed("????????????????????????????????????");
		}
		boolean isChangerUser = true;
		if (srcPath.matches("^/user/[\\s\\S]+/.Trash/[\\s\\S]*")) {
			isChangerUser = false;
		}
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
				"ambari", "moveFile");
		JSONObject jsonObject = new JSONObject();
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add(srcPath);
		jsonObject.put("sourcePaths", arrayList);
		jsonObject.put("destinationPath", destPath);
		JSONObject result = ambariUtil.fileOperation(isChangerUser, username,
				args.getData().getArgValue(), args.getData().getArgValueDesc(),
				jsonObject);
		return Result.succeed(result, "????????????");
	}

	/**
	 * ??????????????????ip
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getIpInfo")
	public JSONObject getClusterIp(@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "ip");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("??????????????????ip????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????host
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getHostInfo")
	public JSONObject getHostIp(@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "hostname");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("??????????????????host????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????host ?????????????????????
	 *
	 * @param sdpsServerInfo
	 *            serverInfo
	 */
	@PostMapping("/getHostInfos")
	public JSONObject getHostIps(@RequestBody SdpsServerInfo sdpsServerInfo) {
		try {
			AmbariUtil ambariUtil = AmbariUtil.getInstance();
			ambariUtil.setUrl("http://" + sdpsServerInfo.getHost() + ":"
					+ sdpsServerInfo.getPort());

			HttpHeaders headers = new HttpHeaders();
			String plainCreds = sdpsServerInfo.getUser() + ":"
					+ AmbariUtil.getDecryptPassword(sdpsServerInfo.getPasswd());
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			headers.set(HttpHeaders.AUTHORIZATION, "Basic "
					+ new String(base64CredsBytes));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			ambariUtil.setHeaders(headers);
			// clusterName = getClusterName();

			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			BigdataCommonFegin bigdataCommonFegin = SpringBeanUtil
					.getBean(BigdataCommonFegin.class);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "hostInfo");
			return ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
		} catch (Exception e) {
			log.error("??????????????????host????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param hostMap
	 *            ????????????
	 */
	@PostMapping("/validateHostMsg")
	public JSONObject validatePlatformAccountPaaswd(
			@RequestBody Map<String, Object> hostMap) {
		try {
			AmbariUtil instance = AmbariUtil.getInstance();

			// http://10.1.3.18:8080/api/users/{username}?fields...
			String url = "http://" + hostMap.get("ip") + ":"
					+ hostMap.get("port");
			String username = String.valueOf(hostMap.get("username"));
			instance.setUrl(url);
			String plainCreds = username + ":" + hostMap.get("passwd");
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			// instance.setClusterName(instance.getClusterName());

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			// http://10.1.3.18:8080/api/v1/users/admin?fields=*,privileges/PrivilegeInfo/cluster_name,privileges/PrivilegeInfo/permission_name&_=1639725716135
			String requestUrl = "/api/v1/users/"
					+ username
					+ "?fields=*,privileges/PrivilegeInfo/cluster_name,privileges/PrivilegeInfo/permission_name";
			JSONObject validateResult = instance
					.getAmbariApi(requestUrl, "GET");
			return validateResult;
		} catch (Exception e) {
			log.error("", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????HDFS????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param hdfsPath
	 *            hdfs??????
	 * @return ??????????????????
	 */
	@GetMapping("/selectHdfsSaveObjList")
	public Result<List<HdfsFSObj>> selectHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			// System.setProperty("HADOOP_USER_NAME", "hdfs");
			bigdataVirtualHost.setVirtualHost(clusterId);
			HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
			ArrayList<HdfsFSObj> hdfsFSObjs = hdfsUtil
					.selectHdfsSaveObjList(hdfsPath);
			hdfsUtil.closeFs();
			return Result.succeed(hdfsFSObjs, "????????????");
		} catch (Exception e) {
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ?????????????????????HDFS??????????????????
	 * 
	 * @param userId
	 * @param username
	 * @param clusterId
	 * @param hdfsPath
	 * @return
	 */
	@GetMapping("/selectHdfsSaveObjListByUser")
	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(
			@RequestParam("userId") Long userId,
			@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			List<String> paths = null;
			if (StrUtil.equalsIgnoreCase("/project", hdfsPath)) {
				Result result = itemCenterFegin.findItemsByUser(username,
						userId, Long.valueOf(clusterId));
				if (result.isFailed()) {
					return Result.failed(result.getMsg());
				}
				List<Map<String, Object>> datas = (List<Map<String, Object>>) result
						.getData();
				paths = CollUtil.newArrayList();
				for (Map<String, Object> data : datas) {
					paths.add(data.get("iden").toString());
				}

			}
			bigdataVirtualHost.setVirtualHost(clusterId);
			HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
			ArrayList<HdfsFSObj> hdfsFSObjs = hdfsUtil
					.selectHdfsSaveObjList(hdfsPath);
			hdfsUtil.closeFs();
			if (null != paths) {
				List<HdfsFSObj> removeList = CollUtil.newArrayList();
				for (HdfsFSObj hdfsFSObj : hdfsFSObjs) {
					if (!paths.contains(hdfsFSObj.getFileName())) {
						removeList.add(hdfsFSObj);
					}
				}
				hdfsFSObjs.removeAll(removeList);
			}
			return Result.succeed(hdfsFSObjs, "????????????");
		} catch (Exception e) {
			log.error("????????????", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/configThemes")
	public JSONObject configThemes(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName) {
		log.info("configThemes clusterId:{}  serviceName:{}", clusterId,
				serviceName);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil
				.configThemes(ambariUtil.getClusterName(), serviceName);
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/getConfigInfo")
	public JSONObject getConfigInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName) {
		log.info("getConfigInfo clusterId:{}  serviceName:{}", clusterId,
				serviceName);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.getConfigInfo(ambariUtil.getClusterName(),
				serviceName);
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/getConfigAllVersion")
	public JSONObject getConfigAllVersion(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName) {
		log.info("getConfigAllVersion clusterId:{}  serviceName:{}", clusterId,
				serviceName);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.getConfigAllVersion(ambariUtil.getClusterName(),
				serviceName);
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	@GetMapping("/getConfigGroup")
	public JSONObject getConfigGroup(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceName") String serviceName) {
		log.info("getConfigGroup clusterId:{}  serviceName:{}", clusterId,
				serviceName);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.getConfigGroup(ambariUtil.getClusterName(),
				serviceName);
	}

	/**
	 * ????????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @return
	 */
	@GetMapping("/getConfigHostInfo")
	public JSONObject getConfigHostInfo(
			@RequestParam("clusterId") Integer clusterId) {
		log.info("getConfigHostInfo clusterId:{}", clusterId);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.getConfigHostInfo(bigdataCommonFegin,
				ambariUtil.getClusterName());
	}

	/**
	 * ???????????????
	 *
	 * @param configGroup
	 *            ???????????????
	 * @return
	 */
	@PostMapping("/updateConfigGroup")
	public JSONObject updateConfigGroup(@RequestBody ConfigGroup configGroup) {
		log.info("updateConfigGroup clusterId:{}  configGroup:{}", configGroup);
		AmbariUtil ambariUtil = new AmbariUtil(configGroup.getClusterId());
		return ambariUtil.updateConfigGroup(bigdataCommonFegin,
				ambariUtil.getClusterName(), configGroup);
	}

	/**
	 * ???????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param groupId
	 *            ?????????id
	 * @return
	 */
	@DeleteMapping("/deleteConfigGroup")
	public JSONObject deleteConfigGroup(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupId") Integer groupId) {
		log.info("deleteConfigGroup clusterId:{} groupId:{}", clusterId,
				groupId);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.deleteConfigGroup(bigdataCommonFegin,
				ambariUtil.getClusterName(), groupId);
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????
	 * @return
	 */
	@PostMapping("/configValidations")
	public JSONObject configValidations(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings) {
		log.info("configValidations clusterId:{}	setting:{}", clusterId,
				settings);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.configValidations(bigdataCommonFegin, settings);
	}

	/**
	 * ???????????????????????????URL??????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param stackName
	 *            ????????????
	 * @param name
	 *            repo??????
	 * @param version
	 *            ??????
	 * @param osType
	 *            ??????????????????
	 * @param repositories
	 *            ??????
	 */
	@PostMapping("/resourceOsUrlValidation")
	public JSONObject resourceOsUrlValidation(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("name") String name,
			@RequestParam("version") String version,
			@RequestParam("osType") String osType,
			@RequestBody JSONObject repositories) {
		log.info(
				"resourceOsUrlValidation clusterId:{} name:{} version {} osType {} repositories:{}",
				clusterId, name, version, osType, repositories.toJSONString());
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.resourceOsUrlValidation(stackName, version, osType,
				name, repositories);
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param clusterId
	 *            ??????Id
	 * @param id
	 *            id
	 * @param stackName
	 *            ????????????
	 * @param stackVersion
	 *            ????????????
	 * @param repositories
	 *            ??????
	 */
	@PutMapping("/clusterVersionSave")
	public JSONObject clusterVersionSave(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("stackName") String stackName,
			@RequestParam("stackVersion") String stackVersion,
			@RequestParam("id") Integer id, @RequestBody JSONObject repositories) {
		log.info(
				"clusterVersionSave clusterId:{} stackName {} stackVersion {} id {} repositories:{}",
				clusterId, stackName, stackVersion, id,
				repositories.toJSONString());
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.clusterVersionSave(stackName, stackVersion, id,
				repositories);
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 */
	@GetMapping("/stackHistory")
	public JSONObject stackHistory(@RequestParam("clusterId") Integer clusterId) {
		return new AmbariUtil(clusterId).stackHistory();
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 */
	@GetMapping("/clusters")
	public JSONObject clusters(@RequestParam("clusterId") Integer clusterId) {
		return new AmbariUtil(clusterId).clusters();
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????
	 * @return
	 */
	@PostMapping("/configRecommendations")
	public JSONObject configRecommendations(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject settings) {
		log.info("configRecommendations clusterId:{}  settings:{}", clusterId,
				settings);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.configRecommendations(bigdataCommonFegin, settings);
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @param settings
	 *            ??????????????????
	 * @return
	 */
	@PostMapping("/updateConfig")
	public JSONObject updateConfig(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONArray settings) {
		log.info("configRecommendations clusterId:{}  settings:{}", clusterId,
				settings);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		return ambariUtil.updateConfig(bigdataCommonFegin,
				ambariUtil.getClusterName(), settings);
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterName")
	public JSONObject getClusterName(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "clusterName");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			return AmbariUtil.analysisClusterName(result);
		} catch (Exception e) {
			log.error("??????????????????host????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ?????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/startOrStopService")
	public JSONObject startOrStopService(
			@RequestBody AmbariStartOrStopServiceObj obj) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(obj.getClusterId());
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			param.put(2, obj.getServiceName());
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Requested-By", "X-Requested-By");
			JSONObject data = getStartOrStopBody(obj);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "startOrStopService");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), param);
			return result;
		} catch (Exception e) {
			log.error("???????????????????????????", e);
			JSONObject error = new JSONObject();
			error.put("errorMsg", e.getMessage());
			return error;
		}
	}

	private JSONObject getStartOrStopBody(AmbariStartOrStopServiceObj obj) {
		JSONObject result = new JSONObject();
		JSONObject requestInfo = new JSONObject();
		if (StrUtil.equalsIgnoreCase("STARTED", "obj.getState()")) {
			requestInfo.put("context", "_PARSE_.START." + obj.getServiceName());
		} else if (StrUtil.equalsIgnoreCase("INSTALLED", "obj.getState()")) {
			requestInfo.put("context", "_PARSE_.STOP." + obj.getServiceName());
		} else {
			throw new BusinessException("?????????????????????");
		}
		JSONObject operationLevelJson = new JSONObject();
		operationLevelJson.put("level", "SERVICE");
		operationLevelJson.put("cluster_name", obj.getClusterName());
		operationLevelJson.put("service_name", obj.getServiceName());
		requestInfo.put("operation_level", operationLevelJson);
		result.put("RequestInfo", requestInfo);
		JSONObject serviceInfoJson = new JSONObject();
		serviceInfoJson.put("state", obj.getState());
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("ServiceInfo", serviceInfoJson);
		result.put("Body", bodyJson);
		return result;
	}

	public static Map<Integer, Object> getClusterNameParam(AmbariUtil ambariUtil) {
		String clusterName = ambariUtil.getClusterName();
		if (StrUtil.isBlank(clusterName)) {
			clusterName = ambariUtil.getClusterName();
		}
		Map<Integer, Object> param = MapUtil.newHashMap();
		param.put(1, clusterName);
		return param;
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getComponentInfo")
	public JSONObject getComponentInfo(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "componentStatus");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			return AmbariUtil.analysisComponentInfo(result);
		} catch (Exception e) {
			log.error("??????????????????host????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ambari??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param file
	 *            ??????????????????
	 * @param path
	 *            ??????
	 * @return ???????????????????????????????????????
	 */
	@PostMapping("/uploadFile")
	public Result uploadFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam("isUserFile") boolean isUserFile,
			@RequestParam("isCrypto") boolean isCrypto) {
		if (file.isEmpty()) {
			return Result.failed("????????????????????????????????????.");
		}
		if (isUserFile) {
			userHomeDirIFNoexistCreateIFExistDoNothing(clusterId, username);
		}
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		try {
			bigdataVirtualHost.setVirtualHost(clusterId);
			// Boolean exist =
			// hdfsUtil.isExist(path.concat("/").concat(file.getOriginalFilename()));
			Boolean exist = hdfsUtil.isExist(path.concat("/project/").concat(
					file.getOriginalFilename()));
			if (exist) {
				return Result.failed("???????????????,??????????????????");
			} else {
				AmbariUtil ambariUtil = new AmbariUtil(clusterId);
				ambariUtil.getHeaders().setContentType(
						MediaType.MULTIPART_FORM_DATA);
				ambariUtil.getHeaders().set("X-Requested-By", "ambari");
				MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
				InputStream inputStream = null;
				if(isCrypto){
					fileCryptoProperties.getSecretKey();
					inputStream = new CipherInputStream(file.getInputStream(),
							CryptoCipherBuilder.buildDes3Crypt("123456781234567812345678",
									true));
				}else{
					inputStream = file.getInputStream();
				}
				Resource resource = new CommonInputStreamResource(
						inputStream, file.getOriginalFilename());
				parts.add("file", resource);
				parts.add("path", path);
				Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
						"ambari", "uploadFile");
				JSONObject result = ambariUtil.uploadFile(username, args
						.getData().getArgValue(), args.getData()
						.getArgValueDesc(), parts);

				Boolean isExistFile = hdfsUtil.isExist(path.concat("/").concat(
						file.getOriginalFilename()));
				if (isExistFile) {
					return Result.succeed(result, "??????????????????");
				} else {
					return Result.failed("??????????????????!");
				}
			}
		} catch (Exception e) {
			String message = "?????????????????????";
			if (e.getMessage().contains("AccessControlException")) {
				message = "Permission denied: user ".concat(username);
			}
			log.error("??????????????????,{}", e.getMessage(), e);
			return Result.failed("??????????????????,".concat(message));
		} finally {
			hdfsUtil.closeFs();
		}
	}

	/**
	 * ????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/restartAllComponent")
	public JSONObject restartAllComponent(@RequestBody JSONObject data) {
		try {
			Integer clusterId = data.getInteger("clusterId");
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(
					MediaType.MULTIPART_FORM_DATA);
			ambariUtil.getHeaders().set("X-Requested-By", "ambari");
			data.put("clusterId", null);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "restartAllComponent");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), getClusterNameParam(ambariUtil));
			return result;
		} catch (Exception e) {
			log.error("??????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ?????????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/startOrStopComponent")
	public JSONObject startOrStopComponent(@RequestBody JSONObject data) {
		try {
			Integer clusterId = data.getInteger("clusterId");
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(
					MediaType.MULTIPART_FORM_DATA);
			ambariUtil.getHeaders().set("X-Requested-By", "ambari");
			data.put("clusterId", null);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "startOrStopComponent");
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			param.put(
					2,
					data.getJSONObject("RequestInfo")
							.getJSONObject("operation_level")
							.getString("host_name"));
			param.put(3, data.getString("componetName"));
			data.put("componetName", null);
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), param);
			return result;
		} catch (Exception e) {
			log.error("???????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 * @return
	 */
	@PostMapping("/restartComponent")
	public JSONObject restartComponent(@RequestBody JSONObject data) {
		try {
			Integer clusterId = data.getInteger("clusterId");
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(
					MediaType.MULTIPART_FORM_DATA);
			ambariUtil.getHeaders().set("X-Requested-By", "ambari");
			data.put("clusterId", null);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "restartComponent");
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(), data
					.toJSONString(), param);
			return result;
		} catch (Exception e) {
			log.error("????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param path
	 *            ????????????????????????
	 * @param permission
	 *            ?????????
	 * @return ??????????????????
	 */
	@GetMapping("/setPermission")
	public boolean permission(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("permission") String permission) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		boolean flag = hdfsUtil.permission(path, permission);
		log.info("flag:{}", flag);
		return flag;
	}

	/**
	 * ???????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param oldPath
	 *            ???????????????
	 * @param newPath
	 *            ???????????????
	 * @return ?????????????????????
	 */
	@GetMapping("/rename")
	public boolean rename(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("oldPath") String oldPath,
			@RequestParam("newPath") String newPath) {
		if (StrUtil.isBlankIfStr(clusterId)) {
			log.error("clusterId??????:{}", clusterId);
			return false;
		}
		bigdataVirtualHost.setVirtualHost(clusterId);
		HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
		return hdfsUtil.rename(oldPath, newPath);
	}

	/**
	 * hdfs????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param path
	 *            ?????????????????????
	 * @return ??????????????????
	 */
	@GetMapping("/download")
	public void download(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path) {
		bigdataVirtualHost.setVirtualHost(clusterId);
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		ambariUtil
				.getHeaders()
				.set("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		Result<SysGlobalArgs> param = bigdataCommonFegin.getGlobalParam(
				"ambari", "downloadFile");
		ambariUtil.download(username, param.getData().getArgValue(), param
				.getData().getArgValueDesc(), path);
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 * @param serverName
	 * @param confStrs
	 * @return
	 */
	@GetMapping("/getServerConfByConfName")
	public String getServerConfByConfName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serverName") String serverName,
			@RequestParam("confStrs") List<String> confStrs) {
		String result = "";
		try {
			result = new AmbariUtil(clusterId).getAmbariServerConfByConfName(
					serverName, confStrs);
		} catch (Exception e) {
			return e.toString();
		}
		return result;
	}

	/**
	 * ????????????Host???IP????????????
	 * 
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/getClusterHostConf")
	public List<ClusterHostConf> getClusterHostConf(
			@RequestParam("clusterId") Integer clusterId) {
		ArrayList<ClusterHostConf> resultList = new ArrayList<>();
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "ip");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc(),
					getClusterNameParam(ambariUtil));
			JSONArray items = result.getJSONArray("items");
			items.forEach(item -> {
				JSONObject jsonObj = (JSONObject) item;
				JSONObject host = jsonObj.getJSONObject("Hosts");
				String hostName = host.getString("host_name");
				String ip = host.getString("ip");
				resultList.add(new ClusterHostConf(hostName, ip));
			});
		} catch (Exception e) {
			log.error("????????????Host???IP??????????????????:", e);
		}
		return resultList;
	}

	/**
	 * ??????YarnApplicationLog???Url??????
	 */
	@GetMapping("/getYarnApplicationLogUrl")
	public String getYarnApplicationLogUrl(
			@RequestParam("clusterId") Integer clusterId) {
		String yarnApplicationLogUrl = "";
		try {
			List<String> confList = new ArrayList<>();
			confList.add("yarn-site");
			String confYarn = getServerConfByConfName(clusterId, "YARN",
					confList);
			Map yarnConfMap = JSON.parseObject(confYarn, Map.class);
			String resourcemanagerIpPost = (String) yarnConfMap
					.get(BigDataConfConstants.YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
			String[] arr = resourcemanagerIpPost.split(":");
			String host = arr[0];
			String port = arr[1];
			String ip = "";
			List<ClusterHostConf> clusterHostConfList = getClusterHostConf(clusterId);
			for (ClusterHostConf clusterHostConf : clusterHostConfList) {
				if (host.equals(clusterHostConf.getHost())) {
					ip = clusterHostConf.getIp();
					break;
				}
			}
			yarnApplicationLogUrl = yarnApplicationLogUrl.concat(ip)
					.concat(":").concat(port).concat("/cluster/app");
		} catch (Exception e) {
			log.error("??????YarnApplicationLog???Url????????????:", e);
		}
		return yarnApplicationLogUrl;
	}

	@GetMapping("/execFetchAndExtractHdfsMetaData")
	public Boolean execFetchAndExtractHdfsMetaData(
			@RequestParam("clusterId") Integer clusterId) {
		boolean flag = false;
		try {
			flag = metaDataExtract.fetchAndExtractHdfsMetaData(clusterId);
		} catch (Exception e) {
			log.error("??????HDFS??????????????????HDFS???????????????:", e);
		}
		return flag;
	}

	/**
	 * ????????????????????????????????????
	 */
	@GetMapping("/performOperation")
	public JSONObject performOperation(@RequestParam("id") Integer id) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(id);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "performOperation");
			SysGlobalArgs globalArgs = args.getData();
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			return ambariUtil.getAmbariApi(globalArgs.getArgValue(),
					globalArgs.getArgValueDesc(), param);
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param id
	 *            ??????ID
	 * @param nodeId
	 *            ??????ID
	 */
	@GetMapping("/performOperationDetail")
	public JSONObject performOperationDetail(@RequestParam("id") Integer id,
			@RequestParam("nodeId") Integer nodeId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(id);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "performOperationDetail");
			SysGlobalArgs globalArgs = args.getData();
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			param.put(2, nodeId);
			return ambariUtil.getAmbariApi(globalArgs.getArgValue(),
					globalArgs.getArgValueDesc(), param);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param id
	 *            ??????ID
	 */
	@GetMapping("/alarmMsg")
	public JSONObject alarmMsg(@RequestParam("id") Integer id) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(id);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "cluster_alert_definition");
			SysGlobalArgs globalArgs = args.getData();
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			JSONObject clusterAlertDefinition = ambariUtil.getAmbariApi(
					globalArgs.getArgValue(), globalArgs.getArgValueDesc(),
					param);

			args = bigdataCommonFegin.getGlobalParam("ambari",
					"cluster_alert_definition_summary");
			globalArgs = args.getData();
			JSONObject clusterAlertDefinitionSummary = ambariUtil.getAmbariApi(
					globalArgs.getArgValue(), globalArgs.getArgValueDesc(),
					param);

			args = bigdataCommonFegin.getGlobalParam("ambari",
					"cluster_alert_service_component");
			globalArgs = args.getData();
			JSONObject clusterAlertServiceComponent = ambariUtil.getAmbariApi(
					globalArgs.getArgValue(), globalArgs.getArgValueDesc(),
					param);

			JSONObject alertResult = new JSONObject();
			alertResult.put("clusterAlertDefinition", clusterAlertDefinition);
			alertResult.put("clusterAlertDefinitionSummary",
					clusterAlertDefinitionSummary);
			alertResult.put("clusterAlertServiceComponent",
					clusterAlertServiceComponent);

			return alertResult;
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????yarn queue????????????
	 */
	@GetMapping("/getYarnQueueConfigurate")
	public JSONObject getYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().remove("X-Requested-By");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "yarnQueueConfguration");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			String yarnSite = ambariUtil.getAmbariServerConfByConfName("YARN",
					CollUtil.newArrayList("yarn-site"));
			return AmbariUtil.analysisYarnQueueConfig(result, yarnSite);
		} catch (Exception e) {
			log.error("??????yarn??????????????????", e);
			return new JSONObject();
		}
	}

	/**
	 * ??????yarn queue????????????
	 */
	@PostMapping("/upateYarnQueueConfigurate")
	public Result updateYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().remove("X-Requested-By");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "yarnQueueConfguration");
			JSONObject sourceResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			String yarnSite = ambariUtil.getAmbariServerConfByConfName("YARN",
					CollUtil.newArrayList("yarn-site"));
			JSONObject body = AmbariUtil.getUpdateYarnQueueBody(infos,
					sourceResult, yarnSite);
			if (Objects.isNull(body)) {
				return Result.failed("????????????????????????");
			}
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"updateYarnQueueConfiguration");
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().add("X-Requested-With", "XMLHttpRequest");
			ambariUtil.getHeaders().add("X-Requested-By",
					"view-capacity-scheduler");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"saveAnfRestartYarnQueue");
			ambariUtil.getHeaders().setContentType(
					MediaType.APPLICATION_JSON_UTF8);
			body = new JSONObject();
			body.put("save", true);
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ??????yarn queue????????????
	 */
	@PostMapping("/deleteYarnQueueConfigurate")
	public Result deleteYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			List<YarnQueueConfInfo> delYarnQueueConfInfos = infos
					.stream()
					.filter(info -> Objects.nonNull(info.getIsDelete())
							&& info.getIsDelete()).collect(Collectors.toList());
			boolean isHasJob = false;
			for (YarnQueueConfInfo yarnQueueConfInfo : delYarnQueueConfInfos) {
				JSONObject yarnJobs = seaBoxYarnController.getJobs(clusterId,
						"RUNNING", yarnQueueConfInfo.getQueueName());
				if (yarnJobs.isEmpty()) {
					throw new BusinessException("??????yarn????????????");
				}
				if (!(yarnJobs.getJSONObject("apps").isEmpty())) {
					isHasJob = true;
					break;
				}
			}
			if (isHasJob) {
				return Result.failed("??????????????????????????????????????????,?????????????????????????????????");
			}
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().remove("X-Requested-By");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "yarnQueueConfguration");
			JSONObject sourceResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			JSONObject body = AmbariUtil.getDeleteYarnQueueBody(sourceResult,
					infos);
			if (Objects.isNull(body)) {
				return Result.failed("????????????????????????");
			}
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"updateYarnQueueConfiguration");
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().add("X-Requested-With", "XMLHttpRequest");
			ambariUtil.getHeaders().add("X-Requested-By",
					"view-capacity-scheduler");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"saveAnfRestartYarnQueue");
			ambariUtil.getHeaders().setContentType(
					MediaType.APPLICATION_JSON_UTF8);
			body = new JSONObject();
			body.put("save", true);
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("??????yarn??????????????????", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ??????yarn queue????????????
	 */
	@PostMapping("/insertYarnQueueConfigurate")
	public Result insertYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().remove("X-Requested-By");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "yarnQueueConfguration");
			JSONObject sourceResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			String yarnSite = ambariUtil.getAmbariServerConfByConfName("YARN",
					CollUtil.newArrayList("yarn-site"));
			JSONObject body = AmbariUtil.getInsertYarnQueueBody(sourceResult,
					yarnSite, infos);
			if (Objects.isNull(body)) {
				return Result.failed("????????????????????????");
			}
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"updateYarnQueueConfiguration");
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().add("X-Requested-With", "XMLHttpRequest");
			ambariUtil.getHeaders().add("X-Requested-By",
					"view-capacity-scheduler");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"saveAnfRestartYarnQueue");
			ambariUtil.getHeaders().setContentType(
					MediaType.APPLICATION_JSON_UTF8);
			body = new JSONObject();
			body.put("save", true);
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("??????yarn????????????", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ???????????????yarn queue????????????
	 */
	@PostMapping("/stopOrRunningYarnQueue")
	public Result stopOrRunningYarnQueue(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			List<YarnQueueConfInfo> delYarnQueueConfInfos = infos
					.stream()
					.filter(info -> StrUtil.equalsAnyIgnoreCase(
							info.getState(), "STOPPED"))
					.collect(Collectors.toList());
			boolean isHasJob = false;
			for (YarnQueueConfInfo yarnQueueConfInfo : delYarnQueueConfInfos) {
				JSONObject yarnJobs = seaBoxYarnController.getJobs(clusterId,
						"RUNNING", yarnQueueConfInfo.getQueueName());
				if (yarnJobs.isEmpty()) {
					throw new BusinessException("??????yarn????????????");
				}
				if (!(yarnJobs.getJSONObject("apps").isEmpty())) {
					isHasJob = true;
					break;
				}
			}
			if (isHasJob) {
				return Result.failed("??????????????????????????????????????????,?????????????????????????????????");
			}
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().remove("X-Requested-By");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "yarnQueueConfguration");
			JSONObject sourceResult = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			JSONObject body = AmbariUtil.getUpdateYarnQueueBody(infos,
					sourceResult);
			if (Objects.isNull(body)) {
				return Result.failed("????????????????????????");
			}
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"updateYarnQueueConfiguration");
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			ambariUtil.getHeaders().add("X-Requested-With", "XMLHttpRequest");
			ambariUtil.getHeaders().add("X-Requested-By",
					"view-capacity-scheduler");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"saveAnfRestartYarnQueue");
			ambariUtil.getHeaders().setContentType(
					MediaType.APPLICATION_JSON_UTF8);
			body = new JSONObject();
			body.put("save", true);
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), body.toJSONString(), true);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("???????????????yarn??????????????????", e);
			return Result.failed(e.getMessage());
		}
	}

	@PostMapping("/saveClusterWithName")
	JSONObject saveClusterWithName(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject reJson) {
		try {

			// http://10.1.3.11:8080/api/v1/clusters/AAQQ
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			// HttpHeaders headers = new HttpHeaders();
			// headers.set("Connection", "keep-alive");
			// headers.set("Accept", "text/plain, */*; q=0.01");
			// headers.set("X-Requested-With", "XMLHttpRequest");
			// headers.set("X-Requested-By", "X-Requested-By");
			// headers.set("X-Requested-By", "ambari");
			// headers.set("Content-Type", "text/plain");
			// headers.set("Accept-Language", "zh-CN,zh;q=0.9");
			// headers.set("Authorization", "Basic YWRtaW46YWRtaW4=");
			// instance.setHeaders(headers);
			// instance.getHeaders().set("X-Http-Method-Override", "GET");

			HttpHeaders headers = new HttpHeaders();
			headers.set("Connection", "keep-alive");
			headers.set("Accept", "text/plain, */*; q=0.01");
			headers.set("X-Requested-With", "XMLHttpRequest");
			headers.set("X-Requested-By", "X-Requested-By");
			headers.set(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
			headers.set("Content-Type", "text/plain");
			headers.set("Origin", "http://" + masterIp + ":8080");
			headers.set("Referer", "http://" + masterIp + ":8080");
			headers.set("Accept-Language", "zh-CN,zh;q=0.9");
			headers.set("Authorization", "Basic YWRtaW46YWRtaW4=");
			instance.setHeaders(headers);

			String path = "/api/v1/clusters/" + clusterName;
			// path = "/api/v1/clusters/" + "cn28";
			JSONObject saveClusterResult = instance.getAmbariApi(path, "POST",
					reJson.toJSONString(), null, false);
			return saveClusterResult.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/saveClusterService")
	JSONObject saveClusterService(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody List<JSONObject> serviceInfos) {
		// http://10.1.3.11:8080/api/v1/clusters/AAQQ/services POST
		// [{"ServiceInfo":{"service_name":"HDFS","desired_repository_version_id":1}},{"ServiceInfo":{"service_name":"ZOOKEEPER","desired_repository_version_id":1}},
		// {"ServiceInfo":{"service_name":"AMBARI_METRICS","desired_repository_version_id":1}},{"ServiceInfo":{"service_name":"SMARTSENSE","desired_repository_version_id":1}}]
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Connection", "keep-alive");
			headers.set("Accept", "text/plain, */*; q=0.01");
			headers.set("X-Requested-With", "XMLHttpRequest");
			headers.set("X-Requested-By", "X-Requested-By");
			headers.set(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
			headers.set("Content-Type", "text/plain");
			headers.set("Origin", "http://" + masterIp + ":8080");
			headers.set("Referer", "http://" + masterIp + ":8080");
			headers.set("Accept-Language", "zh-CN,zh;q=0.9");
			headers.set("Authorization", "Basic YWRtaW46YWRtaW4=");
			instance.setHeaders(headers);

			JSONObject result = instance.getAmbariApi("/api/v1/clusters/"
					+ clusterName + "/services", "POST",
					JSONArray.toJSONString(serviceInfos), null, false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@PutMapping("/saveClusterServiceXmlConfigurations")
	JSONObject saveClusterServiceXmlConfigurations(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody String reqJson) {
		// [{"Clusters":{"desired_config":[{"type":"druid-broker","properties":{"druid.port":"8082","druid.service":"druid/broker"},"service_config_version_note":"???????????? Druid"}]}}]
		// http://10.1.3.24:9999/api/v1/clusters/seabox4 PUT
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters/" + clusterName;
			JSONObject saveClusterServiceXmlConfigurationsResult = instance
					.getAmbariApi(path, "PUT", reqJson, null, false);
			return saveClusterServiceXmlConfigurationsResult.fluentPut(
					"result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	private AmbariUtil ambariInstanceHttp(String masterIp)
			throws UnknownHostException {
		AmbariUtil instance = ambariInstance(masterIp);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Connection", "keep-alive");
		headers.set("Accept", "text/plain, */*; q=0.01");
		headers.set("X-Requested-With", "XMLHttpRequest");
		headers.set("X-Requested-By", "X-Requested-By");
		headers.set(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
		// / headers.set("Content-Type", "text/plain");
		headers.set("Content-Type", "application/json");
		headers.set("Origin", "http://" + masterIp + ":8080");
		headers.set("Referer", "http://" + masterIp + ":8080/");
		headers.set("Accept-Language", "zh-CN,zh;q=0.9");
		headers.set("Authorization", "Basic YWRtaW46YWRtaW4=");
		instance.setHeaders(headers);

		return instance;
	}

	private AmbariUtil ambariInstance(String masterIp)
			throws UnknownHostException {
		AmbariUtil instance = AmbariUtil.getInstance();

		/*
		 * InetAddress addr = InetAddress.getLocalHost();
		 * System.out.println("Local HostAddress: "+addr.getHostAddress()); //
		 * String hostname = addr.getHostName();
		 * 
		 * // ip port user passwd instance.setUrl("http://" +
		 * addr.getHostAddress() + ":" + "8080");
		 */
		instance.setUrl("http://" + masterIp + ":" + "8080");
		String plainCreds = "admin" + ":" + "admin";
		byte[] base64CredsBytes = Base64.encodeBase64(plainCreds.getBytes());
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Requested-By", "ambari");
		instance.setHeaders(headers);

		instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		instance.getHeaders().set("X-Http-Method-Override", "GET");

		return instance;
	}

	// ?????????????????????API
	@PostMapping("/saveStackVersion")
	JSONObject saveStackVersion(
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject clusterJson) {
		try {
			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>
			AmbariUtil instance = AmbariUtil.getInstance();

			InetAddress addr = InetAddress.getLocalHost();
			System.out.println("Local HostAddress: " + addr.getHostAddress());
			// String hostname = addr.getHostName();

			// ip port user passwd
			instance.setUrl("http://" + addr.getHostAddress() + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName,
					"POST", clusterJson.toJSONString(), true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ???????????????????????????host
	@PostMapping("/saveClusterHosts")
	JSONObject saveClusterHosts(
			@RequestParam("clusterName") String clusterName,
			@RequestParam String hostIp) {
		try {
			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>/hosts/<master/slave
			// host>
			AmbariUtil instance = AmbariUtil.getInstance();

			String hostAddr = getHostAddr();

			// ip port user passwd
			instance.setUrl("http://" + hostAddr + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName
					+ "/hosts/" + hostIp, "POST");
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ??????hdfs??????
	@PostMapping("/saveClusterComponent")
	JSONObject saveClusterComponent(
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject componentInfo) {
		try {
			// {
			// "ServiceInfo": {
			// "service_name": "HDFS"
			// }
			// }
			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>/services

			AmbariUtil instance = AmbariUtil.getInstance();
			String hostAddr = getHostAddr();

			// ip port user passwd
			instance.setUrl("http://" + hostAddr + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName
					+ "/services", "POST", componentInfo.toJSONString(), true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ?????????????????? ?????????namenode datanode???
	@PostMapping("/saveClusterComponentNode")
	JSONObject saveClusterComponentNode(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody List<JSONObject> componentNode) {
		try {
			// 3. ????????????????????????
			// http://10.1.3.11:8080/api/v1/clusters/AAQQ/services?ServiceInfo/service_name=HDFS
			// POST
			// ServiceInfo/service_name=HDFS
			// {"components":[{"ServiceComponentInfo":{"component_name":"DATANODE"}},{"ServiceComponentInfo":{"component_name":"HDFS_CLIENT"}},
			// {"ServiceComponentInfo":{"component_name":"JOURNALNODE"}},{"ServiceComponentInfo":{"component_name":"NAMENODE"}},
			// {"ServiceComponentInfo":{"component_name":"NFS_GATEWAY"}},{"ServiceComponentInfo":{"component_name":"SECONDARY_NAMENODE"}},
			// {"ServiceComponentInfo":{"component_name":"ZKFC"}}]}
			//
			// ZOOKEEPER_SERVER
			// {"components":[{"ServiceComponentInfo":{"component_name":"ZOOKEEPER_CLIENT"}},{"ServiceComponentInfo":{"component_name":"ZOOKEEPER_SERVER"}}]}

			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters/" + clusterName
					+ "/services?ServiceInfo/service_name=" + serviceName;
			return instance.getAmbariApi(path, "POST",
					JSONArray.toJSONString(componentNode), null, false);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ???????????????HDFS?????????
	@PostMapping("/saveClusterConfigurations")
	JSONObject saveClusterConfigurations(
			@RequestParam("clusterName") String clusterName,
			@RequestBody JSONObject properties) {
		try {
			// {
			// "type": "hadoop-user-info.properties",
			// "tag": "1",
			// "properties": {
			// "root_ugi": "root,baidu",
			// "user_ugi": "public,slave",
			// "content":
			// " # Format: username=password,group1,group2,group3 root=baidu,root public=slave,slave "
			// }
			// }

			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>/configurations
			AmbariUtil instance = AmbariUtil.getInstance();
			String hostAddr = getHostAddr();

			// ip port user passwd
			instance.setUrl("http://" + hostAddr + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName
					+ "/configurations/", "POST", properties.toJSONString(),
					true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ???????????????????????????

	// ????????????????????????????????????host (03???namenode 02 06???datanode)
	@PostMapping("/saveClusterComponentHost")
	JSONObject saveClusterComponentHost(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestBody String reqBodyJson) {
		try {

			// http://10.1.3.11:8080/api/v1/clusters/AAQQ/hosts POST
			// [{"Hosts":{"host_name":"master"}},{"Hosts":{"host_name":"node1"}},{"Hosts":{"host_name":"node2"}}]
			// {"RequestInfo":{"query":"Hosts/host_name=master"},"Body":{"host_components":[{"HostRoles":{"component_name":"NAMENODE"}}]}}
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			JSONObject saveClusterComponentHostResult = instance.getAmbariApi(
					"/api/v1/clusters/" + clusterName + "/hosts", "POST",
					reqBodyJson, null, false);
			return saveClusterComponentHostResult.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ????????????
	@PutMapping("/clusterInstall")
	JSONObject clusterInstall(@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody JSONObject serviceInfo) {
		try {
			// {
			// "ServiceInfo": {
			// "state": "INSTALLED"
			// }
			// }

			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>/services/<service-name>
			AmbariUtil instance = AmbariUtil.getInstance();
			String hostAddr = getHostAddr();

			// ip port user passwd
			instance.setUrl("http://" + hostAddr + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName
					+ "/services/" + serviceName, "POST",
					serviceInfo.toJSONString(), true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ????????????
	//
	// ???????????? ???????????????????????????????????????????????????????????????????????????????????????started??????
	// ????????????ambari-api??????????????????ambari?????????????????????????????????????????????python????????????stop?????????????????????
	@PutMapping("/clusterStart")
	JSONObject clusterStart(@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody JSONObject serviceInfo) {
		try {
			// {
			// "ServiceInfo": {
			// "state": "STARTED"
			// }
			// }

			// http://<ambari-server>:8080/api/v1/clusters/<cluster-name>/services/<service-name>
			AmbariUtil instance = AmbariUtil.getInstance();
			String hostAddr = getHostAddr();

			// ip port user passwd
			instance.setUrl("http://" + hostAddr + ":" + "8080");
			String plainCreds = "admin" + ":" + "admin";
			byte[] base64CredsBytes = Base64
					.encodeBase64(plainCreds.getBytes());
			String base64Creds = new String(base64CredsBytes);

			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instance.setHeaders(headers);

			instance.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			instance.getHeaders().set("X-Http-Method-Override", "GET");
			return instance.getAmbariApi("/api/v1/clusters/" + clusterName
					+ "/services/" + serviceName, "POST",
					serviceInfo.toJSONString(), true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@PostMapping("/saveVersionDefinition")
	JSONObject saveVersionDefinition(@RequestParam("masterIp") String masterIp,
			@RequestParam("versionDefinition") String versionDefinition) {
		try {
			// ??????rep_version
			// http://10.1.3.11:8080/api/v1/version_definitions POST
			// {"VersionDefinition":{"available":"HDP-3.1"}}
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			// AmbariUtil instance = ambariInstance(masterIp);
			JSONObject getResult = instance.getAmbariApi(
					"/api/v1/version_definitions", "POST", versionDefinition,
					null, false);
			return getResult.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PutMapping("/saveCustomVersionDefinition")
	JSONObject saveCustomVersionDefinition(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("versionVersion") String versionVersion,
			@RequestParam("customVersionDefinition") String customVersionDefinition) {
		try {
			// ???????????????BaseUrl
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/repository_versions/1
			// PUT
			// {"operating_systems":[{"OperatingSystems":{"os_type":"redhat7","ambari_managed_repositories":true},"repositories":[{"Repositories":
			// {"base_url":"http://10.1.3.24/Seabox-SDP-3.1.5/","repo_id":"HDP-3.1","repo_name":"HDP","components":null,"tags":[],"distribution":null,
			// "applicable_services":[]}},{"Repositories":{"base_url":"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/","repo_id":"HDP-3.1-GPL","repo_name":"HDP-GPL",
			// "components":null,"tags":["GPL"],"distribution":null,"applicable_services":[]}},{"Repositories":{"base_url":"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/",
			// "repo_id":"HDP-UTILS-1.1.0.22","repo_name":"HDP-UTILS","components":null,"tags":[],"distribution":null,"applicable_services":[]}}]}]}

			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/repository_versions/1
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/repository_versions/"
					+ versionVersion;
			JSONObject getResult = instance.getAmbariApi(path, "PUT",
					customVersionDefinition, null, false);
			return getResult.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	// TODO ????????????????????????????????????????????????????????????
	@PutMapping("/putOsRepositoriesData")
	JSONObject putOsRepositoriesData(@RequestParam("masterIp") String masterIp,
			@RequestParam("repoJson") String repoJson) {
		// 0.0 PUT ??????????????????????????????
		// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/operating_systems/redhat7/repositories/HDP-3.1
		// PUT
		// {"Repositories":{"base_url":"http://10.1.3.24/Seabox-SDP-3.1.5/","repo_name":"HDP","verify_base_url":true}}
		// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/operating_systems/redhat7/repositories/HDP-3.1-GPL
		// PUT
		// {"Repositories":{"base_url":"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/","repo_name":"HDP-GPL","verify_base_url":true}}
		// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/operating_systems/redhat7/repositories/HDP-UTILS-1.1.0.22
		// PUT
		// {"Repositories":{"base_url":"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/","repo_name":"HDP-UTILS","verify_base_url":true}}

		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			JSONObject repoJsonObject = JSONObject.parseObject(repoJson);

			String hdpUrl = repoJsonObject.getString("HDP");
			String hdpGplUrl = repoJsonObject.getString("HDP-GPL");
			String hdpUtilsUrl = repoJsonObject.getString("HDP-UTILS");

			String path = "/api/v1/stacks/HDP/versions/3.1/operating_systems/redhat7/repositories/%s";
			// String reqBody =
			// "{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-3.1.5/\",\"repo_name\":\"HDP\",\"verify_base_url\":true}}";
			JSONObject hdp3_1Result = instance.getAmbariApi(
					String.format(path, "HDP-3.1"), "PUT", hdpUrl, null, false);
			log.info("hdp3_1Result: {}", hdp3_1Result);

			// reqBody =
			// "{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_name\":\"HDP-GPL\",\"verify_base_url\":true}}";
			JSONObject hdp3_1_GplResult = instance.getAmbariApi(
					String.format(path, "HDP-3.1-GPL"), "PUT", hdpGplUrl, null,
					false);
			log.info("hdp3_1_GplResult: {}", hdp3_1_GplResult);

			// reqBody =
			// "{\"Repositories\":{\"base_url\":\"http://10.1.3.24/Seabox-SDP-UTIL-1.1.0.22/\",\"repo_name\":\"HDP-UTILS\",\"verify_base_url\":true}}";
			JSONObject hdpUtils = instance.getAmbariApi(
					String.format(path, "HDP-UTILS-1.1.0.22"), "PUT",
					hdpUtilsUrl, null, false);
			log.info("hdpUtils: {}", hdpUtils);

			JSONObject resultJson = new JSONObject();
			resultJson.put("result", true);
			return resultJson;
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/postClusterCurrentStatus")
	JSONObject postClusterCurrentStatus(
			@RequestParam("masterIp") String masterIp,
			@RequestBody String clusterCurrentStatus) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);

			// instance.getHeaders().set("X-Requested-By", "ambari");
			// instance.getHeaders().set("Accept",
			// "application/json, text/plain, */*");
			instance.getHeaders().set("Content-Type", "text/plain");

			String path = "/api/v1/persist";
			String reqJson = clusterCurrentStatus;
			/*
			 * if (!JSONUtil.isJson(clusterCurrentStatus)) { JSONObject
			 * clusterCurrentStatusJson = new JSONObject()
			 * .fluentPut("CLUSTER_CURRENT_STATUS", clusterCurrentStatus);
			 * reqJson = clusterCurrentStatusJson.toJSONString(); }
			 */

			// reqJson = "{\"admin-settings-show-bg-admin\":\"true\"}";
			JSONObject result = instance.getAmbariApi(path, "POST", reqJson,
					null, false);
			log.info("result: {}", result);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	String getHostAddr() throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		System.out.println("Local HostAddress: " + addr.getHostAddress());
		// String hostname = addr.getHostName();
		return addr.getHostAddress();
	}

	@PostMapping("/saveBootstrap")
	JSONObject saveBootstrap(String masterIp,
			@RequestBody List<SdpsClusterHost> hostList) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/bootstrap";

			String masterName = "";
			String masterPasswd = "";
			List<String> domainNames = new ArrayList<>();
			for (SdpsClusterHost host : hostList) {
				domainNames.add(host.getDomainName());
				if ("master".equals(host.getDomainName())) {
					masterName = host.getName();
					masterPasswd = host.getPasswd();
				}
			}
			JSONObject execCommandResult = GanymedUtil.ganymedExecCommand(
					masterIp, 22, masterName, masterPasswd,
					"cat /root/.ssh/id_rsa");
			if (!execCommandResult.getBoolean("result")) {
				return execCommandResult;
			}
			String id_rsa = execCommandResult.getString("data");

			JSONObject reqJson = new JSONObject();
			reqJson.put("verbose", true);
			reqJson.put("user", "root");
			reqJson.put("userRunAs", "root");
			reqJson.put("sshPort", "22");
			reqJson.put("sshKey", id_rsa.trim());
			log.info("reqJson: {}", reqJson);
			reqJson.put("hosts", domainNames);
			// String reqBody =
			// "{\"verbose\":true,\"sshKey\":\"%s\",\"hosts\":[\"master\",\"node1\",\"node2\"],\"user\":\"root\",\"sshPort\":\"22\",\"userRunAs\":\"root\"}";
			String reqBody = JSONObject.toJSONString(reqJson);
			JSONObject result = instance.getAmbariApi(path, "POST", reqBody,
					null, false);
			log.info("result: {}", result);
			result.put("sshRsa", id_rsa);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/saveRequests")
	JSONObject saveRequests(String masterIp) {
		try {
			JSONObject jsonObject = new JSONObject().fluentPut("result", true);
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/requests";
			String reqBody = "{\"RequestInfo\":{\"action\":\"check_host\",\"context\":\"Check host\",\"parameters\":{\"check_execute_list\":\"host_resolution_check\",\"jdk_location\":\"http://master:8080/resources\",\"threshold\":\"20\",\"hosts\":\"master,node1,node2\"}},\"Requests/resource_filters\":[{\"hosts\":\"master,node1,node2\"}]}";
			JSONObject result = instance.getAmbariApi(path, "POST", reqBody,
					null, false);
			log.info("result: {}", result);
			jsonObject.put("resultOne", result);
			reqBody = "{\"RequestInfo\":{\"action\":\"check_host\",\"context\":\"Check hosts\",\"parameters\":{\"threshold\":\"60\",\"java_home\":\"/usr/lib/jdk\",\"jdk_location\":\"http://master:8080/resources\",\"check_execute_list\":\"java_home_check\"}},\"Requests/resource_filters\":[{\"hosts\":\"master,node1,node2\"}]}";
			result = instance.getAmbariApi(path, "POST", reqBody, null, false);
			log.info("result: {}", result);
			jsonObject.put("resultSecond", result);

			/*
			 * // ???????????????????????????????????????????????????????????????????????? // 1.
			 * http://10.1.3.11:8080/api/v1/requests
			 * /1?fields=Requests/inputs,Requests
			 * /request_status,tasks/Tasks/host_name
			 * ,tasks/Tasks/structured_out/host_resolution_check
			 * /hosts_with_failures
			 * ,tasks/Tasks/structured_out/host_resolution_check
			 * /failed_count,tasks
			 * /Tasks/structured_out/installed_packages,tasks/
			 * Tasks/structured_out
			 * /last_agent_env_check,tasks/Tasks/structured_out
			 * /transparentHugePage
			 * ,tasks/Tasks/stdout,tasks/Tasks/stderr,tasks/Tasks
			 * /error_log,tasks
			 * /Tasks/command_detail,tasks/Tasks/status&minimal_response
			 * =true&_=1641958787622 // 2.
			 * http://10.1.3.11:8080/api/v1/requests/
			 * 2?fields=*,tasks/Tasks/host_name
			 * ,tasks/Tasks/status,tasks/Tasks/structured_out&_=1641958787621
			 * reqBody =
			 * "{\"RequestInfo\":{\"action\":\"check_host\",\"context\":\"Check host\",\"parameters\":{\"check_execute_list\":\"last_agent_env_check,installed_packages,existing_repos,transparentHugePage\",\"jdk_location\":\"http://master:8080/resources\",\"threshold\":\"20\"}},\"Requests/resource_filters\":[{\"hosts\":\"master,node1,node2\"}]}"
			 * ; result = instance.getAmbariApi(path, "POST", reqBody, null,
			 * false);
			 */
			log.info("result: {}", result);
			// resp: { "href" : "http://10.1.3.11:8080/api/v1/requests/3",
			// "Requests" : { "id" : 3, "status" : "Accepted" } }
			return jsonObject;
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/saveRequestsThird")
	JSONObject saveRequestsThird(String masterIp) {
		try {
			// ????????????????????????????????????????????????????????????????????????
			// 1.
			// http://10.1.3.11:8080/api/v1/requests/1?fields=Requests/inputs,Requests/request_status,tasks/Tasks/host_name,tasks/Tasks/structured_out/host_resolution_check/hosts_with_failures,tasks/Tasks/structured_out/host_resolution_check/failed_count,tasks/Tasks/structured_out/installed_packages,tasks/Tasks/structured_out/last_agent_env_check,tasks/Tasks/structured_out/transparentHugePage,tasks/Tasks/stdout,tasks/Tasks/stderr,tasks/Tasks/error_log,tasks/Tasks/command_detail,tasks/Tasks/status&minimal_response=true&_=1641958787622
			// 2.
			// http://10.1.3.11:8080/api/v1/requests/2?fields=*,tasks/Tasks/host_name,tasks/Tasks/status,tasks/Tasks/structured_out&_=1641958787621
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/requests";
			String reqBody = "{\"RequestInfo\":{\"action\":\"check_host\",\"context\":\"Check host\",\"parameters\":{\"check_execute_list\":\"last_agent_env_check,installed_packages,existing_repos,transparentHugePage\",\"jdk_location\":\"http://master:8080/resources\",\"threshold\":\"20\"}},\"Requests/resource_filters\":[{\"hosts\":\"master,node1,node2\"}]}";
			JSONObject result = instance.getAmbariApi(path, "POST", reqBody,
					null, false);
			log.info("result: {}", result);
			// resp: { "href" : "http://10.1.3.11:8080/api/v1/requests/3",
			// "Requests" : { "id" : 3, "status" : "Accepted" } }
			return new JSONObject().fluentPut("result", true).fluentPut(
					"resultThird", result);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/saveRecommendations")
	JSONObject saveRecommendations(String masterIp, @RequestBody String reqBody) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/recommendations";
			JSONObject result = instance.getAmbariApi(path, "POST", reqBody,
					null, false);
			log.info("result: {}", result);
			// resp: { "href" : "http://10.1.3.11:8080/api/v1/requests/3",
			// "Requests" : { "id" : 3, "status" : "Accepted" } }
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	@PostMapping("/saveValidations")
	JSONObject saveValidations(String masterIp, @RequestBody String reqBody) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/validations";
			JSONObject result = instance.getAmbariApi(path, "POST", reqBody,
					null, false);
			log.info("result: {}", result);
			// resp: { "href" : "http://10.1.3.11:8080/api/v1/requests/3",
			// "Requests" : { "id" : 3, "status" : "Accepted" } }
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false);
		}
	}

	// ?????????????????? ?????????namenode datanode???
	@PostMapping("/saveClusterComponentNodes")
	JSONObject saveClusterComponentNodes(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("serviceName") String serviceName,
			@RequestBody String componentNodes) {
		try {
			// 3. ????????????????????????
			// http://10.1.3.11:8080/api/v1/clusters/AAQQ/services?ServiceInfo/service_name=HDFS
			// POST
			// ServiceInfo/service_name=HDFS
			// {"components":[{"ServiceComponentInfo":{"component_name":"DATANODE"}},{"ServiceComponentInfo":{"component_name":"HDFS_CLIENT"}},
			// {"ServiceComponentInfo":{"component_name":"JOURNALNODE"}},{"ServiceComponentInfo":{"component_name":"NAMENODE"}},
			// {"ServiceComponentInfo":{"component_name":"NFS_GATEWAY"}},{"ServiceComponentInfo":{"component_name":"SECONDARY_NAMENODE"}},
			// {"ServiceComponentInfo":{"component_name":"ZKFC"}}]}
			//
			// ZOOKEEPER_SERVER
			// {"components":[{"ServiceComponentInfo":{"component_name":"ZOOKEEPER_CLIENT"}},{"ServiceComponentInfo":{"component_name":"ZOOKEEPER_SERVER"}}]}

			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters/" + clusterName
					+ "/services?ServiceInfo/service_name=" + serviceName;
			JSONObject result = instance.getAmbariApi(path, "POST",
					componentNodes, null, false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ?????????????????? ?????????namenode datanode???
	@PutMapping("/saveClusterComponentNodeState_old")
	JSONObject saveClusterComponentNodeStatus(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("state") String state) {
		try {
			// 3. ????????????????????????
			// http://10.1.3.11:8080/api/v1/clusters/PPPP/services?ServiceInfo/state=INIT
			// PUT
			// ServiceInfo/state: INIT
			// {"RequestInfo":{"context":"Install Services","operation_level":{"level":"CLUSTER","cluster_name":"PPPP"}},"Body":{"ServiceInfo":{"state":"INSTALLED"}}}
			// back:
			// {"href":"http://10.1.3.11:8080/api/v1/clusters/PPPP/requests/4","Requests":{"id":4,"status":"Accepted"}}

			// Connection:keep-alive
			// Accept:application/json, text/javascript, */*; q=0.01
			// X-Requested-With:XMLHttpRequest
			// X-Requested-By:X-Requested-By
			// User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64)
			// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45
			// Safari/537.36
			// Content-Type:text/plain
			// Origin:http://10.1.3.11:8080
			// Referer:http://10.1.3.11:8080/
			// Accept-Language:zh-CN,zh;q=0.9
			// Cookie:AMBARISESSIONID=node01fvtps62gn7lgxdxon1toilnx2.node0
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			instance.getHeaders().set("Accept",
					"application/json, text/javascript, */*; q=0.01");
			instance.getHeaders().set("Content-Type", "text/plain");

			String path;
			String reqBody;
			if ("INIT".equals(state)) {
				// /api/v1/clusters/testCluster/services?ServiceInfo/state=INIT
				path = "/api/v1/clusters/" + clusterName
						+ "/services?ServiceInfo/state=INIT";
				// {"RequestInfo":{"context":"Install Services","operation_level":{"level":"CLUSTER","cluster_name":"testCluster"}},"Body":{"ServiceInfo":{"state":"INSTALLED"}}}
				reqBody = "{\"RequestInfo\":{\"context\":\"Install Services\",\"operation_level\":{\"level\":\"CLUSTER\",\"cluster_name\":\""
						+ clusterName
						+ "\"}},\"Body\":{\"ServiceInfo\":{\"state\":\"INSTALLED\"}}}";
			} else if ("INSTALLED".equals(state)) {
				path = "/api/v1/clusters/"
						+ clusterName
						+ "/services?ServiceInfo/state=INSTALLED&params/run_smoke_test=true&params/reconfigure_client=false";
				reqBody = "{\"RequestInfo\":{\"context\":\"Start Services\",\"operation_level\":{\"level\":\"CLUSTER\",\"cluster_name\":\""
						+ clusterName
						+ "\"}},\"Body\":{\"ServiceInfo\":{\"state\":\"STARTED\"}}}";
			} else {
				return new JSONObject().fluentPut("result", false).fluentPut(
						"msg", "??????????????????");
			}

			JSONObject result = instance.getAmbariApi(path, "PUT", reqBody,
					null, false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ?????????????????? ?????????namenode datanode???
	@PutMapping("/saveClusterComponentNodeStatusNewZk")
	JSONObject saveClusterComponentNodeStatusNewZk(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("state") String state) {
		try {
			AmbariUtil instanceUtil = AmbariUtil.getInstance();
			instanceUtil.setUrl("http://" + masterIp + ":8080");
			instanceUtil.setClusterName(clusterName);

			String plainCreds = "admin" + ":" + "admin";
			byte[] plainCredsBytes = plainCreds.getBytes();
			byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
			String base64Creds = new String(base64CredsBytes);
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instanceUtil.setHeaders(headers);

			String path = "/api/v1/clusters/" + clusterName + "/services/"
					+ "ZOOKEEPER";
			String reqBody = "{\"ServiceInfo\": {\"state\": \"STARTED\"}}";

			JSONObject result = instanceUtil.getAmbariApi(path, "PUT", reqBody,
					null, false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ?????????????????? ?????????namenode datanode???
	@PutMapping("/saveClusterComponentNodeState")
	JSONObject saveClusterComponentNodeStatusNew(
			@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("state") String state) {
		try {
			AmbariUtil instanceUtil = AmbariUtil.getInstance();
			instanceUtil.setUrl("http://" + masterIp + ":8080");
			instanceUtil.setClusterName(clusterName);

			String plainCreds = "admin" + ":" + "admin";
			byte[] plainCredsBytes = plainCreds.getBytes();
			byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
			String base64Creds = new String(base64CredsBytes);
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Requested-By", "ambari");
			instanceUtil.setHeaders(headers);

			// http://10.1.3.11:8080/api/v1/clusters/testCluster/services?ServiceInfo/state=INSTALLED&params/run_smoke_test=true&params/reconfigure_client=false
			String path;
			String reqBody;
			if ("INIT".equals(state)) {
				// /api/v1/clusters/testCluster/services?ServiceInfo/state=INIT
				// /api/v1/clusters/testCluster/services?ServiceInfo/state=INIT
				// {"RequestInfo":{"context":"Install Services","operation_level":{"level":"CLUSTER","cluster_name":"testCluster"}},"Body":{"ServiceInfo":{"state":"INSTALLED"}}}
				path = "/api/v1/clusters/" + clusterName
						+ "/services?ServiceInfo/state=INIT";
				// {"RequestInfo":{"context":"Install Services","operation_level":{"level":"CLUSTER","cluster_name":"testCluster"}},"Body":{"ServiceInfo":{"state":"INSTALLED"}}}
				reqBody = "{\"RequestInfo\":{\"context\":\"Install Services\",\"operation_level\":{\"level\":\"CLUSTER\",\"cluster_name\":\""
						+ clusterName
						+ "\"}},\"Body\":{\"ServiceInfo\":{\"state\":\"INSTALLED\"}}}";
			} else if ("INSTALLED".equals(state)) {
				path = "/api/v1/clusters/"
						+ clusterName
						+ "/services?ServiceInfo/state=INSTALLED&params/run_smoke_test=true&params/reconfigure_client=false";
				reqBody = "{\"RequestInfo\":{\"context\":\"Start Services\",\"operation_level\":{\"level\":\"CLUSTER\",\"cluster_name\":\""
						+ clusterName
						+ "\"}},\"Body\":{\"ServiceInfo\":{\"state\":\"STARTED\"}}}";
			} else {
				return new JSONObject().fluentPut("result", false).fluentPut(
						"msg", "??????????????????");
			}

			JSONObject result = instanceUtil.getAmbariApi(path, "PUT", reqBody,
					null, false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/getBootStrapStatus")
	JSONObject getBootStrapStatus(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/bootstrap/" + requestId + "?_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/getClusterCurrentStatus")
	JSONObject getClusterCurrentStatus(@RequestParam("masterIp") String masterIp) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/persist/CLUSTER_CURRENT_STATUS?_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/getOneRequest")
	JSONObject getFirstRequest(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId) {
		try {
			// { "id": 1, "request_status": "COMPLETED" }
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/requests/"
					+ requestId
					+ "?fields=Requests/inputs,Requests/request_status,tasks/Tasks/host_name,tasks/Tasks/structured_out/host_resolution_check/hosts_with_failures,tasks/Tasks/structured_out/host_resolution_check/failed_count,tasks/Tasks/structured_out/installed_packages,tasks/Tasks/structured_out/last_agent_env_check,tasks/Tasks/structured_out/transparentHugePage,tasks/Tasks/stdout,tasks/Tasks/stderr,tasks/Tasks/error_log,tasks/Tasks/command_detail,tasks/Tasks/status&minimal_response=true"
					+ "&_=" + System.currentTimeMillis();
			;
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/getSecondRequest")
	JSONObject getSecondRequest(@RequestParam("masterIp") String masterIp,
			@RequestParam("requestId") String requestId) {
		try {
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/requests/"
					+ requestId
					+ "?fields=*,tasks/Tasks/host_name,tasks/Tasks/status,tasks/Tasks/structured_out"
					+ "&_=" + System.currentTimeMillis();
			;
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/hostsfieldsHostshost_status")
	JSONObject hostsfieldsHostshost_status(
			@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/host_status&_=1641966844074
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/hosts?fields=Hosts/host_status&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/fieldsconfigurations31")
	JSONObject fieldsconfigurations31(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1?fields=configurations/*,Versions/config_types/*&_=1641966844665
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1?fields=configurations/*,Versions/config_types/*&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/servicesStackServices")
	JSONObject servicesStackServices(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&fields=configurations/*,configurations/dependencies/*,StackServices/config_types/*&_=1641966844666
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/services"
					+ "?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&fields=configurations/*,configurations/dependencies/*,StackServices/config_types/*&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/servicesthemes")
	JSONObject servicesthemes(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&themes/ThemeInfo/default=true&fields=themes/*&_=1641966844667
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/services"
					+ "?StackServices/service_name.in(HDFS,YARN,MAPREDUCE2,ZOOKEEPER,AMBARI_METRICS,SMARTSENSE)&themes/ThemeInfo/default=true&fields=themes/*&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/hostsfieldsHoststotal_mem")
	JSONObject hostsfieldsHoststotal_mem(
			@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?fields=Hosts/total_mem,Hosts/cpu_count,Hosts/disk_info,Hosts/last_agent_env,Hosts/host_name,Hosts/os_type,Hosts/os_arch,Hosts/os_family,Hosts/ip&_=1641966844657
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?fields=StackServices/*,components/*,components/dependencies/Dependencies/scope,components/dependencies/Dependencies/service_name,artifacts/Artifacts/artifact_name&_=1641966844660
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/hosts?fields=Hosts/total_mem,Hosts/cpu_count,Hosts/disk_info,Hosts/last_agent_env,Hosts/host_name,Hosts/os_type,Hosts/os_arch,Hosts/os_family,Hosts/ip&_="
					+ System.currentTimeMillis();
			;
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/servicesfieldsStackServices")
	JSONObject servicesfieldsStackServices(
			@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1/services?fields=StackServices/*,components/*,components/dependencies/Dependencies/scope,components/dependencies/Dependencies/service_name,artifacts/Artifacts/artifact_name&_=1641966844660
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1/services?fields=StackServices/*,components/*,components/dependencies/Dependencies/scope,components/dependencies/Dependencies/service_name,artifacts/Artifacts/artifact_name&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/hostsHostshost_name")
	JSONObject hostsHostshost_name(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/hosts"
					+ "?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	// ================================================= ????????? GET
	// http://10.1.3.11:8080/api/v1/stacks?_=1641980652149
	// http://10.1.3.11:8080/api/v1/services/AMBARI/components/AMBARI_SERVER?_=1641980652150
	// http://10.1.3.11:8080/api/v1/version_definitions?fields=VersionDefinition/stack_default,VersionDefinition/stack_repo_update_link_exists,VersionDefinition/max_jdk,VersionDefinition/min_jdk,operating_systems/repositories/Repositories/*,operating_systems/OperatingSystems/*,VersionDefinition/stack_services,VersionDefinition/repository_version&VersionDefinition/show_available=true&VersionDefinition/stack_name=HDP&_=1641980652151
	// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.1?fields=operating_systems/repositories/Repositories&_=1641980652152
	// http://10.1.3.11:8080/api/v1/stacks/HDP/versions/3.0?fields=operating_systems/repositories/Repositories&_=1641980652152
	// http://10.1.3.11:8080/api/v1/clusters?fields=Clusters/provisioning_state,Clusters/security_type,Clusters/version,Clusters/cluster_id&_=1641980652154

	@GetMapping("/stackOne")
	JSONObject stackOne(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks?_=" + System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/AMBARI_SERVER")
	JSONObject AMBARI_SERVER(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/services/AMBARI/components/AMBARI_SERVER?_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/version_definitionsfieldsVersionDefinition")
	JSONObject version_definitionsfieldsVersionDefinition(
			@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/version_definitions?fields=VersionDefinition/stack_default,VersionDefinition/stack_repo_update_link_exists,VersionDefinition/max_jdk,VersionDefinition/min_jdk,operating_systems/repositories/Repositories/*,operating_systems/OperatingSystems/*,VersionDefinition/stack_services,VersionDefinition/repository_version&VersionDefinition/show_available=true&VersionDefinition/stack_name=HDP&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/stacksHDPversions31")
	JSONObject stacksHDPversions31(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.1?fields=operating_systems/repositories/Repositories&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/stacksHDPversions30")
	JSONObject stacksHDPversions30(@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/stacks/HDP/versions/3.0?fields=operating_systems/repositories/Repositories&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/clustersfieldsClustersprovisioning_state")
	JSONObject clustersfieldsClustersprovisioning_state(
			@RequestParam("masterIp") String masterIp) {
		try {
			// http://10.1.3.11:8080/api/v1/hosts?Hosts/host_name.in(master,node1,node2)&fields=Hosts/cpu_count,Hosts/disk_info,Hosts/total_mem,Hosts/ip,Hosts/os_type,Hosts/os_arch,Hosts/public_host_name&minimal_response=true&_=1641966844662
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters?fields=Clusters/provisioning_state,Clusters/security_type,Clusters/version,Clusters/cluster_id&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject();
		}
	}

	@GetMapping("/getRequestStatus")
	JSONObject getRequestStatus(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("requestId") String requestId) {
		try {
			// http://10.1.3.11:8080/api/v1/clusters/testCluster/requests/4?fields=tasks/Tasks/command,tasks/Tasks/command_detail,tasks/Tasks/ops_display_name,tasks/Tasks/exit_code,tasks/Tasks/start_time,tasks/Tasks/end_time,tasks/Tasks/host_name,tasks/Tasks/id,tasks/Tasks/role,tasks/Tasks/status&minimal_response=true&_=1642049106982
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters/"
					+ clusterName
					+ "/requests/"
					+ requestId
					+ "?fields=tasks/Tasks/command,tasks/Tasks/command_detail,tasks/Tasks/ops_display_name,tasks/Tasks/exit_code,tasks/Tasks/start_time,tasks/Tasks/end_time,tasks/Tasks/host_name,tasks/Tasks/id,tasks/Tasks/role,tasks/Tasks/status&minimal_response=true&_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????????????????", e);
			return new JSONObject().fluentPut("result", false).fluentPut(
					"excption", e.toString());
		}
	}

	@GetMapping("/getRequestTaskResult")
	JSONObject getRequestTaskResult(@RequestParam("masterIp") String masterIp,
			@RequestParam("clusterName") String clusterName,
			@RequestParam("requestId") Long requestId,
			@RequestParam("taskId") Long taskId) {
		try {
			// http://10.1.3.24:9999/api/v1/clusters/seabox4/requests/913/tasks/4964?_=1642755530984
			AmbariUtil instance = ambariInstanceHttp(masterIp);
			String path = "/api/v1/clusters/" + clusterName + "/requests/"
					+ requestId + "/tasks/" + taskId + "?_="
					+ System.currentTimeMillis();
			JSONObject result = instance.getAmbariApi(path, "GET", null, null,
					false);
			return result.fluentPut("result", true);
		} catch (Exception e) {
			log.error("????????????????????????", e);
			return new JSONObject().fluentPut("result", false).fluentPut(
					"excption", e.toString());
		}
	}

	@PostMapping("/addAmbariUser")
	public Result addAmbariUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = getClusterNameParam(ambariUtil);
			ambariUtil.getHeaders().set("Content-Type", "text/plain");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "createUser");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), ambariUser
					.getRequestCreateUserBody().toJSONString(), false);
			args = bigdataCommonFegin.getGlobalParam("ambari",
					"createUserPrivileges");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), ambariUser
					.getRequestCreatePrivilegesBody().toString(), param, false);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("??????ambari????????????", e);
			return Result.failed("????????????");
		}
	}

	@DeleteMapping("/deleteAmbariUser")
	public Result deleteAmbariUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("username") String username) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = MapUtil.newHashMap(1);
			param.put(1, username);
			ambariUtil.getHeaders().set("Content-Type", "text/plain");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "deleteUser");
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), param);
			return Result.succeed("????????????");
		} catch (Exception e) {
			log.error("??????ambari????????????", e);
			return Result.failed("????????????");
		}
	}

	@PutMapping("/updateAmbariUserPassword")
	public Result updateAmbariUserPassword(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			Map<Integer, Object> param = MapUtil.newHashMap(1);
			param.put(1, ambariUser.getUser_name());
			ambariUtil.getHeaders().set("Content-Type", "text/plain");
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "updateUserPassword");
			ambariUser.setOld_password(ambariUtil.getPassword());
			ambariUtil.getAmbariApi(args.getData().getArgValue(), args
					.getData().getArgValueDesc(), ambariUser
					.getRequestUpdatePasswdBody().toJSONString(), false, param);
			return Result.succeed("??????????????????");
		} catch (Exception e) {
			log.error("??????????????????", e);
			return Result.failed("??????????????????");
		}
	}

	/**
	 * ??????ambari????????????
	 *
	 * @return
	 */
	@GetMapping("/getAmbariUsers")
	public Result<JSONArray> getAmbariUsers(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "getAmbariUsers");
			JSONObject result = ambariUtil.getAmbariApi(args.getData()
					.getArgValue(), args.getData().getArgValueDesc());
			return Result.succeed(AmbariUtil.analysisAmbariUsersInfo(result),
					"????????????");
		} catch (Exception e) {
			log.error("??????ambari??????????????????", e);
			return Result.failed(new JSONArray(), "????????????");
		}
	}

	/**
	 * ??????keytab??????
	 * 
	 * @param clusterIdList
	 *            ??????id??????
	 * @param username
	 *            ??????
	 * @return
	 */
	@GetMapping("/downloadKeytab")
	public Result downloadKeytab(
			@RequestParam("clusterId") List<Integer> clusterIdList,
			@RequestParam("username") String username,
			@RequestParam("isServer") Boolean isServer) {
		try {
			KeytabUtil.checkKeytabExist(clusterIdList, username,
					"SeaboxInnerRestTemplate", isServer);
			return Result.succeed("??????");
		} catch (Exception e) {
			log.error("??????keytab??????", e);
			return Result.failed("????????????");
		}
	}

	/**
	 * ??????ambari???kerberos??????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @return
	 */
	@GetMapping("/getServerKeytabs")
	public Result getServerKeytabs(@RequestParam("clusterId") Integer clusterId) {
		try {
			Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
					"ambari", "downloadKeytab");
			SysGlobalArgs sysGlobalArgs = args.getData();
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			return Result.succeed(ambariUtil.getKeytabs(sysGlobalArgs));
		} catch (Exception e) {
			log.error("??????keytab????????????", e);
			return Result.failed("??????keytab????????????");
		}
	}

}
