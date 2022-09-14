package com.seaboxdata.sdps.bigdataProxy.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterStatusMapper;
import com.seaboxdata.sdps.common.core.model.*;
import com.seaboxdata.sdps.common.framework.bean.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.SeaBoxPlatform;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterCommonService;
import com.seaboxdata.sdps.bigdataProxy.task.ClusterHandleTask;
import com.seaboxdata.sdps.bigdataProxy.util.ListUtil;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.constant.SQLConstants;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SqlTool;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;

import static com.seaboxdata.sdps.common.framework.bean.SdpsCluster.Status.AMBARI_FAILED;
import static com.seaboxdata.sdps.common.framework.bean.SdpsCluster.Status.FAILED;

/**
 * 集群管理Service实现
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
@Service
@Slf4j
public class ClusterCommonServiceImpl extends
		SuperServiceImpl<SdpsClusterMapper, SdpsCluster> implements
		IClusterCommonService {

	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;
	@Autowired
	private SdpsClusterServiceMapper clusterServiceMapper;
	@Autowired
	private SeaBoxPlatform seaBoxPlatform;

	@Autowired
	private SdpsClusterStatusMapper statusMapper;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private SysGlobalArgsMapper sysGlobalArgsMapper;

	@Override
	@SuppressWarnings("unchecked")
	public PageResult<SdpsCluster> clusters(PageRequest<JSONObject> requestBean) {
		IPage<SdpsCluster> page = new Page<SdpsCluster>(requestBean.getPage(),
				requestBean.getSize());
		// 没有排序则按照 创建时间

		QueryWrapper<SdpsCluster> queryWrapper = new QueryWrapper<>();
		queryWrapper = queryWrapper.orderByDesc(SQLConstants.CREATE_TIME);
		JSONObject param = requestBean.getParam();
		if (MapUtil.isNotEmpty(param)) {
			SqlTool.setLkParam(queryWrapper, "cluster_show_name",
					param.get("clusterShowName"));
			SqlTool.setEqParam(queryWrapper, "cluster_status_id",
					param.get("clusterStatusId"));
			SqlTool.setGeParam(queryWrapper, "create_time",
					param.get("startTime"));
			SqlTool.setLeParam(queryWrapper, "create_time",
					param.get("endTime"));
		}
		queryWrapper.eq("is_use", true);

		IPage<SdpsCluster> sdpsClusterPage = baseMapper.selectPage(page,
				queryWrapper);

		List<SdpsCluster> records = sdpsClusterPage.getRecords();

		Map<Integer, SdpsClusterStatus> sdpsClusterStatusMap = statusMapper
				.selectList(new QueryWrapper<>())
				.stream()
				.collect(
						Collectors.toMap(SdpsClusterStatus::getClusterStatusId,
								Function.identity()));

		records.forEach(cluster -> {
			// 设置运行状态
			cluster.setRunStatus(cluster.getRunning());
			// 设置集群状态
			cluster.setStatus(sdpsClusterStatusMap.get(
					cluster.getClusterStatusId()).getStatusName());
		});
		records.forEach(record -> {
			// 执行操作数
			JSONObject performOperation = performOperation(record
					.getClusterId());
			JSONArray performOperationItems = performOperation
					.getJSONArray("items");
			if (performOperationItems == null
					|| performOperationItems.isEmpty()) {
				record.setPerformOperationCount(0L);
			} else {
				record.setPerformOperationCount((long) performOperationItems
						.size());
			}

			// 警告数
			JSONObject alarmMsg = alarmMsg(record.getClusterId());
			JSONObject alarmMsgAlertDefinition = alarmMsg
					.getJSONObject("clusterAlertDefinition");
			if (alarmMsgAlertDefinition == null
					|| alarmMsgAlertDefinition.isEmpty()) {
				record.setAlarmCount(0L);
			} else {
				record.setAlarmCount((long) alarmMsgAlertDefinition
						.getJSONArray("items").size());
			}
		});

		return (PageResult<SdpsCluster>) PageResult.success(
				sdpsClusterPage.getTotal(), records);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> remoteClusterSave(SysUser user,
			SdpsCluster sdpsCluster) throws Exception {
		String validate = validatRemoteParams(sdpsCluster);
		if (StringUtils.isNotBlank(validate)) {
			return Result.failed(validate);
		}

		SdpsCluster qSdpsCluster = baseMapper.selectOne(new QueryWrapper<>(
				sdpsCluster).eq("is_use", true));
		if (qSdpsCluster != null) {
			return Result.failed("该远程集群已存在");
		}

		sdpsCluster.setClusterSource(ClusterConstants.REMOTE_JOIN);
		sdpsCluster.setClusterStatusId(SdpsCluster.Status.INIT.getId());
		sdpsCluster.setClusterTypeId(SdpsCluster.Type.SEABOX.getId());
		sdpsCluster.setIsUse(false);

		// [{'host':'master','ip':'10.1.3.113'},{'host':'slave1','ip':'10.1.3.114'},{'host':'slave2','ip':'10.1.3.115'}]
		sdpsCluster.setClusterHostConf("[{'host':'master', 'ip':'10.1.3.254'}]");

		// sdpsCluster.setCreaterId(user.getId());
		sdpsCluster.setCreateTime(new Date());
		sdpsCluster.setUpdateTime(new Date());

		String remoteUrl = sdpsCluster.getRemoteUrl();
		String regexStr = "(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)";
		Matcher matcher = Pattern.compile(regexStr).matcher(remoteUrl);
		String ip = "";
		String port = "";
		while(matcher.find()) {
			ip = matcher.group(1);
			port = matcher.group(2);
		}

		// 真实名称为url中的名称 传入的名称为showname
		sdpsCluster.setClusterIp(ip);
		sdpsCluster.setClusterPort(Integer.valueOf(port));

		// ServerInfo保存相应信息
		String username = sdpsCluster.getClusterAccount();
		SysGlobalArgs sysGlobalArgs = sysGlobalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "publicKey"));
		String passwd = RsaUtil.encrypt(sdpsCluster.getClusterPasswd(),sysGlobalArgs.getArgValue());

		// 使用登录来校验 远程集群是否已经存在
		Result<String> validateLoginResult = validateLogin(username, sdpsCluster.getClusterPasswd(),
				ip, port);
		if (validateLoginResult.isFailed()) {
			return Result.failed(validateLoginResult.getMsg());
		}

		//设置集群名称
		JSONObject jsonObject = JSONObject.parseObject(validateLoginResult.getData());
		JSONArray widget_layouts = jsonObject.getJSONArray("widget_layouts");
		String cluster_name = widget_layouts.getJSONObject(0).getJSONObject("WidgetLayoutInfo").getString("cluster_name");
		sdpsCluster.setClusterName(cluster_name);
		log.info("校验成功, 插入数据中...");
		baseMapper.insert(sdpsCluster);

		Integer clusterId = sdpsCluster.getClusterId();
		log.info("集群ID {}", clusterId);

		SdpsServerInfo sdpsServerInfo = SdpsServerInfo.builder()
				.serverId(clusterId)
				.host(ip)
				.port(port)
				.user(username)
				.type(ServerTypeEnum.A.name())
				.build();
		// 插入之前 查询是否存在
		Long serverCount = serverInfoMapper.selectCount(new QueryWrapper<>(sdpsServerInfo));
		if (serverCount > 0) {
			log.info("ServerInfo 已经存在 {}", JSONUtil.toJsonStr(sdpsServerInfo));
		} else {
			sdpsServerInfo.setPasswd(passwd);
			serverInfoMapper.insert(sdpsServerInfo);
		}

		JSONObject clusterHost = seaBoxPlatform.getClusterHosts(sdpsServerInfo);
		/*JSONObject clusterNameResult = seaBoxPlatform
				.getClusterName(sdpsCluster.getClusterId());
		String clusterName = clusterNameResult.getJSONObject("Clusters")
				.getString("cluster_name");*/
		JSONArray items = clusterHost.getJSONArray("items");
		if (ListUtil.isNotEmpty(items)) {
			JSONArray saveHosts = new JSONArray();
			items.toJavaList(JSONObject.class).forEach(item -> {
				JSONObject nameHost = new JSONObject();
				JSONObject hosts = item.getJSONObject("Hosts");
				nameHost.put("host", hosts.getString("host_name"));
				nameHost.put("ip", hosts.getString("ip"));
				saveHosts.add(nameHost);
			});
			sdpsCluster.setClusterHostConf(saveHosts.toJSONString());
			// sdpsCluster.setClusterName(clusterName);
			sdpsCluster.setIsUse(true);
			sdpsCluster.setServerId(sdpsCluster.getClusterId().toString());
			sdpsCluster.setRunning(true);
			// sdpsCluster.setCreaterId(user.getId());
			sdpsCluster.setClusterStatusId(SdpsCluster.Status.OK.getId());
			baseMapper.updateById(sdpsCluster);
			log.info("远程集群主机更新成功");
		}

		return Result.succeed(sdpsCluster, "远程集群创建成功");
	}

	/**
	 * 使用页面登录接口校验是否可以登录成功
	 *
	 * @param username
	 *            用户名
	 * @param passwd
	 *            密码
	 * @param ip
	 *            IP
	 * @param port
	 *            端口
	 */
	private Result<String> validateLogin(String username, String passwd,
			String ip, String port) {
		Map<String, String> headerParameter = MapUtil.newHashMap();
		headerParameter.put("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, String> parameter = MapUtil.newHashMap();
		parameter.put("username", username);
		parameter.put("password", passwd);

		// 使用登录来校验 远程集群是否已经存在
		Map<String, Object> hostMap = new HashedMap<>();
		hostMap.put("username", username);
		hostMap.put("passwd", passwd);
		hostMap.put("ip", ip);
		hostMap.put("port", port);
		JSONObject validateResult = seaBoxPlatform
				.validatePlatformAccountPaaswd(hostMap);
		if (validateResult.isEmpty()) {
			return Result.failed("请检查账号密码");
		}

		return Result.succeed(JSONObject.toJSONString(validateResult),"校验登录成功");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> update(SysUser user, Long id,
			SdpsCluster sdpsCluster) {
		Result<SdpsCluster> validateCluster = validateCluster(id, sdpsCluster);
		if (validateCluster.isFailed()) {
			return validateCluster;
		}

		SdpsCluster qClusterData = validateCluster.getData();
		qClusterData.setClusterShowName(ObjectUtil.isNotNull(sdpsCluster
				.getClusterShowName()) ? sdpsCluster.getClusterShowName()
				: qClusterData.getClusterShowName());
		qClusterData.setClusterDescription(ObjectUtil.isNotNull(sdpsCluster
				.getClusterDescription()) ? sdpsCluster.getClusterDescription()
				: qClusterData.getClusterDescription());
		// qClusterData.setMenderId(user.getId());
		qClusterData.setUpdateTime(new Date());
		int result = baseMapper.updateById(qClusterData);
		return result > 0 ? Result.succeed("集群更新成功") : Result.failed("集群更新失败");
	}

	/**
	 * 检验集群数据，并检查ip账号密码
	 *
	 * @param id
	 *            集群ID
	 * @param sdpsCluster
	 *            集群
	 */
	private Result<SdpsCluster> validateCluster(Long id, SdpsCluster sdpsCluster) {
		SdpsCluster qSdpsCluster = baseMapper.selectById(id);
		if (qSdpsCluster == null) {
			return Result.failed("集群不存在");
		}

		Integer clusterStatusId = qSdpsCluster.getClusterStatusId();
		if (clusterStatusId.equals(FAILED.getId())
				|| clusterStatusId.equals(AMBARI_FAILED.getId())) {
			return Result.succeed(qSdpsCluster, "集群安装失败，无需校验");
		}

		String account = qSdpsCluster.getClusterAccount();
		qSdpsCluster.setClusterAccount(account);
		String passwd = qSdpsCluster.getClusterPasswd();
		qSdpsCluster.setClusterPasswd(qSdpsCluster.getClusterPasswd());
		String ip = qSdpsCluster.getClusterIp();
		Integer port = qSdpsCluster.getClusterPort();
		if (sdpsCluster != null) {
			// IP和端口不可修改，所以无需更新修改
			ip = ObjectUtil.isNotNull(sdpsCluster.getClusterIp()) ? sdpsCluster
					.getClusterIp() : ip;
			qSdpsCluster.setClusterIp(ip);
			port = ObjectUtil.isNotNull(sdpsCluster.getClusterPort()) ? sdpsCluster
					.getClusterPort() : port;
			qSdpsCluster.setClusterPort(port);
		}

		Result<String> validateLoginResult = validateLogin(account, passwd, ip,
				String.valueOf(port));
		if (validateLoginResult.isFailed()) {
			return Result.failed("请检查集群账号密码是否正确");
		}

		return Result.succeed(qSdpsCluster, "检验成功");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> delete(SysUser user, Long id) {
		Result<SdpsCluster> validateCluster = validateCluster(id, null);
		if (validateCluster.isFailed()) {
			return validateCluster;
		}

		SdpsCluster qSdpsCluster = validateCluster.getData();
		/*
		 * if (qSdpsCluster.getRunning()) { return Result.failed("请先停用集群再进行删除");
		 * }
		 */

		qSdpsCluster.setIsUse(false);
		qSdpsCluster.setMenderId(user.getId());
		qSdpsCluster.setUpdateTime(new Date());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("集群移除成功") : Result.failed("集群移除失败");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> clusterSave(SysUser user, SdpsCluster sdpsCluster)
			throws Exception {
		// 校验 集群 集群服务
		SdpsCluster qSdpsCluster = baseMapper.selectOne(new QueryWrapper<>(
				sdpsCluster));
		if (qSdpsCluster != null) {
			return Result.failed("集群已存在");
		}

		Set<String> loginFailedIps = new HashSet<>();
		// TODO 需要分主从节点？ 主机之间有数据交互吗 有先后顺序吗
		// [{'host':'10.1.1.1','type':'master','services':[{'name':'zk',
		// 'version':'0.0.1'}, ...]}, ...]
		List<SdpsClusterHostService> hostAndServiceList = sdpsCluster
				.getHostAndServiceList();

		for (SdpsClusterHostService hostAndService : hostAndServiceList) {
			SdpsClusterHost host = hostAndService.getHost();
			RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(
					host.getIp(), host.getName(), host.getPasswd());
			boolean login = shellExecutorUtil.login();
			if (!login) {
				loginFailedIps.add(host.getIp());
			}
		}
		if (loginFailedIps.size() > 0) {
			return Result.failed("请检查主机信息是否正确");
		}

		// 保存初始化数据
		// 1. 保存cluster和Service
		sdpsCluster.setCreaterId(user.getId());
		Result<String> saveClusterAndServices = saveClusterAndServices(
				sdpsCluster, hostAndServiceList);
		log.info(saveClusterAndServices.getMsg());

		// 执行脚本 异步？
		// Future<String> clusterInstallResult = taskExecutor.submit(new
		// ClusterHandleTask(hostAndServiceList));
		taskExecutor.submit(new ClusterHandleTask(hostAndServiceList,
				sdpsCluster.getClusterId()));
		// String s = clusterInstallResult.get();
		// 一般三台主机？
		// 集群表

		// 封装数据

		// 异步执行 使用volatile 变量共享 守护线程监听获取数据返回到socket

		// 异步调用
		// ssh 执行脚本 携带参数
		// 返回参数到 rabbitmq
		// 后端监听rabbitmq，处理返回结果，提示消息 Socket ？

		return Result.succeed("新建集群运行成功");
	}

	/**
	 * 初始化集群信息
	 *
	 * @param sdpsCluster
	 *            集群
	 * @param hostAndServiceList
	 *            主机和Service
	 */
	private Result<String> saveClusterAndServices(SdpsCluster sdpsCluster,
			List<SdpsClusterHostService> hostAndServiceList) {
		List<SdpsClusterService> clusterServices = new ArrayList<>();

		Date createDate = new Date();
		JSONArray clusterHosts = new JSONArray();
		for (SdpsClusterHostService sdpsClusterHostService : hostAndServiceList) {

			SdpsClusterHost host = sdpsClusterHostService.getHost();
			List<SdpsClusterService> services = sdpsClusterHostService
					.getServices();
			services.forEach(service -> {
				service.setCreateTime(createDate);
				service.setHost(host.getIp());
			});
			clusterServices.addAll(services);

			JSONObject hostAndType = new JSONObject();
			hostAndType.put("host", sdpsClusterHostService.getType());
			hostAndType.put("ip", host.getIp());
			hostAndType.put("username", host.getName());
			String encodePasswd = Base64.getEncoder().encodeToString(
					(host.getPasswd() + ClusterConstants.CLUSTER_SALT)
							.getBytes());
			hostAndType.put("passwd", encodePasswd);
			clusterHosts.add(hostAndType);
			if ("master".equals(sdpsClusterHostService.getType())) {
				String masterIp = host.getIp();
				sdpsCluster.setClusterIp(masterIp);
				sdpsCluster.setClusterPort(8080);
				sdpsCluster.setClusterName(sdpsCluster.getClusterShowName()
						+ "_real");
				sdpsCluster.setClusterAccount("admin");
				sdpsCluster.setClusterPasswd("admin");
				sdpsCluster.setClusterStatusId(SdpsCluster.Status.DEPLOYING
						.getId());
				sdpsCluster.setClusterSource(ClusterConstants.PLATFORM_NEW);
				sdpsCluster.setIsUse(true);
				sdpsCluster.setClusterTypeId(SdpsCluster.Type.SEABOX.getId());
				sdpsCluster.setCreateTime(createDate);
				sdpsCluster.setRunning(SdpsCluster.RunStatus.STOP.getStatus());
			}
		}

		sdpsCluster.setClusterHostConf(JSONUtil.toJsonStr(clusterHosts
				.toJSONString()));

		// 保存集群
		int clusterResult = baseMapper.insert(sdpsCluster);

		// 保存集群服务
		clusterServices.forEach(clusterService -> clusterService
				.setClusterId(sdpsCluster.getClusterId()));
		Integer serviceResult = clusterServiceMapper
				.insertBatchSomeColumn(clusterServices);

		if (clusterResult < 0 || serviceResult == null || serviceResult < 0) {
			return Result.failed("保存集群及服务成功");
		}

		return Result.succeed("保存集群及服务成功");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> batchDelete(SysUser user, List<Long> ids) {
		List<SdpsCluster> qSdpsClusters = baseMapper.selectBatchIds(ids);
		log.info("需要更新数量 {} 实际更新数量 {}", ids.size(), qSdpsClusters.size());
		if (ListUtil.isEmpty(qSdpsClusters)) {
			return Result.failed("批量删除的集群不存在");
		}

		/*
		 * List<Boolean> useList =
		 * qSdpsClusters.stream().map(SdpsCluster::getRunning).filter(m ->
		 * m.equals(true)) .collect(Collectors.toList()); if
		 * (ListUtil.isNotEmpty(useList)) { return Result.failed("请先停用集群再进行删除");
		 * }
		 */

		Set<Long> failedIds = new HashSet<>();
		Set<String> failedNames = new HashSet<>();
		for (SdpsCluster qSdpsCluster : qSdpsClusters) {
			Integer clusterStatusId = qSdpsCluster.getClusterStatusId();
			if (clusterStatusId.equals(FAILED.getId())
					|| clusterStatusId.equals(AMBARI_FAILED.getId())) {
				log.info("集群【{}】安装失败，无需校验", qSdpsCluster.getClusterName());
				continue;
			}
			Result<String> validateLoginResult = validateLogin(
					qSdpsCluster.getClusterAccount(),
					qSdpsCluster.getClusterPasswd(),
					qSdpsCluster.getClusterIp(),
					String.valueOf(qSdpsCluster.getClusterPort()));
			if (validateLoginResult.isFailed()) {
				failedIds.add(Long.valueOf(qSdpsCluster.getClusterId()));
				failedNames.add(qSdpsCluster.getClusterName());
			}
		}
		log.info("检验登录失败的集群 {}", JSONUtil.toJsonStr(failedNames));
		String msg = String.format("共需移除 %s 个， 实际移除 %s 个", ids.size(),
				ids.size() - failedIds.size());
		log.info(msg);

		ids.removeAll(failedIds);

		List<SdpsCluster> realDelClusters = baseMapper.selectBatchIds(ids);

		Date currentDate = new Date();
		realDelClusters.forEach(cluster -> {
			cluster.setIsUse(false);
			cluster.setMenderId(user.getId());
			cluster.setUpdateTime(currentDate);
		});
		boolean result = saveOrUpdateBatch(realDelClusters);
		// todo 待提示 未删除的集群 数据库未查找到的，校验登录失败的
		return result ? Result.succeed("批量删除成功") : Result.failed("批量删除失败");
	}

	/**
	 * 校验用户及权限
	 *
	 * @param user
	 *            操作者
	 */
	public Result<String> validateUser(SysUser user) {
		if (user == null) {
			return Result.failed("当前用户不存在");
		}
		List<String> roleCodes = user.getRoles().stream().map(SysRole::getCode)
				.collect(Collectors.toList());
		if (!roleCodes.contains("admin")) {
			return Result.failed("当前用户非管理员，不可进行操作");
		}
		return Result.succeed("校验成功");
	}

	@Override
	public Result<SdpsCluster> startCluster(SysUser user, Long id,
			JSONObject usernameAndPasswd) throws Exception {
		Result<String> validateUser = validateUser(user);
		if (validateUser.isFailed()) {
			return Result.failed(validateUser.getMsg());
		}

		// isUse 是删除 runStatus 是运行状态
		SdpsCluster qSdpsCluster = baseMapper.selectById(id);
		if (qSdpsCluster == null) {
			return Result.failed("集群不存在");
		}

		if (SdpsCluster.Status.OK.getId() != qSdpsCluster.getClusterStatusId()) {
			return Result.failed("集群状态异常");
		}
		if (ClusterConstants.REMOTE_JOIN
				.equals(qSdpsCluster.getClusterSource())
				&& usernameAndPasswd == null) {
			return Result.failed("远程集群请提供主节点账号密码");
		}
		if (qSdpsCluster.getRunning()) {
			return Result.failed("该集群已经是启用状态");
		}

		SdpsClusterHost host = getHost(qSdpsCluster);
		if (host == null) {
			log.error("启用失败，未找到主节点信息，请查看主机信息是否正确");
			return Result.failed("启用失败");
		}
		// TODO 使用脚本操作虚拟机集群 启动集群 使用主节点的ip 启用脚本？
		// String decodePasswd = new
		// String(Base64.getDecoder().decode(host.getPasswd()));

		String hostName = host.getName();
		String hostPasswd = host.getPasswd();
		if (usernameAndPasswd != null) {
			hostName = usernameAndPasswd.getString("username");
			hostPasswd = usernameAndPasswd.getString("passwd");
		}
		RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(
				host.getIp(), hostName, hostPasswd);

		// 执行脚本
		String shScript = "ambari-server start";
		int shResult = shellExecutorUtil.execShell(shScript);

		// 输出日志中可返回自定义错误信息，用于展示脚本所获取的错误信息
		if (shResult == -1) {
			StringBuffer outErrStringBuffer = shellExecutorUtil
					.getOutErrStringBuffer();
			log.info("主机 {} 启动脚本执行失败 {}", qSdpsCluster.getClusterIp(),
					outErrStringBuffer.toString());
			return Result.failed("启用失败 "
					+ String.format("脚本执行失败 %s", outErrStringBuffer));
		}

		// 启动成功后，校验账号密码，如果失败则停止集群
		Result<String> validateLogin = validateLogin(
				qSdpsCluster.getClusterAccount(),
				qSdpsCluster.getClusterPasswd(), host.getIp(), "8080");
		if (validateLogin.isFailed()) {
			shellExecutorUtil.execShell("ambari-server stop");
			return Result.failed("启用失败，请检查集群登录账号密码");
		}

		qSdpsCluster.setRunning(SdpsCluster.RunStatus.START.getStatus());
		qSdpsCluster.setUpdateTime(new Date());
		qSdpsCluster.setMenderId(user.getId());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("启用成功") : Result.failed("启用失败");
	}

	/**
	 * 从主机信息中获取主节点主机信息
	 *
	 * @param qSdpsCluster
	 *            集群
	 */
	private SdpsClusterHost getHost(SdpsCluster qSdpsCluster) {
		List<JSONObject> hostList = JSONArray.parseArray(
				qSdpsCluster.getClusterHostConf()).toJavaList(JSONObject.class);
		SdpsClusterHost hostData = null;
		if (ListUtil.isNotEmpty(hostList)) {
			for (JSONObject host : hostList) {
				// 新建为 domainName， 远程为 host
				if ("master".equals(host.getString("domainName"))
						|| "master".equals(host.getString("host"))) {
					hostData = new SdpsClusterHost();
					hostData.setIp(host.getString("ip"));
					hostData.setName(host.getString("name"));
					hostData.setPasswd(host.getString("passwd"));
					break;
				}
			}
		}
		return hostData;
	}

	@Override
	public Result<SdpsCluster> stopCluster(SysUser user, Long id,
			JSONObject usernameAndPasswd) throws Exception {
		Result<String> validateUser = validateUser(user);
		if (validateUser.isFailed()) {
			return Result.failed(validateUser.getMsg());
		}

		// isUse 是删除 runStatus 是运行状态
		Result<SdpsCluster> validateCluster = validateCluster(id, null);
		if (validateCluster.isFailed()) {
			return validateCluster;
		}
		SdpsCluster qSdpsCluster = validateCluster.getData();

		if (SdpsCluster.Status.OK.getId() != qSdpsCluster.getClusterStatusId()) {
			return Result.failed("集群状态异常");
		}
		if (ClusterConstants.REMOTE_JOIN
				.equals(qSdpsCluster.getClusterSource())
				&& usernameAndPasswd == null) {
			return Result.failed("远程集群请提供主节点账号密码");
		}
		if (!qSdpsCluster.getRunning()) {
			return Result.failed("该集群已经是停用状态");
		}

		SdpsClusterHost host = getHost(qSdpsCluster);
		if (host == null) {
			log.error("停用失败，未找到主节点信息，请查看主机信息是否正确");
			return Result.failed("停用失败");
		}
		// TODO 使用脚本操作虚拟机集群 停用集群
		// String decodePasswd = new
		// String(Base64.getDecoder().decode(host.getPasswd()));

		String hostName = host.getName();
		String hostPasswd = host.getPasswd();
		if (usernameAndPasswd != null) {
			hostName = usernameAndPasswd.getString("username");
			hostPasswd = usernameAndPasswd.getString("passwd");
		}
		RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(
				host.getIp(), hostName, hostPasswd);

		// 执行脚本
		String shScript = "ambari-server stop";
		int shResult = shellExecutorUtil.execShell(shScript);

		// 输出日志中可返回自定义错误信息，用于展示脚本所获取的错误信息
		if (shResult == -1) {
			StringBuffer outErrStringBuffer = shellExecutorUtil
					.getOutErrStringBuffer();
			log.info("主机 {} 停用脚本执行失败 {}", qSdpsCluster.getClusterIp(),
					outErrStringBuffer.toString());
			return Result.failed("停用失败 "
					+ String.format("脚本执行失败 %s", outErrStringBuffer));
		}

		qSdpsCluster.setRunning(SdpsCluster.RunStatus.STOP.getStatus());
		qSdpsCluster.setUpdateTime(new Date());
		qSdpsCluster.setMenderId(user.getId());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("停用成功") : Result.failed("停用失败");
	}

	@Override
	public JSONObject performOperation(Integer id) {
		return seaBoxPlatform.performOperation(id);
	}

	@Override
	public JSONObject performOperationDetail(Integer id, Integer nodeId) {
		return seaBoxPlatform.performOperationDetail(id, nodeId);
	}

	@Override
	public JSONObject alarmMsg(Integer id) {
		return seaBoxPlatform.alarmMsg(id);
	}

	@Override
	public List<Integer> getEnableKerberosClusters() {
		QueryWrapper<SdpsCluster> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("kerberos", true)
				.eq("is_use", true)
				.eq("is_running", true);
		List<SdpsCluster> sdpsClusters = baseMapper.selectList(queryWrapper);
		log.info("enable kerberos cluster:{}", sdpsClusters);
		return sdpsClusters.stream().map(cluster -> cluster.getClusterId()).collect(Collectors.toList());
	}

	/**
	 * 检验远程集群基础信息
	 *
	 * @param cluster
	 *            集群信息
	 */
	private String validatRemoteParams(SdpsCluster cluster) {
		if (StringUtils.isBlank(cluster.getClusterShowName())) {
			return "集群名称不能为空";
		}
		if (StringUtils.isBlank(cluster.getRemoteUrl())) {
			return "集群URL不能为空";
		}
		if (StringUtils.isBlank(cluster.getClusterAccount())) {
			return "集群用户不能为空";
		}
		if (StringUtils.isBlank(cluster.getClusterPasswd())) {
			return "密码不能为空";
		}
		return null;
	}
}
