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
	 * ?????????????????????????????????
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
			log.error("YarnUtil?????????????????????:", e);
		}
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param queueInfo
	 *            ??????????????????
	 * @param findQueueName
	 *            ????????????
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
			// ??????????????????
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
			log.error("??????Yarn????????????:", e);
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
				log.info("Yarn?????????????????????:" + findQueueName);
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
	 * ??????YARN??????
	 *
	 * @param queueName
	 *            ????????????
	 * @return true:????????????; false:????????????.
	 */
	public boolean deleteQueue(String queueName) {
		if (null == queueName || "".equals(queueName.trim()))
			return false;
		try {
			List<QueueInfo> rootQueueInfos = yc.getRootQueueInfos();
			Iterator<QueueInfo> queueInfoIterator = rootQueueInfos.iterator();
			while (queueInfoIterator.hasNext()) {
				QueueInfo queueInfo = queueInfoIterator.next();
				// ??????????????????????????????????????????????????????????????????
				if (queueInfo.getQueueName().equals(queueName)) {
					return rootQueueInfos.remove(queueInfo);
				}
			}
		} catch (Exception e) {
			log.error("??????????????????.", e);
		}
		return false;
	}

	/**
	 * ??????????????????????????????
	 *
	 * @return ???????????????json
	 */
	public JSONObject listScheduler(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		log.info("baseUrl:{}", baseUrl);
		String url = baseUrl + schedulerUrl;
		log.info("?????????????????????????????????url:{}", url);
		return processRequestAndResponse(url);
	}

	/**
	 * ????????????Get?????????????????????
	 *
	 * @param url
	 *            ????????????
	 * @return ??????????????????
	 */
	public JSONObject processRequestAndResponse(String url) {
		HttpEntity httpEntity = new HttpEntity(headers);
		ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, httpEntity, JSONObject.class);
		JSONObject jsonObject = responseEntity.getBody();
		log.info("????????????result:{}", jsonObject);
		return jsonObject;
	}

	/**
	 * ????????????PUT?????????????????????
	 *
	 * @param url
	 *            ????????????
	 * @return ??????????????????
	 */
	public JSONObject processRequestPUTAndResponse(String url,
			JSONObject jsonObject) {
		HttpEntity httpEntity = new HttpEntity(
				JSONObject.toJSONString(jsonObject), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.PUT, httpEntity, String.class);
		String result = responseEntity.getBody();
		log.info("????????????result:{}", jsonObject);
		return JSONObject.parseObject(result);
	}

	/**
	 * ????????????PUT??????
	 *
	 * @param url
	 *            ????????????
	 * @param jsonObject
	 *            ?????????
	 * @return ??????????????????
	 */
	public JSONObject processModifyQueueRequestAndResponse(String url,
			JSONObject jsonObject) {
		/**
		 * ????????????????????????????????????????????????????????????
		 */
		log.info("??????????????????jsonObject:{}", jsonObject);
		JSONObject modifyQueue = null;
		log.info("????????????modifyQueue:{}", modifyQueue);
		String jsonString = JSONObject.toJSONString(modifyQueue);
		HttpEntity<String> httpEntity = new HttpEntity(jsonString, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.PUT, httpEntity, String.class);
		String body = responseEntity.getBody();
		log.info("body:{}", body);
		return JSONObject.parseObject(body);
	}

	/**
	 * ??????clusterId??????yarn RM?????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ??????yarn???RM??????ip?????????
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
			throw new BusinessException("??????yarn????????????");
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
	 * ??????clusterId??????yarn RM?????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ??????yarn???RM??????ip?????????
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
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????json???
	 */
	public JSONObject listApps(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + appUrl;
		log.info("???????????????????????????url?????????:{}", url);
		JSONObject jsonObject = processRequestAndResponse(url);
		return jsonObject;
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????json???
	 */
	public JSONObject listApps(Integer clusterId, Map<String, Object> param) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + appUrl;
		// ????????????
		if (param != null && !param.isEmpty()) {
			List<String> paramList = Lists.newArrayList();
			Set<Map.Entry<String, Object>> entries = param.entrySet();
			entries.forEach(entry -> {
				paramList.add(entry.getKey() + "=" + entry.getValue());
			});
			String joinStr = StringUtils.join(paramList, "&");
			url += "?" + joinStr;
		}
		log.info("???????????????????????????url?????????:{}", url);
		JSONObject jsonObject = processRequestAndResponse(url);
		return jsonObject;
	}

	/**
	 * ????????????yarn????????????
	 *
	 * @param jsonObject
	 *            ???????????????????????????
	 * @return ??????????????????
	 */
	public JSONObject modifyQueue(Integer clusterId, JSONObject jsonObject) {
		String ambariBaseUrl = this.getAmbariBaseUrl(clusterId);
		String url = ambariBaseUrl + updateUrl;
		log.info("??????ambari-server???url???:{}", url);
		JSONObject responseJson = this.processModifyQueueRequestAndResponse(
				url, jsonObject);
		log.info("ambari-server??????????????????:{}", responseJson);
		return jsonObject;
	}

	/**
	 * ?????????YarnUtil?????????
	 *
	 * @param clusterId
	 *            ??????Id
	 */
	public void init(Integer clusterId) {
		try {
			YarnConfiguration yarnConfiguration = loadConf(clusterId);
			yc = YarnClient.createYarnClient();
			yc.init(yarnConfiguration);
			yc.start();

		} catch (Exception e) {
			log.error("YarnUtil????????????????????????:", e);
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ??????????????????
	 */
	public JSONObject listMetrics(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + metricsUrl;
		return processRequestAndResponse(url);
	}

	/**
	 * ????????????????????????
	 *
	 * @param clusterId
	 *            ??????id
	 * @return ??????????????????
	 */
	public JSONObject listNodes(Integer clusterId) {
		String baseUrl = getBaseUrl(clusterId);
		String url = baseUrl + nodesUrl;
		return processRequestAndResponse(url);
	}

	/**
	 * ??????ambari???saveAndRefresh??????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????????????????
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
	 * ??????ambari???saveAndRestart??????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @return ????????????????????????
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
	 * ?????????ambari??????
	 *
	 * @param clusterId
	 *            ??????ID
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
	 * ?????????ambari??????
	 *
	 * @param clusterId
	 *            ??????ID
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
	 * ??????????????????
	 *
	 * @param clusterId
	 *            ??????ID
	 * @param jsonObject
	 *            ????????????
	 * @return ????????????
	 */
	public JSONObject queueStartAndStop(Integer clusterId, JSONObject jsonObject) {
		String ambariBaseUrl = this.getAmbariBaseUrl(clusterId);
		String url = ambariBaseUrl + updateUrl;
		// log.info("??????ambari-server???url???:{}", url);
		JSONObject responseJson = this
				.processQueueStartAndStopRequestAndResponse(url, jsonObject);
		// log.info("ambari-server??????????????????:{}", responseJson);
		return jsonObject;
	}

	/**
	 * ????????????
	 *
	 * @param url
	 *            ?????????url
	 * @param jsonObject
	 *            ???????????????
	 * @return ???????????????
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
	 * ??????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @return ???????????????json???
	 */
	public List<ApplicationDTO> usedMemoryInfo(Integer clusterId, Integer topN,
			Long startTime, Long endTime) {
		// ????????????
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
		log.info("??????????????????:{}", list);
		return list;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @return ???????????????????????????
	 */
	public JSONObject listAppsByUser(Integer clusterId, String user) {
		String url = getBaseUrl(clusterId) + this.listAppsByUser + user;
		log.info("url:{}", url);
		return this.processRequestAndResponse(url);
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param states
	 *            ????????????
	 * @return ???????????????????????????
	 */
	public JSONObject listAppsByState(Integer clusterId, String[] states) {
		String s = null;
		for (String state : states) {
			s += state + ",";
		}
		log.info("states????????????:{}", s);
		String url = getBaseUrl(clusterId) + this.listAppsByStates + s;
		return this.processRequestAndResponse(url);
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param clusterId
	 *            ??????ID
	 * @param user
	 *            ??????
	 * @param states
	 *            ????????????
	 * @return ?????????????????????????????????
	 */
	public JSONObject listAppsByUserAndState(Integer clusterId, String user,
			String[] states) {
		String s = null;
		for (String state : states) {
			s += state + ",";
		}
		log.info("states????????????:{}", s);
		String url = getBaseUrl(clusterId) + this.listAppsByUserAndStates
				+ user + "&states=" + s;
		return this.processRequestAndResponse(url);
	}
}
