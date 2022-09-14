package com.seaboxdata.sdps.seaboxProxy.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.dto.ApplicationDTO;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.seaboxProxy.bean.QueuesObj;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsClusterMapper;

@Slf4j
@Component
public class YarnUtil {

	YarnClient yc;
	@Autowired
	BigdataVirtualHost bigdataVirtualHost;
	@Autowired
	@Qualifier("SeaboxRestTemplate")
	private RestTemplate restTemplate;
	@Value("${yarn.resourceManager.schedulerUrl}")
	private String schedulerUrl;
	@Value("${yarn.resourceManager.appUrl}")
	private String appUrl;
	@Value("${yarn.resourceManager.metricsUrl}")
	private String metricsUrl;
	@Value("${yarn.resourceManager.nodesUrl}")
	private String nodesUrl;
	@Value("${ambari.server.updateYarn.updateUrl}")
	private String updateUrl;
	@Value("${ambari.server.updateYarn.saveAndRefreshUrl}")
	private String saveAndRefreshUrl;
	@Value("${ambari.server.updateYarn.saveAndRestartUrl}")
	private String saveAndRestartUrl;
	@Value("${yarn.resourceManager.listAppsByUser}")
	private String listAppsByUser;
	@Value("${yarn.resourceManager.listAppsByStates}")
	private String listAppsByStates;
	@Value("${yarn.resourceManager.listAppsByUserAndStates}")
	private String listAppsByUserAndStates;

	private HttpHeaders headers;

	private String ambariBaseUrl;

	/**
	 * 在计算资源管理测试用。
	 */
	public YarnUtil() {
	}

	public void initKerberosENV(Configuration conf, SdpsCluster sdpsCluster,KerberosProperties kerberosProperties ) {
		System.setProperty("java.security.krb5.conf",
				kerberosProperties.getKrb5());
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation.loginUserFromKeytab(
					"hdfs-".concat(sdpsCluster.getClusterName()).concat(
							kerberosProperties.getUserSuffix()),
					kerberosProperties.getSeaboxKeytabPath().concat("/")
							.concat(sdpsCluster.getClusterId().toString())
							.concat(".hdfs.headless.keytab"));
		} catch (IOException e) {
			log.error("yarn kerberos login err", e);
		}
	}

	public YarnUtil(Integer clusterId) {
		try {
			YarnConfiguration yarnConfiguration = loadConf(clusterId);
			yarnConfiguration
					.setBoolean(
							CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
							true);
			SdpsClusterMapper clusterMapper = SpringUtil.getBean(SdpsClusterMapper.class);
			BigdataVirtualHost bigdataVirtualHost = SpringUtil.getBean(BigdataVirtualHost.class);
			KerberosProperties kerberosProperties = SpringUtil.getBean(KerberosProperties.class);
			SdpsCluster sdpsCluster = clusterMapper.selectById(clusterId);
			if (sdpsCluster.getKerberos() && kerberosProperties.getEnable()) {
				bigdataVirtualHost.setVirtualHost(clusterId);
				initKerberosENV(yarnConfiguration, sdpsCluster,kerberosProperties);
			}
			yc = YarnClient.createYarnClient();
			yc.init(yarnConfiguration);
			yc.start();

		} catch (Exception e) {
			log.error("YarnUtil工具类构造异常:", e);
		}
	}

	/**
	 * 根据队列名获取当前节点信息
	 *
	 * @param queueInfo
	 *            节点列表集合
	 * @param findQueueName
	 *            队列全名
	 * @return
	 */
	public static QueueInfo getQueueByQueueName(QueueInfo queueInfo,
			String findQueueName) {
		try {
			String queueName = queueInfo.getQueueName();
			if (findQueueName.equalsIgnoreCase(queueName)) {
				return queueInfo;
			} else {
				List<QueueInfo> queueList = queueInfo.getChildQueues();
				if (queueList.size() > 0) {
					for (QueueInfo childQueue : queueList) {
						QueueInfo childNodeObj = getQueueByQueueName(
								childQueue, findQueueName);
						if (childNodeObj != null) {
							return childNodeObj;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private YarnConfiguration loadConf(Integer clusterId) {
		YarnConfiguration yarnConf = new YarnConfiguration();
		try {
			// 加载集群配置
			ArrayList<String> confListHdfs = new ArrayList<>();
			confListHdfs.add("core-site");
			confListHdfs.add("hdfs-site");
			String confJsonHdfs = new AmbariUtil(clusterId)
					.getAmbariServerConfByConfName("HDFS", confListHdfs);
			Map confMap = JSON.parseObject(confJsonHdfs, Map.class);
			for (Object obj : confMap.entrySet()) {
				yarnConf.set(String.valueOf(((Map.Entry) obj).getKey()),
						String.valueOf(((Map.Entry) obj).getValue()));
			}

			ArrayList<String> confListYarn = new ArrayList<>();
			confListYarn.add("yarn-site");
			String confJsonYarn = new AmbariUtil(clusterId)
					.getAmbariServerConfByConfName("YARN", confListYarn);
			Map confMapYarn = JSON.parseObject(confJsonYarn, Map.class);
			for (Object obj : confMapYarn.entrySet()) {
				yarnConf.set(String.valueOf(((Map.Entry) obj).getKey()),
						String.valueOf(((Map.Entry) obj).getValue()));
			}
		} catch (Exception e) {
			log.error("加载Yarn配置异常:", e);
		}

		return yarnConf;
	}

	public YarnClient getYc() {
		return yc;
	}

	public void closeYc() {
		try {
			yc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public QueueInfo queryYarnQueue(String findQueueName) {
		QueueInfo findQueueInfo = null;
		try {
			List<QueueInfo> rootQueueInfos = yc.getRootQueueInfos();
			for (QueueInfo rootChildQueue : rootQueueInfos) {
				findQueueInfo = YarnUtil.getQueueByQueueName(rootChildQueue,
						findQueueName);
				if (findQueueInfo != null) {
					break;
				}
			}
			if (findQueueInfo == null) {
				log.info("Yarn队列中没有找到:" + findQueueName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return findQueueInfo;
	}

	public QueuesObj queueAllExtract() {
		QueuesObj queuesObj = new QueuesObj();
		try {
			queuesObj.setQueueName("root");
			ArrayList<QueuesObj> newQueuesList = new ArrayList<>();
			List<QueueInfo> rootQueueInfos = yc.getRootQueueInfos();
			for (QueueInfo rootQueueInfo : rootQueueInfos) {
				String oldQueueJson = JSONObject.toJSONString(rootQueueInfo);
				QueuesObj obj = JSON.parseObject(oldQueueJson, QueuesObj.class);
				newQueuesList.add(obj);
			}
			queuesObj.setChildQueues(newQueuesList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queuesObj;
	}

	/**
	 * 删除YARN队列
	 *
	 * @param queueName
	 *            队列名称
	 * @return true:删除成功; false:删除失败.
	 */
	public boolean deleteQueue(String queueName) {
		if (null == queueName || "".equals(queueName.trim()))
			return false;
		try {
			List<QueueInfo> rootQueueInfos = yc.getRootQueueInfos();
			Iterator<QueueInfo> queueInfoIterator = rootQueueInfos.iterator();
			while (queueInfoIterator.hasNext()) {
				QueueInfo queueInfo = queueInfoIterator.next();
				// 如果查询的队列名和传入的队列名匹配，则删除。
				if (queueInfo.getQueueName().equals(queueName)) {
					return rootQueueInfos.remove(queueInfo);
				}
			}
		} catch (Exception e) {
			log.error("删除队列失败.", e);
		}
		return false;
	}

	/**
	 * 查询集群队列信息列表
	 *
	 * @return 集群的队列json
	 */
	public JSONObject listScheduler(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		log.info("baseUrl:{}", baseUrl);
		String url = baseUrl + schedulerUrl;
		log.info("查询集群队列信息列表的url:{}", url);
		return processRequestAndResponse(url);
	}

	/**
	 * 处理发送Get请求及接受请求
	 *
	 * @param url
	 *            请求链接
	 * @return 返回请求结果
	 */
	public JSONObject processRequestAndResponse(String url) {
		HttpEntity httpEntity = new HttpEntity(headers);
		ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, httpEntity, JSONObject.class);
		JSONObject jsonObject = responseEntity.getBody();
		log.info("请求结果result:{}", jsonObject);
		return jsonObject;
	}

	/**
	 * 处理发送PUT请求及接受请求
	 *
	 * @param url
	 *            请求链接
	 * @return 返回请求结果
	 */
	public JSONObject processRequestPUTAndResponse(String url,
			JSONObject jsonObject) {
		HttpEntity httpEntity = new HttpEntity(
				JSONObject.toJSONString(jsonObject), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.PUT, httpEntity, String.class);
		String result = responseEntity.getBody();
		log.info("请求结果result:{}", jsonObject);
		return JSONObject.parseObject(result);
	}

	/**
	 * 处理发送PUT请求
	 *
	 * @param url
	 *            请求链接
	 * @param jsonObject
	 *            请求体
	 * @return 返回请求结果
	 */
	public JSONObject processModifyQueueRequestAndResponse(String url,
			JSONObject jsonObject) {
		/**
		 * 处理队列增删改的逻辑，需要前端传入队列。
		 */
		log.info("传入待修改的jsonObject:{}", jsonObject);
		JSONObject modifyQueue = null;
		log.info("修改后的modifyQueue:{}", modifyQueue);
		String jsonString = JSONObject.toJSONString(modifyQueue);
		HttpEntity<String> httpEntity = new HttpEntity(jsonString, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.PUT, httpEntity, String.class);
		String body = responseEntity.getBody();
		log.info("body:{}", body);
		return JSONObject.parseObject(body);
	}

	/**
	 * 根据clusterId查询yarn RM所在的节点信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 返回yarn的RM主机ip和端口
	 */
	public String getBaseUrl(Integer clusterId) {
		/*
		 * bigdataVirtualHost.setVirtualHost(clusterId); return
		 * yc.getConfig().get(YarnConfiguration.RM_WEBAPP_ADDRESS);
		 */
		BigdataCommonFegin commonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		// SdpsYarnInfo sdpsYarnInfo = commonFegin.selectYarnInfo(clusterId);
		Result<SdpsServerInfo> result = commonFegin.selectServerInfo(
				Long.valueOf(clusterId), ServerTypeEnum.Y.name());
		if (result.isFailed()) {
			throw new BusinessException("获取yarn信息失败");
		}
		SdpsServerInfo sdpsServerInfo = result.getData();
		return BigDataConfConstants.HTTP_URL_PREFIX + sdpsServerInfo.getHost()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ sdpsServerInfo.getPort();
	}

	public String getBaseUrl() {
		String ipAndPort = yc.getConfig().get(
				YarnConfiguration.RM_WEBAPP_ADDRESS);
		return BigDataConfConstants.HTTP_URL_PREFIX + ipAndPort;
	}

	/**
	 * 根据clusterId查询yarn RM所在的节点信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 返回yarn的RM主机ip和端口
	 */
	public String getAmbariBaseUrl(Integer clusterId) {
		BigdataCommonFegin commonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		// SdpsServerInfo sdpsServerInfo =
		// commonFegin.selectAmbariInfo(clusterId);
		Result<SdpsServerInfo> result = commonFegin.selectAmbariInfo(clusterId,
				ServerTypeEnum.A.name());
		SdpsServerInfo serverInfo = result.getData();
		return BigDataConfConstants.HTTP_URL_PREFIX + serverInfo.getHost()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ serverInfo.getPort();
	}

	/**
	 * 查询集群任务列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 任务列表json串
	 */
	public JSONObject listApps(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + appUrl;
		log.info("请求集群任务列表的url地址为:{}", url);
		JSONObject jsonObject = processRequestAndResponse(url);
		return jsonObject;
	}

	/**
	 * 查询集群任务列表
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 任务列表json串
	 */
	public JSONObject listApps(Integer clusterId, Map<String, Object> param) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + appUrl;
		// 拼接参数
		if (param != null && !param.isEmpty()) {
			List<String> paramList = Lists.newArrayList();
			Set<Map.Entry<String, Object>> entries = param.entrySet();
			entries.forEach(entry -> {
				paramList.add(entry.getKey() + "=" + entry.getValue());
			});
			String joinStr = StringUtils.join(paramList, "&");
			url += "?" + joinStr;
		}
		log.info("请求集群任务列表的url地址为:{}", url);
		JSONObject jsonObject = processRequestAndResponse(url);
		return jsonObject;
	}

	/**
	 * 更新所有yarn队列参数
	 *
	 * @param jsonObject
	 *            所有队列的参数列表
	 * @return 是否更新成功
	 */
	public JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject) {
		String ambariBaseUrl = this.getAmbariBaseUrl(clusterId);
		String url = ambariBaseUrl + updateUrl;
		log.info("请求ambari-server的url为:{}", url);
		JSONObject responseJson = this.processModifyQueueRequestAndResponse(
				url, jsonObject);
		log.info("ambari-server返回的结果为:{}", responseJson);
		return jsonObject;
	}

	/**
	 * 初始化YarnUtil工具类
	 *
	 * @param clusterId
	 *            集群Id
	 */
	public void init(Integer clusterId) {
		try {
			YarnConfiguration yarnConfiguration = loadConf(clusterId);
			yc = YarnClient.createYarnClient();
			yc.init(yarnConfiguration);
			yc.start();

		} catch (Exception e) {
			log.error("YarnUtil工具类初始化异常:", e);
		}
	}

	/**
	 * 查询集群指标信息
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 集群指标信息
	 */
	public JSONObject listMetrics(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + metricsUrl;
		return processRequestAndResponse(url);
	}

	/**
	 * 查询集群节点信息
	 *
	 * @param clusterId
	 *            集群id
	 * @return 集群节点信息
	 */
	public JSONObject listNodes(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + nodesUrl;
		return processRequestAndResponse(url);
	}

	/**
	 * 调用ambari的saveAndRefresh方法
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 新的集群信息数据
	 */
	public JSONObject saveAndRefresh(Integer clusterId) {
		JSONObject requestJson = new JSONObject();
		requestJson.put("save", "true");
		log.info("requestJson:{}", requestJson.toJSONString());
		String baseurl = this.getAmbariBaseUrl(clusterId);
		String url = baseurl + saveAndRefreshUrl;
		log.info("saveAndRefreshUrl:{}", url);
		return processRequestPUTAndResponse(url, requestJson);
	}

	/**
	 * 调用ambari的saveAndRestart方法
	 *
	 * @param clusterId
	 *            集群ID
	 * @return 新的集群信息数据
	 */
	public JSONObject saveAndRestart(Integer clusterId) {
		JSONObject requestJson = new JSONObject();
		requestJson.put("save", "true");
		log.info("requestJson:{}", requestJson.toJSONString());
		String baseurl = this.getAmbariBaseUrl(clusterId);
		String url = baseurl + saveAndRestartUrl;
		log.info("saveAndRestartUrl:{}", url);
		return processRequestPUTAndResponse(url, requestJson);
	}

	/**
	 * 初始化ambari组件
	 *
	 * @param clusterId
	 *            集群ID
	 */
	public void initAmbari(Integer clusterId) {

		BigdataCommonFegin commonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		// SdpsServerInfo sdpsServerInfo =
		// commonFegin.selectAmbariInfo(clusterId);
		Result<SdpsServerInfo> result = commonFegin.selectAmbariInfo(clusterId,
				ServerTypeEnum.A.name());
		SdpsServerInfo serverInfo = result.getData();
		ambariBaseUrl = BigDataConfConstants.HTTP_URL_PREFIX
				+ serverInfo.getHost()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ serverInfo.getPort();

		String plainCreds = serverInfo.getUser()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ serverInfo.getPasswd();
		System.out.println("ambariBaseUrl:" + ambariBaseUrl);
		System.out.println("plainCreds:" + plainCreds);
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		// headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Content-Type", "text/plain; charset=UTF-8");
		headers.set("X-Requested-By", "view-capacity-scheduler");
		headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
	}

	/**
	 * 初始化ambari组件
	 *
	 * @param clusterId
	 *            集群ID
	 */
	public void initAmbariWithStartAndStop(Integer clusterId) {

		BigdataCommonFegin commonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		// SdpsServerInfo sdpsServerInfo =
		// commonFegin.selectAmbariInfo(clusterId);
		Result<SdpsServerInfo> result = commonFegin.selectAmbariInfo(clusterId,
				ServerTypeEnum.A.name());
		SdpsServerInfo serverInfo = result.getData();
		ambariBaseUrl = BigDataConfConstants.HTTP_URL_PREFIX
				+ serverInfo.getHost()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ serverInfo.getPort();

		String plainCreds = serverInfo.getUser()
				+ BigDataConfConstants.HTTP_URL_SEPARATOR
				+ serverInfo.getPasswd();
		System.out.println("ambariBaseUrl:" + ambariBaseUrl);
		System.out.println("plainCreds:" + plainCreds);
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		// headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Content-Type", "application/json; charset=UTF-8");
		headers.set("X-Requested-By", "view-capacity-scheduler");
		headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
	}

	/**
	 * 队列启停接口
	 *
	 * @param clusterId
	 *            集群ID
	 * @param jsonObject
	 *            传入参数
	 * @return 返回数据
	 */
	public JSONObject queueStartAndStop(Integer clusterId, JSONObject jsonObject) {
		String ambariBaseUrl = this.getAmbariBaseUrl(clusterId);
		String url = ambariBaseUrl + updateUrl;
		// log.info("请求ambari-server的url为:{}", url);
		JSONObject responseJson = this
				.processQueueStartAndStopRequestAndResponse(url, jsonObject);
		// log.info("ambari-server返回的结果为:{}", responseJson);
		return jsonObject;
	}

	/**
	 * 队列启停
	 *
	 * @param url
	 *            请求的url
	 * @param jsonObject
	 *            请求的参数
	 * @return 返回的数据
	 */
	private JSONObject processQueueStartAndStopRequestAndResponse(String url,
			JSONObject jsonObject) {
		String jsonString = JSONObject.toJSONString(jsonObject);
		if (StringUtils.isEmpty(jsonString)) {
			return null;
		}
		String queueName = jsonString.substring(2, jsonString.length() - 2)
				.split(":")[0];
		queueName = queueName.substring(0, queueName.length() - 1);
		String queueState = jsonObject.getString(queueName);
		String queueStateOriginal = jsonObject.getString(queueName);
		/*
		 * if (queueState.equals(QueueState.QUEUE_STATE_RUNNING)) queueState =
		 * QueueState.QUEUE_STATE_STOPPED; else queueState =
		 * QueueState.QUEUE_STATE_RUNNING;
		 */
		JSONObject requestJson = new JSONObject();
		requestJson.put(queueName, queueState);
		// log.info("requestJson:{}", requestJson);
		HttpEntity<String> httpEntity = new HttpEntity(
				JSONObject.toJSONString(requestJson), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.PUT, httpEntity, String.class);
		String body = responseEntity.getBody();
		// log.info("body:{}", body);
		return JSONObject.parseObject(body);
	}

	/**
	 * 使用资源排行
	 * 
	 * @param clusterId
	 *            集群ID
	 * @return 使用资源的json串
	 */
	public List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN,
			Long startTime, Long endTime) {
		// 封装参数
		Map<String, Object> param = Maps.newHashMap();
		if (startTime != null && endTime != null) {
			param.put("startedTimeBegin", startTime);
			param.put("startedTimeEnd", endTime);
		}

		JSONObject jsonObject = listApps(clusterId, param);
		JSONArray jsonArray = jsonObject.getJSONObject("apps").getJSONArray(
				"app");
		List<ApplicationDTO> list = Arrays
				.stream(jsonArray.toArray())
				.map(jo -> {
					LinkedHashMap map = (LinkedHashMap) jo;
					Long memorySeconds = Long.parseLong(map
							.get("memorySeconds").toString());
					String queue = map.get("queue").toString();
					Object amObj = map.get("amHostHttpAddress");
					String amHostHttpAddress = amObj == null ? "" : amObj
							.toString();
					String applicationType = map.get("applicationType")
							.toString();
					String id = map.get("id").toString();
					String name = map.get("name").toString();
					return new ApplicationDTO(id, name, queue, applicationType,
							amHostHttpAddress, memorySeconds);
				})
				.sorted(Comparator.comparing(ApplicationDTO::getMemorySeconds)
						.reversed()).limit(topN).collect(Collectors.toList());
		log.info("使用资源排行:{}", list);
		return list;
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
	public JSONObject listAppsByUser(Integer clusterId, String user) {
		String url = getBaseUrl(clusterId) + this.listAppsByUser + user;
		log.info("url:{}", url);
		return this.processRequestAndResponse(url);
	}

	/**
	 * 根据任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param states
	 *            任务状态
	 * @return 该状态下的任务列表
	 */
	public JSONObject listAppsByState(Integer clusterId, String[] states) {
		String s = null;
		for (String state : states) {
			s += state + ",";
		}
		log.info("states的状态为:{}", s);
		String url = getBaseUrl(clusterId) + this.listAppsByStates + s;
		return this.processRequestAndResponse(url);
	}

	/**
	 * 根据用户和任务状态筛选任务列表
	 * 
	 * @param clusterId
	 *            集群ID
	 * @param user
	 *            用户
	 * @param states
	 *            任务状态
	 * @return 该综合状态下的任务列表
	 */
	public JSONObject listAppsByUserAndState(Integer clusterId, String user,
			String[] states) {
		String s = null;
		for (String state : states) {
			s += state + ",";
		}
		log.info("states的状态为:{}", s);
		String url = getBaseUrl(clusterId) + this.listAppsByUserAndStates
				+ user + "&states=" + s;
		return this.processRequestAndResponse(url);
	}
}
