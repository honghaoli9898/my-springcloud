package com.seaboxdata.sdps.bigdataProxy.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsTaskConfigMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsYarnInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerGroupUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXGroups;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.request.ApplicationRequest;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;
import com.seaboxdata.sdps.common.framework.bean.yarn.SdpsYarnInfo;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;

import feign.Response;

@Slf4j
@RestController
@RequestMapping("/bigdataCommon")
public class BigdataCommonController {

	@Autowired
	CommonBigData commonBigData;

	@Autowired
	SdpsClusterMapper sdpsClusterMapper;

	@Autowired
	SdpsYarnInfoMapper sdpsYarnInfoMapper;

	@Autowired
	SdpsTaskConfigMapper sdpsTaskConfigMapper;

	@Autowired
	SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	SdpsClusterMapper clusterMapper;

	@GetMapping("/selectClusterList")
	public Result selectClusterList() {
		try {
			QueryWrapper<SdpsCluster> clusterQueryWrapper = new QueryWrapper<SdpsCluster>()
					.select("cluster_id", "cluster_name", "cluster_show_name",
							"server_id", "cluster_source", "cluster_host_conf");
			// 查询在使用且状态为正常的集群
			clusterQueryWrapper.eq("is_use", 1).eq("cluster_status_id", 2)
					.eq("is_running", true);
			List<SdpsCluster> sdpsClusters = sdpsClusterMapper
					.selectList(clusterQueryWrapper);
			return Result.succeed(sdpsClusters, "查询集群列表信息成功");
		} catch (Exception e) {
			return Result.failed("查询集群列表信息失败", e.getMessage());
		}
	}

	@GetMapping("/getTaskConfByClusterTypeAndTaskType")
	public List<TaskConfig> getTaskConfByClusterTypeAndTaskType(
			@RequestParam("taskType") String taskType,
			@RequestParam("cluster_type") String cluster_type) {
		List<TaskConfig> taskConfigList = sdpsTaskConfigMapper
				.queryTaskConfByClusterTypeAndTaskType(taskType, cluster_type);
		return taskConfigList;
	}

	@GetMapping("/makeHdfsPath")
	public Result makeHdfsPath(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			Boolean result = commonBigData.makeHdfsPath(sysUser.getUsername(),
					clusterId, hdfsPath);
			if (result) {
				return Result.succeed(result, "创建HDFS目录成功");
			} else {
				return Result.failed("创建HDFS目录失败");
			}
		} catch (Exception e) {
			return Result.failed("创建HDFS目录失败", e.getMessage());
		}
	}

	/**
	 * 删除文件
	 *
	 * @param clusterId
	 *            集群ID
	 * @param hdfsPaths
	 *            待删除的文件路径
	 * @return 是否删除成功。
	 */
	@DeleteMapping("/deleteFile")
	public Result deleteFile(@LoginUser(isFull = false) SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths,
			@RequestParam("flag") Boolean flag) {
		JSONObject jsonObject = null;
		try {
			Result result = commonBigData.deleteFile(sysUser.getUsername(),
					clusterId, hdfsPaths, flag);
			jsonObject = new JSONObject((Map) result.getData());
			if (jsonObject.getBoolean("success")) {
				return Result.succeed(result, "删除HDFS目录:" + hdfsPaths + "成功");
			} else {
				String message = jsonObject.getString("message");
				if (message.contains("Permission denied")) {
					return Result.failed(
							jsonObject,
							"删除HDFS目录"
									+ hdfsPaths
									+ "失败,用户".concat(sysUser.getUsername())
											.concat("没有权限"));
				}
				return Result.failed(jsonObject, "删除HDFS目录" + hdfsPaths + "失败");
			}
		} catch (Exception e) {
			jsonObject = new JSONObject();
			jsonObject.put("message", e.getMessage());
			return Result.failed(jsonObject, e.getMessage());
		}
	}

	@DeleteMapping("/deleteScriptFile")
	public Result deleteScriptFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths,
			@RequestParam("flag") boolean flag) {
		JSONObject jsonObject = null;
		try {
			Result result = commonBigData.deleteFile(username, clusterId,
					hdfsPaths, flag);
			log.info("获取返回结果:{}", result);
			jsonObject = new JSONObject((Map) result.getData());
			if (jsonObject.getBoolean("success")) {
				return Result.succeed(result.getData(), "删除HDFS目录:" + hdfsPaths
						+ "成功");
			} else {
				String message = jsonObject.getString("message");
				if (message.contains("Permission denied")) {
					return Result.failed(jsonObject, "删除HDFS目录" + hdfsPaths
							+ "失败,用户".concat(username).concat("没有权限"));
				}
				return Result.failed(jsonObject, "删除HDFS目录" + hdfsPaths + "失败");
			}
		} catch (Exception e) {
			jsonObject = new JSONObject();
			jsonObject.put("message", e.getMessage());
			return Result.failed(jsonObject, e.getMessage());
		}
	}

	@DeleteMapping("/cleanHdfsDir")
	public Result cleanHdfsDir(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPathList") ArrayList<String> hdfsPathList) {
		try {
			Boolean bool = commonBigData.cleanHdfsDir(clusterId, hdfsPathList);
			if (bool) {
				return Result.succeed(bool,
						"清理HDFS目录:" + hdfsPathList.toString() + "成功");
			} else {
				return Result.failed("清理HDFS目录" + hdfsPathList.toString()
						+ "失败");
			}
		} catch (Exception e) {
			return Result.failed("清理HDFS目录" + hdfsPathList.toString() + "失败",
					e.getMessage());
		}
	}

	@PostMapping("/createHdfsQNAndSQNAndOwner")
	public Result createHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		try {
			Boolean bool = commonBigData
					.createHdfsQNAndSQNAndOwner(hdfsSetDirObj);
			if (bool) {
				return Result.succeed(bool, "创建HDFS目录并设置目录配额或设置所属者所属组成功");
			} else {
				return Result
						.failed("创建HDFS目录并设置目录配额或设置所属者所属组失败,请检查[空间消耗或文件数限制]");
			}
		} catch (Exception e) {
			return Result.failed("创建HDFS目录并设置目录配额或设置所属者所属组失败", e.getMessage());
		}
	}

	@PostMapping(value = "/updataHdfsQNAndSQNAndOwner")
	public Result updataHdfsQNAndSQNAndOwner(
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		try {
			Result<Boolean> bool = commonBigData
					.updataHdfsQNAndSQNAndOwner(hdfsSetDirObj);
			if (bool.isSuccess()) {
				return Result.succeed(bool, "HDFS目录设置目录配额或设置所属者所属组成功");
			} else {
				return Result.failed(bool.getMsg());
			}
		} catch (Exception e) {
			return Result.failed("HDFS目录设置目录配额或设置所属者所属组失败", e.getMessage());
		}
	}

	@GetMapping("/getHdfsSaveObjList")
	public Result getHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			ArrayList<HdfsDirObj> hdfsDirObjs = commonBigData
					.getHdfsSaveObjList(clusterId, hdfsPath);
			if (hdfsDirObjs != null) {
				return Result.succeed(hdfsDirObjs, "查询hdfs目录信息成功");
			} else {
				return Result.failed("查询hdfs目录异常," + hdfsPath + "为空", "null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failed("查询hdfs目录信息失败", e.getMessage());
		}
	}

	@GetMapping("/selectHdfsSaveObjList")
	public Result selectHdfsSaveObjList(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			Result<List<HdfsFSObj>> hdfsFSObjs = commonBigData
					.selectHdfsSaveObjList(clusterId, hdfsPath);
			if (hdfsFSObjs.isFailed()) {
				return Result.failed("查询hdfs目录及文件信息失败", hdfsFSObjs.getMsg());
			}
			return Result.succeed(hdfsFSObjs.getData(), "查询hdfs目录及文件信息成功");
		} catch (Exception e) {
			log.error("查询目录报错", e);
			return Result.failed("查询hdfs目录及文件信息失败", e.getMessage());
		}
	}

	@GetMapping("/selectHdfsQNAndSQN")
	public Result selectHdfsQNAndSQN(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			HdfsDirObj hdfsDirObj = commonBigData.selectHdfsQNAndSQN(clusterId,
					hdfsPath);
			return Result.succeed(hdfsDirObj, "查询hdfs目录信息成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failed("查询hdfs目录信息失败", e.getMessage());
		}
	}

	@GetMapping("/selectAllQueueTree")
	public Result selectAllQueueTree(Integer clusterId) {
		try {
			Map result = commonBigData.selectAllQueueTree(clusterId);
			return Result.succeed(result, "操作成功");
		} catch (Exception e) {
			return Result.failed("查询大数据队列失败", e.getMessage());
		}
	}

	@GetMapping("/getRangerUserByName")
	public Result getRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName) {
		try {
			VXUsers rangerUserObj = commonBigData.getRangerUserByName(
					clusterId, userName);
			if (rangerUserObj != null) {
				return Result.succeed(rangerUserObj, "查询Ranger用户[" + userName
						+ "]信息成功");
			} else {
				return Result.failed("查询Ranger用户[" + userName + "]信息为null");
			}
		} catch (Exception e) {
			return Result.failed("查询Ranger用户[" + userName + "]信息失败:",
					e.getMessage());
		}
	}

	@PostMapping("/addRangerUser")
	public Result addRangerUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody ArrayList<VXUsers> rangerObjList) {
		try {
			Boolean bool = commonBigData
					.addRangerUser(clusterId, rangerObjList);
			if (bool) {
				return Result.succeed(bool, "Ranger添加用户:"
						+ rangerObjList.stream().map(user -> user.getName())
								.collect(Collectors.toList()) + "成功");
			} else {
				return Result.failed("添加Ranger用户"
						+ rangerObjList.stream().map(user -> user.getName())
								.collect(Collectors.toList()) + "失败");
			}
		} catch (Exception e) {
			return Result.failed(
					"添加Ranger用户"
							+ rangerObjList.stream()
									.map(user -> user.getName())
									.collect(Collectors.toList()) + "失败",
					e.getMessage());
		}
	}

	@DeleteMapping("/deleteRangerUserByName")
	public Result deleteRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("userName") String userName) {
		try {
			Boolean bool = commonBigData.deleteRangerUserByName(clusterId,
					userName);
			if (bool) {
				return Result.succeed(bool, "删除Ranger用户[" + userName + "]成功");
			} else {
				return Result.failed("删除Ranger用户[" + userName + "]失败");
			}
		} catch (Exception e) {
			return Result.failed("删除Ranger用户[" + userName + "]失败",
					e.getMessage());
		}
	}

	@PutMapping("/updateRangerUserByName")
	public Result updateRangerUserByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXUsers rangerUserObj) {
		try {
			Boolean bool = commonBigData.updateRangerUserByName(clusterId,
					rangerUserObj);
			if (bool) {
				return Result.succeed(bool,
						"更新Ranger用户[" + rangerUserObj.getName() + "]成功");
			} else {
				return Result.failed("更新Ranger用户[" + rangerUserObj.toString()
						+ "]失败");
			}
		} catch (Exception e) {
			return Result.failed("更新Ranger用户[" + rangerUserObj.toString()
					+ "]失败", e.getMessage());
		}
	}

	@GetMapping("/getRangerGroupByName")
	public Result getRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		try {
			VXGroups rangerGroupObj = commonBigData.getRangerGroupByName(
					clusterId, groupName);
			if (rangerGroupObj != null) {
				return Result.succeed(rangerGroupObj, "查询Ranger用户组["
						+ groupName + "]成功");
			} else {
				return Result.failed("查询Ranger用户组[" + groupName + "]失败");
			}
		} catch (Exception e) {
			return Result.failed("查询Ranger用户组[" + groupName + "]失败",
					e.getMessage());
		}
	}

	@PostMapping("/addRangerGroup")
	public Result addRangerGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj) {
		try {
			Boolean bool = commonBigData.addRangerGroup(clusterId,
					rangerGroupObj);
			if (bool) {
				return Result.succeed(bool,
						"添加Ranger用户组[" + rangerGroupObj.getName() + "]成功");
			} else {
				return Result.failed("添加Ranger用户组[" + rangerGroupObj.getName()
						+ "]失败");
			}
		} catch (Exception e) {
			return Result.failed("添加Ranger用户组[" + rangerGroupObj.getName()
					+ "]失败", e.getMessage());
		}
	}

	@DeleteMapping("/deleteRangerGroupByName")
	public Result deleteRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		try {
			Boolean bool = commonBigData.deleteRangerGroupByName(clusterId,
					groupName);
			if (bool) {
				return Result.succeed(bool, "删除Ranger用户组[" + groupName + "]成功");
			} else {
				return Result.failed("删除Ranger用户组[" + groupName + "]失败");
			}
		} catch (Exception e) {
			return Result.failed("删除Ranger用户组[" + groupName + "]失败",
					e.getMessage());
		}
	}

	@PutMapping("/updateRangerGroupByName")
	public Result updateRangerGroupByName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody VXGroups rangerGroupObj) {
		try {
			Boolean bool = commonBigData.updateRangerGroupByName(clusterId,
					rangerGroupObj);
			if (bool) {
				return Result.succeed(bool,
						"修改Ranger用户组[" + rangerGroupObj.getName() + "]成功");
			} else {
				return Result.failed("修改Ranger用户组[" + rangerGroupObj.getName()
						+ "]失败");
			}
		} catch (Exception e) {
			return Result.failed("修改Ranger用户组[" + rangerGroupObj.getName()
					+ "]失败", e.getMessage());
		}
	}

	@GetMapping("/getUsersByGroupName")
	public Result getUsersByGroupName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName) {
		try {
			RangerGroupUser rangerGroupUser = commonBigData
					.getUsersByGroupName(clusterId, groupName);
			if (rangerGroupUser != null) {
				return Result.succeed(rangerGroupUser, "查询Ranger用户组["
						+ groupName + "]中的所有用户信息成功");
			} else {
				return Result
						.failed("查询Ranger用户组[" + groupName + "]中的所有用户信息失败");
			}
		} catch (Exception e) {
			return Result.failed("查询Ranger用户组[" + groupName + "]中的所有用户信息失败",
					e.getMessage());
		}
	}

	@PostMapping("/addUsersToGroup")
	public Result addUsersToGroup(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers) {
		try {
			return commonBigData.addUsersToGroup(clusterId, groupName,
					rangerUsers);
		} catch (Exception e) {
			return Result.failed("添加一批Ranger用户到用户组[" + groupName + "]失败",
					e.getMessage());
		}
	}

	@DeleteMapping("/deleteUsersToGroup")
	public Result deleteUsersToGroup(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("groupName") String groupName,
			@RequestParam("rangerUsers") List<String> rangerUsers) {
		try {
			Result bool = commonBigData.deleteUsersToGroup(clusterId,
					groupName, rangerUsers);
			return bool;
		} catch (Exception e) {
			return Result.failed("删除一批Ranger用户从用户组[" + groupName + "]失败",
					e.getMessage());
		}
	}

	/**
	 * 新增Ranger策略
	 * 
	 * @param rangerPolicyObj
	 * @return
	 */
	@PostMapping("/addRangerPolicy")
	public Result addRangerPolicy(@RequestBody RangerPolicyObj rangerPolicyObj) {
		return commonBigData.addRangerPolicy(rangerPolicyObj);
	}

	/**
	 * 查询Ranger策略
	 * 
	 * @param clusterId
	 * @param serviceType
	 * @param policyName
	 * @return
	 */
	@GetMapping("/queryRangerPolicy")
	public Result queryRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		return commonBigData.queryRangerPolicy(clusterId, serviceType,
				policyName);
	}

	/**
	 * 模糊查询Ranger策略
	 * 
	 * @param clusterId
	 * @param serviceType
	 * @param policyName
	 * @return
	 */
	@GetMapping("/likeRangerPolicy")
	public Result likeRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		return commonBigData.likeRangerPolicy(clusterId, serviceType,
				policyName);
	}

	/**
	 * 删除Ranger策略
	 * 
	 * @param clusterId
	 * @param serviceType
	 * @param policyName
	 * @return
	 */
	@DeleteMapping("/deleteRangerPolicy")
	public Result deleteRangerPolicy(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serviceType") String serviceType,
			@RequestParam("policyName") String policyName) {
		return commonBigData.deleteRangerPolicy(clusterId, serviceType,
				policyName);
	}

	/**
	 * 查询集群队列信息列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 集群返回的信息
	 */
	@GetMapping("/listScheduler")
	public Result listScheduler(@RequestParam("clusterId") Integer clusterId) {
		JSONObject jsonObject = commonBigData.listScheduler(clusterId);
		return Result.succeed(jsonObject, "查询队列列表信息成功");
	}

	/**
	 * 查询集群任务列表
	 *
	 * @param request
	 * @return 任务列表
	 */
	@GetMapping("/listApps")
	public Result listApps(ApplicationRequest request) {
		JSONObject jsonObject = commonBigData.listApps(request);
		return Result.succeed(jsonObject, "查询任务列表信息成功");
	}

	/**
	 * 查询集群资源配置信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 集群资源配置信息。
	 */
	@GetMapping("/listMetrics")
	public Result listMetrics(@RequestParam("clusterId") Integer clusterId) {
		JSONObject jsonObject = commonBigData.listMetrics(clusterId);
		return Result.succeed(jsonObject, "查询集群资源配置信息成功");
	}

	/**
	 * 查询集群各个节点信息
	 *
	 * @param clusterId
	 * @return
	 */
	@GetMapping("/listNodes")
	public Result listNodes(@RequestParam("clusterId") Integer clusterId) {
		JSONObject jsonObject = commonBigData.listNodes(clusterId);
		return Result.succeed(jsonObject, "查询集群各个节点信息成功");
	}

	/**
	 * 查看yarn队列资源信息
	 *
	 * @return
	 */
	@GetMapping("/selectYarnListInfo")
	public Result selectYarnListInfo() {
		try {
			QueryWrapper<SdpsYarnInfo> queryWrapper = new QueryWrapper<SdpsYarnInfo>()
					.select("queue_name", "queue_capacity",
							"user_capacity_limit", "scheduler_policy",
							"update_time", "project_name", "is_used");
			List<SdpsYarnInfo> sdpsYarnInfos = sdpsYarnInfoMapper
					.selectList(queryWrapper);
			return Result.succeed(sdpsYarnInfos, "查询yarn资料信息成功.");
		} catch (Exception e) {
			return Result.failed("查询yarn资料信息失败", e.getMessage());
		}
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
	public Result modifyQueue(@RequestParam("clusterId") Integer clusterId,
			@RequestBody JSONObject jsonObject) {
		JSONObject body = commonBigData.modifyQueue(clusterId, jsonObject);
		if (!StringUtils.isEmpty(JSONObject.toJSONString(body))) {
			return Result.succeed(body, "修改队列成功.");
		} else {
			return Result.failed(body, "修改队列失败.");
		}
	}

	/**
	 * 上传文件
	 *
	 * @param clusterId
	 *            集群ID
	 * @param file
	 *            源文件
	 * @param path
	 *            目标路径
	 * @return 是否上传成功
	 */
	@PostMapping("/copyFromLocalFile")
	public Result putFile(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam(value = "isUserFile", required = false) Boolean isUserFile,
			@RequestParam(value = "isCrypto", required = false) boolean isCrypto) {
		if (Objects.isNull(isUserFile)) {
			isUserFile = false;
			isCrypto = false;
		}
		return commonBigData.copyFromLocalFile(sysUser.getUsername(),
				clusterId, file, path, isUserFile, isCrypto);
	}

	@PostMapping("/uploadScripFile")
	public Result uploadScripFile(@RequestParam("username") String username,
			@RequestParam("clusterId") Integer clusterId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("path") String path,
			@RequestParam("isUserFile") boolean isUserFile,
			@RequestParam(value = "isCrypto", required = false) boolean isCrypto) {
		return commonBigData.copyFromLocalFile(username, clusterId, file, path,
				isUserFile, isCrypto);
	}

	/**
	 * 复制HDFS文件
	 *
	 * @param clusterId
	 *            集群ID
	 * @param srcPath
	 *            待复制的文件路径
	 * @param destPath
	 *            目标文件路径
	 * @return 是否复制成功
	 */
	@PostMapping("/copyFileFromHDFS")
	public Result<JSONObject> copyFileFromHDFS(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath) {
		return commonBigData.copyFileFromHDFS(sysUser.getUsername(), clusterId,
				srcPath, destPath);
	}

	/**
	 * 移动HDFS文件
	 *
	 * @param clusterId
	 *            集群ID
	 * @param srcPath
	 *            待移动的文件路径
	 * @param destPath
	 *            目标文件路径
	 * @return 是否移动成功
	 */
	@PostMapping("/moveFileFromHDFS")
	public Result<JSONObject> moveFileFromHDFS(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("srcPath") String srcPath,
			@RequestParam("destPath") String destPath) {
		return commonBigData.moveFileFromHDFS(sysUser.getUsername(), clusterId,
				srcPath, destPath);
	}

	/**
	 * 修改文件权限
	 *
	 * @param clusterId
	 *            集群ID
	 * @param path
	 *            待修改的文件路径
	 * @param permission
	 *            新权限
	 * @return 是否修改成功
	 */
	@GetMapping("/setPermission")
	public Result permission(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path,
			@RequestParam("permission") String permission) {
		try {
			boolean flag = commonBigData
					.permission(clusterId, path, permission);
			if (flag)
				return Result.succeed(flag, "权限修改成功.");
			else
				return Result.failed(flag, "权限修改失败.");
		} catch (Exception e) {
			return Result.failed(e, "系统出现异常.");
		}
	}

	@GetMapping("/getServerConfByConfName")
	public Result<String> getServerConfByConfName(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("serverName") String serverName,
			@RequestParam("confStrs") List<String> confStrs) {
		try {
			return Result.succeed(commonBigData.getServerConfByConfName(
					clusterId, serverName, confStrs), "操作成功");
		} catch (Exception e) {
			log.error("调用失败", e);
		}
		return Result.failed("操作失败");
	}

	/**
	 * 获取YarnApplicationLog的Url地址
	 */
	@GetMapping("/getYarnApplicationLogUrl")
	public Result getYarnApplicationLogUrl(
			@RequestParam("clusterId") Integer clusterId) {
		String result = "";
		try {
			result = commonBigData.getYarnApplicationLogUrl(clusterId);
			return Result.succeed(result);
		} catch (Exception e) {
			log.error("获取YarnApplicationLog的Url地址异常:", e);
			return Result.failed(e, "获取YarnApplicationLog的Url地址异常");
		}
	}

	/**
	 * 文件重命名
	 *
	 * @param clusterId
	 *            集群ID
	 * @param oldPath
	 *            旧文件路径
	 * @param newPath
	 *            新文件路径
	 * @return 是否重命名成功
	 */
	@GetMapping("/rename")
	public Result rename(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("oldPath") String oldPath,
			@RequestParam("newPath") String newPath) {
		try {
			boolean flag = commonBigData.rename(clusterId, oldPath, newPath);
			if (flag)
				return Result.succeed(flag, "文件重命名成功.");
			else
				return Result.failed(flag, "文件重命名失败.");
		} catch (Exception e) {
			return Result.failed(e, "文件重命名异常.");
		}
	}

	/**
	 * hdfs文件下载
	 *
	 * @param clusterId
	 *            集群ID
	 * @param path
	 *            待下载文件路径
	 * @return 是否下载成功
	 */
	@GetMapping("/download")
	public void download(@LoginUser(isFull = false) SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("path") String path) {
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletResponse res = requestAttributes.getResponse();
		Response response = commonBigData.download(sysUser.getUsername(),
				clusterId, path);
		Response.Body body = response.body();
		InputStream inputStream;
		try {
			inputStream = body.asInputStream();

			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					inputStream);
			res.setContentType(response.headers().get("Content-Type")
					.toString());
			res.setHeader("Content-Disposition",
					response.headers().get("Content-Disposition").toString()
							.replace("[", "").replace("]", ""));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					res.getOutputStream());
			int length = 0;
			byte[] temp = new byte[1024 * 10];
			while ((length = bufferedInputStream.read(temp)) != -1) {
				bufferedOutputStream.write(temp, 0, length);
			}
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			bufferedInputStream.close();
			inputStream.close();
		} catch (IOException e) {
			log.error("返回下载流失败", e);
		}
	}

	/**
	 * 实时显示使用的核数和内存
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 集群使用的核数和内存数
	 */
	@GetMapping("/usedMemoryInfo")
	public Result usedMemoryInfo(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("topN") Integer topN,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {
		try {
			List<ApplicationDTO> jsonObject = commonBigData.usedMemoryInfo(
					clusterId, topN, startTime, endTime);
			return Result.succeed(jsonObject, "查询成功.");
		} catch (Exception e) {
			log.error("获取使用的核数和内存失败{}", e);
			return Result.failed(e, "获取使用的核数和内存失败");
		}
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
	public Result listAppsByUser(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user) {
		try {
			JSONObject jsonObject = commonBigData.listAppsByUser(clusterId,
					user);
			return Result.succeed(jsonObject, "请求成功.");
		} catch (Exception e) {
			return Result.failed(e, "请求失败.");
		}
	}

	/**
	 * 根据任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param states
	 *            状态
	 * @return 该任务状态下的任务列表
	 */
	@GetMapping("/listAppsByStates")
	public Result listAppsByStates(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("states") String[] states) {
		try {
			JSONObject jsonObject = commonBigData.listAppsByStates(clusterId,
					states);
			return Result.succeed(jsonObject, "请求成功.");
		} catch (Exception e) {
			return Result.failed(e, "请求失败.");
		}
	}

	/**
	 * 根据用户和任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @param states
	 *            状态
	 * @return 该用户和任务状态下的任务列表
	 */
	@GetMapping("/listAppsByUserAndStates")
	public Result listAppsByUserAndStates(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("user") String user,
			@RequestParam("states") String[] states) {
		try {
			JSONObject jsonObject = commonBigData.listAppsByUserAndStates(
					clusterId, user, states);
			return Result.succeed(jsonObject, "请求成功.");
		} catch (Exception e) {
			return Result.failed(e, "请求失败.");
		}
	}

	/**
	 * 获取yarn queue配置信息
	 */
	@GetMapping("/getYarnQueueConfigurate")
	public Result getYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			JSONObject jsonObject = commonBigData
					.getYarnQueueConfigurate(clusterId);
			return Result.succeed(jsonObject, "操作成功");
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 更新yarn queue配置信息
	 */
	@PostMapping("/upateYarnQueueConfigurate")
	public Result updateYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			return commonBigData.updateYarnQueueConfigurate(clusterId, infos);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 删除yarn queue配置信息
	 */
	@PostMapping("/deleteYarnQueueConfigurate")
	public Result deleteYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			return commonBigData.deleteYarnQueueConfigurate(clusterId, infos);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 新增yarn queue配置信息
	 */
	@PostMapping("/insertYarnQueueConfigurate")
	public Result insertYarnQueueConfigurate(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			return commonBigData.insertYarnQueueConfigurate(clusterId, infos);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 停止或启动yarn queue配置信息
	 */
	@PostMapping("/stopOrRunningYarnQueue")
	public Result stopOrRunningYarnQueue(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody List<YarnQueueConfInfo> infos) {
		try {
			return commonBigData.stopOrRunningYarnQueue(clusterId, infos);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@GetMapping("/findServerInfo")
	public Result<SdpsServerInfo> selectServerInfo(
			@RequestParam("serverId") Long serverId,
			@RequestParam("type") String type) {
		try {
			SdpsCluster sdpsCluster = clusterMapper.selectById(serverId);
			List<SdpsServerInfo> serverInfos = serverInfoMapper
					.selectList(new QueryWrapper<SdpsServerInfo>()
							.select("server_id", "type", "host", "port",
									"domain")
							.eq("server_id", sdpsCluster.getServerId())
							.eq("type", type));
			if (CollUtil.isEmpty(serverInfos)) {
				return Result.succeed("操作成功");
			}
			return Result.succeed(serverInfos.get(0), "操作成功");

		} catch (Exception ex) {
			log.error("findServerInfo-error", ex);
		}
		return Result.failed("操作失败");
	}

	@GetMapping("/getServerInfo")
	public SdpsServerInfo getServerInfo(
			@RequestParam("username") String username) {
		try {
			List<SdpsServerInfo> serverInfos = serverInfoMapper
					.selectList(new QueryWrapper<SdpsServerInfo>().eq(
							"server_id", "0").eq("user", username));
			if (CollUtil.isEmpty(serverInfos)) {
				return null;
			}
			return serverInfos.get(0);

		} catch (Exception ex) {
			log.error("findServerInfo-error", ex);
		}
		return null;
	}

	@PostMapping("/addAmbariUser")
	public Result addAmbariUser(@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser) {
		try {
			return commonBigData.addAmbariUser(clusterId, ambariUser);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@DeleteMapping("/deleteAmbariUser")
	public Result deleteAmbariUser(
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("username") String username) {
		try {
			return commonBigData.deleteAmbariUser(clusterId, username);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@PutMapping("/updateAmbariUserPassword")
	public Result updateAmbariUserPassword(
			@RequestParam("clusterId") Integer clusterId,
			@RequestBody AmbariUser ambariUser) {
		try {
			return commonBigData
					.updateAmbariUserPassword(clusterId, ambariUser);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@PostMapping("/createItemResource")
	public Result createItemResource(@RequestParam("itemIden") String itemIden,
			@RequestBody HdfsSetDirObj hdfsSetDirObj) {
		try {
			return commonBigData.createItemResource(itemIden, hdfsSetDirObj);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@DeleteMapping("/deleteItemFile")
	public Result deleteItemFile(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") List<String> hdfsPaths) {
		JSONObject jsonObject = null;
		try {
			Result result = commonBigData.deleteItemFile(clusterId, hdfsPaths);
			jsonObject = new JSONObject((Map) result.getData());
			if (jsonObject.getBoolean("success")) {
				return Result.succeed(result, "删除HDFS目录:" + hdfsPaths + "成功");
			} else {
				return Result.failed(jsonObject, "删除HDFS目录" + hdfsPaths + "失败");
			}
		} catch (Exception e) {
			jsonObject = new JSONObject();
			jsonObject.put("message", e.getMessage());
			return Result.failed(jsonObject, e.getMessage());
		}
	}

	@GetMapping("/selectHdfsSaveObjListByUser")
	public Result<List<HdfsFSObj>> selectHdfsSaveObjListByUser(
			@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam("hdfsPath") String hdfsPath) {
		try {
			return commonBigData.selectHdfsSaveObjListByUser(sysUser.getId(),
					sysUser.getUsername(), clusterId, hdfsPath);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 获取ambari用户信息
	 *
	 * @return
	 */
	@GetMapping("/getAmbariUsers")
	public Result<JSONArray> getAmbariUsers(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			return commonBigData.getAmbariUsers(clusterId);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}

	@GetMapping("/findServerKerberosInfo")
	public Result<JSONObject> findServerKerberosInfo(
			@RequestParam("clusterId") Integer clusterId) {
		try {
			return commonBigData.findServerKerberosInfo(clusterId);
		} catch (Exception e) {
			return Result.failed("操作失败");
		}
	}
}
