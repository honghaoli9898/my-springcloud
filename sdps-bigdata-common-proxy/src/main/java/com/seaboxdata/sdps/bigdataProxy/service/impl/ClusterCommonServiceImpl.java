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
 * ????????????Service??????
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
		// ????????????????????? ????????????

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
			// ??????????????????
			cluster.setRunStatus(cluster.getRunning());
			// ??????????????????
			cluster.setStatus(sdpsClusterStatusMap.get(
					cluster.getClusterStatusId()).getStatusName());
		});
		records.forEach(record -> {
			// ???????????????
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

			// ?????????
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
			return Result.failed("????????????????????????");
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

		// ???????????????url???????????? ??????????????????showname
		sdpsCluster.setClusterIp(ip);
		sdpsCluster.setClusterPort(Integer.valueOf(port));

		// ServerInfo??????????????????
		String username = sdpsCluster.getClusterAccount();
		SysGlobalArgs sysGlobalArgs = sysGlobalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "publicKey"));
		String passwd = RsaUtil.encrypt(sdpsCluster.getClusterPasswd(),sysGlobalArgs.getArgValue());

		// ????????????????????? ??????????????????????????????
		Result<String> validateLoginResult = validateLogin(username, sdpsCluster.getClusterPasswd(),
				ip, port);
		if (validateLoginResult.isFailed()) {
			return Result.failed(validateLoginResult.getMsg());
		}

		//??????????????????
		JSONObject jsonObject = JSONObject.parseObject(validateLoginResult.getData());
		JSONArray widget_layouts = jsonObject.getJSONArray("widget_layouts");
		String cluster_name = widget_layouts.getJSONObject(0).getJSONObject("WidgetLayoutInfo").getString("cluster_name");
		sdpsCluster.setClusterName(cluster_name);
		log.info("????????????, ???????????????...");
		baseMapper.insert(sdpsCluster);

		Integer clusterId = sdpsCluster.getClusterId();
		log.info("??????ID {}", clusterId);

		SdpsServerInfo sdpsServerInfo = SdpsServerInfo.builder()
				.serverId(clusterId)
				.host(ip)
				.port(port)
				.user(username)
				.type(ServerTypeEnum.A.name())
				.build();
		// ???????????? ??????????????????
		Long serverCount = serverInfoMapper.selectCount(new QueryWrapper<>(sdpsServerInfo));
		if (serverCount > 0) {
			log.info("ServerInfo ???????????? {}", JSONUtil.toJsonStr(sdpsServerInfo));
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
			log.info("??????????????????????????????");
		}

		return Result.succeed(sdpsCluster, "????????????????????????");
	}

	/**
	 * ??????????????????????????????????????????????????????
	 *
	 * @param username
	 *            ?????????
	 * @param passwd
	 *            ??????
	 * @param ip
	 *            IP
	 * @param port
	 *            ??????
	 */
	private Result<String> validateLogin(String username, String passwd,
			String ip, String port) {
		Map<String, String> headerParameter = MapUtil.newHashMap();
		headerParameter.put("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, String> parameter = MapUtil.newHashMap();
		parameter.put("username", username);
		parameter.put("password", passwd);

		// ????????????????????? ??????????????????????????????
		Map<String, Object> hostMap = new HashedMap<>();
		hostMap.put("username", username);
		hostMap.put("passwd", passwd);
		hostMap.put("ip", ip);
		hostMap.put("port", port);
		JSONObject validateResult = seaBoxPlatform
				.validatePlatformAccountPaaswd(hostMap);
		if (validateResult.isEmpty()) {
			return Result.failed("?????????????????????");
		}

		return Result.succeed(JSONObject.toJSONString(validateResult),"??????????????????");
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
		return result > 0 ? Result.succeed("??????????????????") : Result.failed("??????????????????");
	}

	/**
	 * ??????????????????????????????ip????????????
	 *
	 * @param id
	 *            ??????ID
	 * @param sdpsCluster
	 *            ??????
	 */
	private Result<SdpsCluster> validateCluster(Long id, SdpsCluster sdpsCluster) {
		SdpsCluster qSdpsCluster = baseMapper.selectById(id);
		if (qSdpsCluster == null) {
			return Result.failed("???????????????");
		}

		Integer clusterStatusId = qSdpsCluster.getClusterStatusId();
		if (clusterStatusId.equals(FAILED.getId())
				|| clusterStatusId.equals(AMBARI_FAILED.getId())) {
			return Result.succeed(qSdpsCluster, "?????????????????????????????????");
		}

		String account = qSdpsCluster.getClusterAccount();
		qSdpsCluster.setClusterAccount(account);
		String passwd = qSdpsCluster.getClusterPasswd();
		qSdpsCluster.setClusterPasswd(qSdpsCluster.getClusterPasswd());
		String ip = qSdpsCluster.getClusterIp();
		Integer port = qSdpsCluster.getClusterPort();
		if (sdpsCluster != null) {
			// IP????????????????????????????????????????????????
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
			return Result.failed("???????????????????????????????????????");
		}

		return Result.succeed(qSdpsCluster, "????????????");
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
		 * if (qSdpsCluster.getRunning()) { return Result.failed("?????????????????????????????????");
		 * }
		 */

		qSdpsCluster.setIsUse(false);
		qSdpsCluster.setMenderId(user.getId());
		qSdpsCluster.setUpdateTime(new Date());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("??????????????????") : Result.failed("??????????????????");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> clusterSave(SysUser user, SdpsCluster sdpsCluster)
			throws Exception {
		// ?????? ?????? ????????????
		SdpsCluster qSdpsCluster = baseMapper.selectOne(new QueryWrapper<>(
				sdpsCluster));
		if (qSdpsCluster != null) {
			return Result.failed("???????????????");
		}

		Set<String> loginFailedIps = new HashSet<>();
		// TODO ???????????????????????? ?????????????????????????????? ??????????????????
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
			return Result.failed("?????????????????????????????????");
		}

		// ?????????????????????
		// 1. ??????cluster???Service
		sdpsCluster.setCreaterId(user.getId());
		Result<String> saveClusterAndServices = saveClusterAndServices(
				sdpsCluster, hostAndServiceList);
		log.info(saveClusterAndServices.getMsg());

		// ???????????? ?????????
		// Future<String> clusterInstallResult = taskExecutor.submit(new
		// ClusterHandleTask(hostAndServiceList));
		taskExecutor.submit(new ClusterHandleTask(hostAndServiceList,
				sdpsCluster.getClusterId()));
		// String s = clusterInstallResult.get();
		// ?????????????????????
		// ?????????

		// ????????????

		// ???????????? ??????volatile ???????????? ???????????????????????????????????????socket

		// ????????????
		// ssh ???????????? ????????????
		// ??????????????? rabbitmq
		// ????????????rabbitmq???????????????????????????????????? Socket ???

		return Result.succeed("????????????????????????");
	}

	/**
	 * ?????????????????????
	 *
	 * @param sdpsCluster
	 *            ??????
	 * @param hostAndServiceList
	 *            ?????????Service
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

		// ????????????
		int clusterResult = baseMapper.insert(sdpsCluster);

		// ??????????????????
		clusterServices.forEach(clusterService -> clusterService
				.setClusterId(sdpsCluster.getClusterId()));
		Integer serviceResult = clusterServiceMapper
				.insertBatchSomeColumn(clusterServices);

		if (clusterResult < 0 || serviceResult == null || serviceResult < 0) {
			return Result.failed("???????????????????????????");
		}

		return Result.succeed("???????????????????????????");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<SdpsCluster> batchDelete(SysUser user, List<Long> ids) {
		List<SdpsCluster> qSdpsClusters = baseMapper.selectBatchIds(ids);
		log.info("?????????????????? {} ?????????????????? {}", ids.size(), qSdpsClusters.size());
		if (ListUtil.isEmpty(qSdpsClusters)) {
			return Result.failed("??????????????????????????????");
		}

		/*
		 * List<Boolean> useList =
		 * qSdpsClusters.stream().map(SdpsCluster::getRunning).filter(m ->
		 * m.equals(true)) .collect(Collectors.toList()); if
		 * (ListUtil.isNotEmpty(useList)) { return Result.failed("?????????????????????????????????");
		 * }
		 */

		Set<Long> failedIds = new HashSet<>();
		Set<String> failedNames = new HashSet<>();
		for (SdpsCluster qSdpsCluster : qSdpsClusters) {
			Integer clusterStatusId = qSdpsCluster.getClusterStatusId();
			if (clusterStatusId.equals(FAILED.getId())
					|| clusterStatusId.equals(AMBARI_FAILED.getId())) {
				log.info("?????????{}??????????????????????????????", qSdpsCluster.getClusterName());
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
		log.info("??????????????????????????? {}", JSONUtil.toJsonStr(failedNames));
		String msg = String.format("???????????? %s ?????? ???????????? %s ???", ids.size(),
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
		// todo ????????? ?????????????????? ????????????????????????????????????????????????
		return result ? Result.succeed("??????????????????") : Result.failed("??????????????????");
	}

	/**
	 * ?????????????????????
	 *
	 * @param user
	 *            ?????????
	 */
	public Result<String> validateUser(SysUser user) {
		if (user == null) {
			return Result.failed("?????????????????????");
		}
		List<String> roleCodes = user.getRoles().stream().map(SysRole::getCode)
				.collect(Collectors.toList());
		if (!roleCodes.contains("admin")) {
			return Result.failed("?????????????????????????????????????????????");
		}
		return Result.succeed("????????????");
	}

	@Override
	public Result<SdpsCluster> startCluster(SysUser user, Long id,
			JSONObject usernameAndPasswd) throws Exception {
		Result<String> validateUser = validateUser(user);
		if (validateUser.isFailed()) {
			return Result.failed(validateUser.getMsg());
		}

		// isUse ????????? runStatus ???????????????
		SdpsCluster qSdpsCluster = baseMapper.selectById(id);
		if (qSdpsCluster == null) {
			return Result.failed("???????????????");
		}

		if (SdpsCluster.Status.OK.getId() != qSdpsCluster.getClusterStatusId()) {
			return Result.failed("??????????????????");
		}
		if (ClusterConstants.REMOTE_JOIN
				.equals(qSdpsCluster.getClusterSource())
				&& usernameAndPasswd == null) {
			return Result.failed("??????????????????????????????????????????");
		}
		if (qSdpsCluster.getRunning()) {
			return Result.failed("??????????????????????????????");
		}

		SdpsClusterHost host = getHost(qSdpsCluster);
		if (host == null) {
			log.error("???????????????????????????????????????????????????????????????????????????");
			return Result.failed("????????????");
		}
		// TODO ????????????????????????????????? ???????????? ??????????????????ip ???????????????
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

		// ????????????
		String shScript = "ambari-server start";
		int shResult = shellExecutorUtil.execShell(shScript);

		// ??????????????????????????????????????????????????????????????????????????????????????????
		if (shResult == -1) {
			StringBuffer outErrStringBuffer = shellExecutorUtil
					.getOutErrStringBuffer();
			log.info("?????? {} ???????????????????????? {}", qSdpsCluster.getClusterIp(),
					outErrStringBuffer.toString());
			return Result.failed("???????????? "
					+ String.format("?????????????????? %s", outErrStringBuffer));
		}

		// ??????????????????????????????????????????????????????????????????
		Result<String> validateLogin = validateLogin(
				qSdpsCluster.getClusterAccount(),
				qSdpsCluster.getClusterPasswd(), host.getIp(), "8080");
		if (validateLogin.isFailed()) {
			shellExecutorUtil.execShell("ambari-server stop");
			return Result.failed("????????????????????????????????????????????????");
		}

		qSdpsCluster.setRunning(SdpsCluster.RunStatus.START.getStatus());
		qSdpsCluster.setUpdateTime(new Date());
		qSdpsCluster.setMenderId(user.getId());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("????????????") : Result.failed("????????????");
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param qSdpsCluster
	 *            ??????
	 */
	private SdpsClusterHost getHost(SdpsCluster qSdpsCluster) {
		List<JSONObject> hostList = JSONArray.parseArray(
				qSdpsCluster.getClusterHostConf()).toJavaList(JSONObject.class);
		SdpsClusterHost hostData = null;
		if (ListUtil.isNotEmpty(hostList)) {
			for (JSONObject host : hostList) {
				// ????????? domainName??? ????????? host
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

		// isUse ????????? runStatus ???????????????
		Result<SdpsCluster> validateCluster = validateCluster(id, null);
		if (validateCluster.isFailed()) {
			return validateCluster;
		}
		SdpsCluster qSdpsCluster = validateCluster.getData();

		if (SdpsCluster.Status.OK.getId() != qSdpsCluster.getClusterStatusId()) {
			return Result.failed("??????????????????");
		}
		if (ClusterConstants.REMOTE_JOIN
				.equals(qSdpsCluster.getClusterSource())
				&& usernameAndPasswd == null) {
			return Result.failed("??????????????????????????????????????????");
		}
		if (!qSdpsCluster.getRunning()) {
			return Result.failed("??????????????????????????????");
		}

		SdpsClusterHost host = getHost(qSdpsCluster);
		if (host == null) {
			log.error("???????????????????????????????????????????????????????????????????????????");
			return Result.failed("????????????");
		}
		// TODO ????????????????????????????????? ????????????
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

		// ????????????
		String shScript = "ambari-server stop";
		int shResult = shellExecutorUtil.execShell(shScript);

		// ??????????????????????????????????????????????????????????????????????????????????????????
		if (shResult == -1) {
			StringBuffer outErrStringBuffer = shellExecutorUtil
					.getOutErrStringBuffer();
			log.info("?????? {} ???????????????????????? {}", qSdpsCluster.getClusterIp(),
					outErrStringBuffer.toString());
			return Result.failed("???????????? "
					+ String.format("?????????????????? %s", outErrStringBuffer));
		}

		qSdpsCluster.setRunning(SdpsCluster.RunStatus.STOP.getStatus());
		qSdpsCluster.setUpdateTime(new Date());
		qSdpsCluster.setMenderId(user.getId());
		int result = baseMapper.updateById(qSdpsCluster);
		return result > 0 ? Result.succeed("????????????") : Result.failed("????????????");
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
	 * ??????????????????????????????
	 *
	 * @param cluster
	 *            ????????????
	 */
	private String validatRemoteParams(SdpsCluster cluster) {
		if (StringUtils.isBlank(cluster.getClusterShowName())) {
			return "????????????????????????";
		}
		if (StringUtils.isBlank(cluster.getRemoteUrl())) {
			return "??????URL????????????";
		}
		if (StringUtils.isBlank(cluster.getClusterAccount())) {
			return "????????????????????????";
		}
		if (StringUtils.isBlank(cluster.getClusterPasswd())) {
			return "??????????????????";
		}
		return null;
	}
}
