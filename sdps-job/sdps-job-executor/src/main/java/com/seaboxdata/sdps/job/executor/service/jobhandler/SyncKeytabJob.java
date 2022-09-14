package com.seaboxdata.sdps.job.executor.service.jobhandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import cn.hutool.core.bean.BeanUtil;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.job.core.context.XxlJobHelper;
import com.seaboxdata.sdps.job.core.handler.annotation.XxlJob;
import com.seaboxdata.sdps.job.executor.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.job.executor.feign.ItemDataSourceFegin;
import com.seaboxdata.sdps.job.executor.feign.SeaboxProxyFegin;
import com.seaboxdata.sdps.job.executor.feign.UserSyncCenterFegin;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpServerKeytabMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SysGlobalArgsMapper;

@Component
@Slf4j
public class SyncKeytabJob {
	public static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	@Autowired
	SeaboxProxyFegin seaboxProxyFegin;

	@Autowired
	BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	SdpsClusterMapper clusterMapper;

	@Autowired
	SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	SysGlobalArgsMapper sysGlobalArgsMapper;

	@Autowired
	ItemDataSourceFegin itemDataSourceFegin;

	@Autowired
	SdpServerKeytabMapper serverKeytabMapper;

	@Autowired
	private KerberosProperties kerberosProperties;

	@Autowired
	private UserSyncCenterFegin userSyncCenterFegin;
	@Autowired
	private RestTemplate restTemplate;

	@XxlJob("SyncServerKeytab")
	public void syncServerKeytab() {
		XxlJobHelper.log("syncServerKeytab-JOB, start");
		XxlJobHelper.log("params:{}", XxlJobHelper.getJobParam());

		// 查询开启kerberos的集群
		QueryWrapper<SdpsCluster> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("kerberos", true).eq("is_use", true)
				.eq("is_running", true);
		List<SdpsCluster> sdpsClusters = clusterMapper.selectList(queryWrapper);
		if (CollectionUtils.isEmpty(sdpsClusters)) {
			return;
		}
		List<Integer> clusterIds = sdpsClusters.stream()
				.map(sdpsCluster -> sdpsCluster.getClusterId())
				.collect(Collectors.toList());
		// 生成新的keytab文件
		List<SdpServerKeytab> keytabAll = Lists.newArrayList();

		for (Integer clusterId : clusterIds) {
			// 获取集群kerberos列表
			Result serverKeytabs = seaboxProxyFegin.getServerKeytabs(clusterId);
			List<LinkedHashMap> data = (List) serverKeytabs.getData();
			List<SdpServerKeytab> keytabs = BeanUtil.copyToList(data,
					SdpServerKeytab.class);
			List<SdpServerKeytab> userKeytabs = keytabs.stream()
					.filter(keytab -> "USER".equals(keytab.getPrincipalType()))
					.collect(Collectors.toList());
			List<SdpServerKeytab> filterUserKeytabs = userKeytabs
					.stream()
					.collect(
							Collectors.collectingAndThen(
									Collectors
											.toCollection(() -> new TreeSet<SdpServerKeytab>(
													Comparator.comparing(s -> s
															.getPrincipalName()))),
									ArrayList::new));
			List<SdpServerKeytab> serviceKeytabs = keytabs
					.stream()
					.filter(keytab -> "SERVICE".equals(keytab
							.getPrincipalType())).collect(Collectors.toList());
			serviceKeytabs.addAll(filterUserKeytabs);
			// serviceKeytabs = serviceKeytabs.stream().filter(keytab ->
			// "USER".equals(keytab.getPrincipalType())).collect(Collectors.toList());
			// serviceKeytabs = serviceKeytabs.subList(0, 1);
			// 检查目录是否创建
			new File(kerberosProperties.getKdcKeytabPath()).mkdirs();
			generateKeytab(clusterId, serviceKeytabs);
			keytabAll.addAll(serviceKeytabs);
		}

		// 用户同步模块拉取keytab到本地
		keytabAll.forEach(keytab -> keytab.setKeytabFilePath(kerberosProperties
				.getKdcKeytabPath() + "/" + keytab.getKeytabFileName()));
		List<String> keytabList = keytabAll.stream()
				.map(keytab -> keytab.getKeytabFilePath())
				.collect(Collectors.toList());
		Map<String, Object> map = new HashMap<>();
		boolean isReturn = false;
		// 获取用户同步服务,集群配置API接口的HOST和IP
		SpringClientFactory springClientFactory = SpringUtil
				.getBean(SpringClientFactory.class);
		ILoadBalancer loadBalancerUserSync = springClientFactory
				.getLoadBalancer(ServiceNameConstants.USER_SYNC_SERVER);
		List<Server> serversUserSync = loadBalancerUserSync
				.getReachableServers();
		for (Server server : serversUserSync) {
			String usersyncProxyHostIp = server.getHostPort();
			String updateKeytabUrl = "http://".concat(usersyncProxyHostIp)
					.concat("/usersync/pullKeytabFromKdc");
			JSONObject jo = RestTemplateUtil.restPost(restTemplate,
					updateKeytabUrl, JSONArray.toJSONString(keytabList));
			Result result = jo.toJavaObject(Result.class);
			if (result != null && 0 == result.getCode()) {
				if (isReturn) {
					continue;
				} else {
					map = (Map<String, Object>) result.getData();
					isReturn = true;
				}
				log.info("成功同步到userSyncCenter模块");
			} else {
				log.error("同步到userSyncCenter模块失败：{}", result.getMsg());
				return;
			}
		}
		// 获取到keytab的base64字符串封装到keytabAll中
		Date date = new Date();
		for (SdpServerKeytab keytab : keytabAll) {
			keytab.setKeytabContent(map.get(keytab.getKeytabFilePath())
					.toString());
			keytab.setKeytabFilePath(kerberosProperties.getUserSyncKeytabPath()
					+ "/" + keytab.getKeytabFileName());
			keytab.setCreateTime(date);
			keytab.setUpdateTime(date);
		}

		// 更新数据库
		updateKeytabInfo(keytabAll);

		// 获取海盒服务,集群配置API接口的HOST和IP
		ILoadBalancer loadBalancerSeabox = springClientFactory
				.getLoadBalancer(ServiceNameConstants.SEABOX_PROXY_SERVICE);
		List<Server> serversSeabox = loadBalancerSeabox.getReachableServers();
		// 变更keytab文件路径
		keytabAll.forEach(keytab -> keytab.setKeytabFilePath(kerberosProperties
				.getSeaboxKeytabPath() + "/" + keytab.getKeytabFileName()));
		String seaboxParam = JSONArray.toJSONString(keytabAll);
		serversSeabox.forEach(server -> {
			String seaboxProxyHostIp = server.getHostPort();
			String updateKeytabUrl = "http://".concat(seaboxProxyHostIp)
					.concat("/seaboxKeytab/updateKeytab");
			JSONObject jo = RestTemplateUtil.restPost(restTemplate,
					updateKeytabUrl, seaboxParam);
			Result result = jo.toJavaObject(Result.class);
			if (result != null && 0 == result.getCode()) {
				log.info("成功同步到seabox模块");
			} else {
				log.error("同步到seabox模块失败：{}", result.getMsg());
				return;
			}
		});

		// 获取item服务
		ILoadBalancer loadBalancerItem = springClientFactory
				.getLoadBalancer(ServiceNameConstants.ITEM_CENTER);
		List<Server> serversItem = loadBalancerItem.getReachableServers();
		// 变更keytab文件路径
		keytabAll.forEach(keytab -> keytab.setKeytabFilePath(kerberosProperties
				.getItemKeytabPath() + "/" + keytab.getKeytabFileName()));
		String itemParam = JSONArray.toJSONString(keytabAll);
		serversItem.forEach(server -> {
			String itemProxyHostIp = server.getHostPort();
			String updateKeytabUrl = "http://".concat(itemProxyHostIp).concat(
					"/MC30/updateKeytab");
			JSONObject jo = RestTemplateUtil.restPost(restTemplate,
					updateKeytabUrl, itemParam);
			Result res = jo.toJavaObject(Result.class);
			if (res != null && 0 == res.getCode()) {
				log.info("成功同步到itemcenter模块");
			} else {
				log.error("同步到itemcenter模块失败：{}", res.getMsg());
				return;
			}
		});
	}

	/**
	 * 生成服务的keytab文件
	 * 
	 * @param clusterId
	 *            集群id
	 * @param keytabList
	 *            keytab集合
	 */
	public void generateKeytab(Integer clusterId,
			List<SdpServerKeytab> keytabList) {
		// 获取集群对应的kdc所在地址信息
		SdpsCluster sdpsCluster = clusterMapper
				.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
						clusterId));
		SdpsServerInfo serverInfo = serverInfoMapper
				.selectOne(new QueryWrapper<SdpsServerInfo>().eq("server_id",
						sdpsCluster.getServerId()).eq("type",
						ServerTypeEnum.KDC.name()));
		SysGlobalArgs sysGlobalArgs = sysGlobalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		String pass = RsaUtil.decrypt(serverInfo.getPasswd(),
				sysGlobalArgs.getArgValue());

		RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
				serverInfo.getHost(), serverInfo.getUser(), pass,
				Integer.valueOf(serverInfo.getPort()));
		for (SdpServerKeytab keytab : keytabList) {
			String keytabName = clusterId + "." + keytab.getKeytabFileName();
			if ("SERVICE".equals(keytab.getPrincipalType())) {
				keytabName = keytab.getHost() + "." + keytabName;
			}
			String keytabPath = kerberosProperties.getKdcKeytabPath() + "/"
					+ keytabName;
			String backKeytab = keytabPath + "." + sdf.format(new Date());
			try {
				// 将文件mv为其他名
				String mvCommand = mvRemoteFileCommand(keytabPath, backKeytab);
				remoteShellExecutorUtil.commonExec(mvCommand);
				// 生成新的keytab文件
				String command = "kadmin.local -q 'xst -k %s -norandkey %s'";

				command = String.format(command, keytabPath,
						keytab.getPrincipalName());
				int status = remoteShellExecutorUtil.commonExec(command);
				if (-1 == status) {
					throw new Exception("生成keytab失败");
				}
				// 生成成功后，删除备份文件
				keytab.setKeytabFilePath(keytabPath);
				keytab.setKeytabFileName(keytabName);

				String delCommand = "rm -f " + backKeytab;
				remoteShellExecutorUtil.commonExec(delCommand);
				Thread.sleep(50);
			} catch (Exception e) {
				log.error("生成keytab失败", e);
				// 将备份文件恢复
				String s = mvRemoteFileCommand(backKeytab, keytabPath);
				try {
					remoteShellExecutorUtil.commonExec(s);
				} catch (Exception e1) {
					log.error("恢复备份文件失败:{}", s, e1);
				}

			}

		}
		// 编辑keytab权限为644
		chmod(remoteShellExecutorUtil);
	}

	/**
	 * 编辑keytab权限为644
	 * 
	 * @param remoteShellExecutorUtil
	 */
	private void chmod(RemoteShellExecutorUtil remoteShellExecutorUtil) {
		// 编辑keytab权限为644
		String chmodCommand = "chmod 644 "
				+ kerberosProperties.getKdcKeytabPath() + "/*";
		try {
			remoteShellExecutorUtil.commonExec(chmodCommand);
		} catch (Exception e) {
			log.error("keytab文件修改权限异常", e);
		}
	}

	@Transactional
	public void updateKeytabInfo(List<SdpServerKeytab> list) {
		serverKeytabMapper.delete(new QueryWrapper<SdpServerKeytab>().gt(
				"cluster_id", 0));
		serverKeytabMapper.insertBatchSomeColumn(list);
	}

	/**
	 * 拼接mv命令
	 * 
	 * @param source
	 *            源文件
	 * @param target
	 *            目标文件
	 * @return
	 */
	public String mvRemoteFileCommand(String source, String target) {
		return "mv " + source + " " + target;
	}

	/**
	 * 根据集群和服务获取principal
	 * 
	 * @param sdpsCluster
	 *            集群信息
	 * @param serverName
	 *            服务名
	 * @return
	 */
	// public String getPrincipal(SdpsCluster sdpsCluster, String serverName) {
	// String principal = "";
	// List<String> confList = Lists.newArrayList();
	// switch (KeytabServerEnum.valueOf(serverName)) {
	// case HDFS:
	// confList.add("hadoop-env");
	// break;
	// case PHOENIX:
	// confList.add("hbase-site");
	// break;
	// case HBASE:
	// confList.add("hbase-env");
	// break;
	// default:
	// }
	// if (KeytabServerEnum.SPARK2.equals(KeytabServerEnum.valueOf(serverName)))
	// {
	// principal = "spark-" + sdpsCluster.getClusterName() +
	// kerberosProperties.getUserSuffix();
	// } else if
	// (KeytabServerEnum.HIVE.equals(KeytabServerEnum.valueOf(serverName))) {
	// principal =
	// itemDataSourceFegin.getHivePrincipal(sdpsCluster.getClusterId());
	// // principal = "hive/slave1@HADOOP.COM";
	// } else if
	// (KeytabServerEnum.PHOENIX.equals(KeytabServerEnum.valueOf(serverName))) {
	// Result componentAndHost =
	// bigdataCommonFegin.getComponentAndHost(sdpsCluster.getClusterId(),
	// KeytabServerEnum.HBASE.name());
	// LinkedHashMap<String, List> data = (LinkedHashMap<String, List>)
	// componentAndHost.getData();
	// List<LinkedHashMap<String, Object>> phoenixServer =
	// (List<LinkedHashMap<String, Object>>) data.get("PHOENIX_QUERY_SERVER");
	// String host = "";
	// for (LinkedHashMap<String, Object> param : phoenixServer) {
	// if (param.containsKey("host_name")) {
	// host = param.get("host_name").toString();
	// break;
	// }
	// }
	// principal = "HTTP/" + host + kerberosProperties.getUserSuffix();
	// } else {
	// String confJson =
	// seaboxProxyFegin.getServerConfByConfName(sdpsCluster.getClusterId(),
	// KeytabServerEnum.valueOf(serverName).name(), confList);
	// Map confMap = JSON.parseObject(confJson, Map.class);
	// switch (KeytabServerEnum.valueOf(serverName)) {
	// case HDFS:
	// principal = confMap.get("hdfs_principal_name").toString();
	// break;
	// case PHOENIX:
	// principal =
	// confMap.get("phoenix.queryserver.kerberos.principal").toString();
	// break;
	// case HBASE:
	// principal = confMap.get("hbase_principal_name").toString();
	// break;
	// default:
	// }
	// }
	// confList.clear();
	// return principal;
	// }
}
