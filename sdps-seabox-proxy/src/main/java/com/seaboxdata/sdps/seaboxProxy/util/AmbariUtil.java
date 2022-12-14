package com.seaboxdata.sdps.seaboxProxy.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.constant.DurationConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.ambari.ConfigGroup;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.seaboxProxy.bean.ServerKerberos;
import com.seaboxdata.sdps.seaboxProxy.bean.cluster.Configuration;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.mapper.SysGlobalArgsMapper;

@Slf4j
@Data
public class AmbariUtil {
	private String url;

	private Integer port;

	private String path;

	private HttpHeaders headers;

	private String clusterName;

	private String password;

	private Integer clusterId;

	private RestTemplate restTemplate = SpringBeanUtil.getBean(
			"SeaboxRestTemplate", RestTemplate.class);

	public static String getDecryptPassword(String pass) {
		SysGlobalArgs sysGlobalArgs = SpringBeanUtil.getBean(
				SysGlobalArgsMapper.class).selectOne(
				new QueryWrapper<SysGlobalArgs>().eq("arg_type", "password")
						.eq("arg_key", "privateKey"));
		return RsaUtil.decrypt(pass, sysGlobalArgs.getArgValue());
	}

	public void changeLoginUser(String username) {
		BigdataCommonFegin bigdataCommonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		SdpsServerInfo serverInfo = bigdataCommonFegin.getServerInfo(username);
		if (Objects.nonNull(serverInfo)) {
			String password = serverInfo.getPasswd();
			String plainCreds = username + ":" + getDecryptPassword(password);
			byte[] plainCredsBytes = plainCreds.getBytes();
			byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
			String base64Creds = new String(base64CredsBytes);
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		}
	}

	public AmbariUtil(Integer clusterId) {
		this.clusterId = clusterId;
		BigdataCommonFegin bigdataCommonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		SdpsServerInfo sdpsServerInfo = bigdataCommonFegin
				.queryClusterServerInfo(clusterId, ServerTypeEnum.A.name());
		url = "http://" + sdpsServerInfo.getHost() + ":"
				+ sdpsServerInfo.getPort();
		password = getDecryptPassword(sdpsServerInfo.getPasswd());
		String plainCreds = sdpsServerInfo.getUser() + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Requested-By", "ambari");

		clusterName = getClusterName();
	}

	public static JSONObject analysisYarnAndHdfsInfo(JSONObject jsonObject) {
		if (Objects.isNull(jsonObject)) {
			return new JSONObject();
		}
		final JSONObject jsonObj = new JSONObject();
		JSONArray serviceComponentInfos = jsonObject.getJSONArray("items");
		serviceComponentInfos
				.stream()
				.filter(info -> {
					String serviceName = ((JSONObject) info).getJSONObject(
							"ServiceComponentInfo").getString("component_name");
					return (StrUtil.equals(serviceName, "NAMENODE", true)
							|| StrUtil.equals(serviceName, "RESOURCEMANAGER",
									true) || StrUtil.equals(serviceName,
							"DATANODE"));
				})
				.forEach(
						info -> {
							String serviceName = ((JSONObject) info)
									.getJSONObject("ServiceComponentInfo")
									.getString("component_name");

							JSONArray hostComponents = ((JSONObject) info)
									.getJSONArray("host_components");
							if (StrUtil.containsIgnoreCase(serviceName,
									"RESOURCEMANAGER")) {
								analysisYarn(hostComponents, jsonObj);
							}
							if (StrUtil.containsIgnoreCase(serviceName,
									"NAMENODE")) {
								analysisNameNode(hostComponents, jsonObj);
							}
							if (StrUtil.containsIgnoreCase(serviceName,
									"DATANODE")) {
								analysisDataNode(hostComponents, jsonObj);
							}

						});
		return jsonObj;
	}

	private static void analysisDataNode(JSONArray source, JSONObject result) {
		final JSONArray array = new JSONArray();
		source.forEach(obj -> {
			JSONObject jsonNObject = ((JSONObject) obj)
					.getJSONObject("metrics");
			if (CollUtil.isNotEmpty(jsonNObject)) {
				array.add(jsonNObject.getJSONObject("dfs").getJSONObject(
						"FSNamesystem"));
			}
		});
		result.put("datanode", array);
	}

	private static void analysisNameNode(JSONArray source, JSONObject result) {
		JSONObject nameNodeObj = source.getJSONObject(0);
		result.put("namenode", nameNodeObj.getJSONObject("metrics")
				.getJSONObject("dfs").getJSONObject("FSNamesystem"));
	}

	private static void analysisYarn(JSONArray source, JSONObject result) {
		JSONObject yarnObj = source.getJSONObject(0);
		result.put("yarn",
				yarnObj.getJSONObject("metrics").getJSONObject("yarn"));
	}

	public static JSONObject analysisServiceUsersAndGroups(
			JSONObject jsonObject, JSONObject ambariResult) {
		if (Objects.isNull(jsonObject)) {
			return new JSONObject();
		}
		JSONArray items = jsonObject.getJSONArray("items");
		JSONArray resultArray = new JSONArray();
		items.stream()
				.forEach(
						item -> {
							((JSONObject) item)
									.getJSONArray("configurations")
									.parallelStream()
									.filter(conf -> {
										JSONArray propertyName = ((JSONObject) conf)
												.getJSONObject(
														"StackConfigurations")
												.getJSONArray("property_type");
										return ((CollUtil.contains(
												propertyName, "USER") || CollUtil
												.contains(propertyName, "GROUP")) && (StrUtil
												.isNotBlank(((JSONObject) conf)
														.getJSONObject(
																"StackConfigurations")
														.getString(
																"property_display_name"))));
									}).forEach(obj -> {
										resultArray.add(obj);
									});
						});
		JSONArray amabriArray = ambariResult.getJSONArray("configurations");
		amabriArray
				.stream()
				.filter(conf -> {
					JSONArray propertyName = ((JSONObject) conf).getJSONObject(
							"StackLevelConfigurations").getJSONArray(
							"property_type");
					return ((CollUtil.contains(propertyName, "USER") || CollUtil
							.contains(propertyName, "GROUP")) && (StrUtil
							.isNotBlank(((JSONObject) conf).getJSONObject(
									"StackLevelConfigurations").getString(
									"property_display_name"))));
				})
				.forEach(
						obj -> {
							JSONObject data = (JSONObject) obj;
							data.put("StackConfigurations", data
									.getJSONObject("StackLevelConfigurations"));
							data.put("StackLevelConfigurations", null);
							resultArray.add(obj);
						});
		JSONObject result = new JSONObject();
		result.put("items", resultArray);
		return result;
	}

	/**
	 * ??????http??????
	 *
	 * @param url
	 * @param httpMethod
	 * @param header
	 * @param restTemplate
	 * @param data
	 * @return
	 */
	public static JSONObject request(String url, HttpMethod httpMethod,
			HttpHeaders header, RestTemplate restTemplate, String data) {
		JSONObject result = new JSONObject();
		log.info("??????url={},headers={}", url, header);
		HttpEntity<Object> httpEntity;
		if (httpMethod == HttpMethod.POST) {
			httpEntity = new HttpEntity<>(data, header);
		} else {
			httpEntity = new HttpEntity<>(header);
		}
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				httpMethod, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	/**
	 * ???double??????????????????????????????
	 *
	 * @param number
	 * @return
	 */
	public static String getRatio(Double number) {
		// ?????????????????????????????????
		NumberFormat numberFormat = NumberFormat.getInstance();
		// ????????????????????????
		numberFormat.setMaximumFractionDigits(-1);
		return numberFormat.format(number * 100) + "%";
	}

	public static JSONObject analysisServiceAutoStart(JSONObject jsonObject,
			JSONObject display) {
		if (Objects.isNull(jsonObject) || Objects.isNull(display)) {
			return new JSONObject();
		}
		JSONArray items = jsonObject.getJSONArray("items");
		Map<String, List<Object>> groups = items.stream().collect(
				Collectors.groupingBy(item -> {
					JSONObject propertyName = ((JSONObject) item)
							.getJSONObject("ServiceComponentInfo");
					return propertyName.getString("service_name");
				}));
		JSONObject displayJson = analysisServiceDisplayName(display);
		JSONObject result = new JSONObject();
		groups.forEach((k, v) -> {
			JSONArray array = new JSONArray();
			v.forEach(obj -> array.add(obj));
			result.put(displayJson.getString(k), array);
		});
		return result;
	}

	public static JSONObject analysisServiceDisplayName(JSONObject jsonObject) {
		if (Objects.isNull(jsonObject)) {
			return new JSONObject();
		}
		JSONArray displayItems = jsonObject.getJSONArray("items");
		JSONObject result = new JSONObject();
		displayItems.stream().forEach(
				name -> {
					JSONObject service = ((JSONObject) name)
							.getJSONObject("StackServices");
					result.put(service.getString("service_name"),
							service.getString("display_name"));
				});
		return result;
	}

	public static String replaceStr(CharSequence str, String regex,
			Map<Integer, Object> map) {
		if (StrUtil.isEmpty(str)) {
			return StrUtil.str(str);
		}
		final Matcher matcher = Pattern.compile(regex).matcher(str);
		final StringBuffer buffer = new StringBuffer();
		int i = 1;
		while (matcher.find()) {
			try {
				matcher.appendReplacement(buffer, map.get(i).toString());
			} catch (Exception e) {

			}
			i++;
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public static JSONObject analysisServiceInstalled(JSONObject jsonObject) {
		if (Objects.isNull(jsonObject)) {
			return new JSONObject();
		}
		JSONObject installedJson = jsonObject.getJSONObject("Clusters")
				.getJSONObject("desired_service_config_versions");
		Set<String> installedSet = installedJson.keySet();
		JSONArray jsonArray = new JSONArray();
		installedSet.forEach(str -> jsonArray.add(str));
		JSONObject result = new JSONObject();
		result.put("installed", jsonArray);
		return result;
	}

	public static JSONObject analysisStackAndVersions(JSONObject jsonObject,
			JSONObject installedResult, JSONObject statusJson,
			JSONObject categoryResult) {
		JSONArray installedArray = analysisServiceInstalled(installedResult)
				.getJSONArray("installed");
		Map<String, String> statusMap = MapUtil.newHashMap();
		statusJson
				.getJSONArray("items")
				.stream()
				.forEach(
						item -> {
							JSONObject data = (JSONObject) item;
							JSONObject obj = data.getJSONObject("ServiceInfo");
							statusMap.put(obj.getString("service_name"),
									obj.getString("state"));
						});

		JSONArray categroyItems = categoryResult.getJSONArray("items");
		Map<String, List<Object>> categoryGroupMap = categroyItems.stream()
				.collect(
						Collectors.groupingBy(item -> ((JSONObject) item)
								.getJSONObject("ServiceComponentInfo")
								.getString("service_name")));
		Map<String, String> categoryMap = MapUtil.newHashMap();
		categoryGroupMap.forEach((k, v) -> {
			Set<String> categorySet = v
					.stream()
					.map(obj -> ((JSONObject) obj).getJSONObject(
							"ServiceComponentInfo").getString("category"))
					.collect(Collectors.toSet());
			if (categorySet.contains("MASTER")) {
				categoryMap.put(k, "MASTER");
			} else if (categorySet.contains("SLAVE")) {
				categoryMap.put(k, "SLAVE");
			} else if (categorySet.contains("CLIENT")) {
				categoryMap.put(k, "CLIENT");
			}
		});
		JSONArray items = jsonObject.getJSONArray("items");
		items.forEach(item -> {
			JSONObject data = (JSONObject) item;
			data.getJSONObject("ClusterStackVersions").put(
					"repository_summary", null);
			data.getJSONArray("repository_versions").getJSONObject(0)
					.getJSONObject("RepositoryVersions")
					.getJSONArray("stack_services").parallelStream()
					.forEach(obj -> {
						JSONObject service = (JSONObject) obj;
						String name = service.getString("name");
						if (installedArray.contains(name)) {
							service.put("installed", true);
							service.put("status", statusMap.get(name));
							service.put("category", categoryMap.get(name));
						} else {
							service.put("installed", false);
						}

					});
		});
		return jsonObject;
	}

	public static JSONObject analysisClusterName(JSONObject clusterResult) {
		JSONObject clusterInfo = clusterResult.getJSONArray("items")
				.getJSONObject(0);
		clusterInfo.put("href", null);
		return clusterInfo;
	}

	public static JSONObject analysisComponentInfo(JSONObject jsonObject) {
		JSONArray items = jsonObject.getJSONArray("items");
		Map<String, List<Object>> componentsMap = items.stream().collect(
				Collectors.groupingBy(item -> ((JSONObject) item)
						.getJSONObject("ServiceComponentInfo").getString(
								"service_name")));
		JSONObject result = new JSONObject();
		componentsMap.forEach((k, v) -> result.put(k, v));
		return result;
	}

	public String getClusterName() {
		BigdataCommonFegin bigdataCommonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
				"ambari", "clusterName");
		JSONObject result = getAmbariApi(args.getData().getArgValue(), args
				.getData().getArgValueDesc());
		JSONObject clusterJson = analysisClusterName(result);
		return clusterJson.getJSONObject("Clusters").getString("cluster_name");
	}

	private HttpMethod getRequestMode(String method) {
		HttpMethod httpMethod = null;
		switch (method) {
		case "GET":
			httpMethod = HttpMethod.GET;
			break;
		case "POST":
			httpMethod = HttpMethod.POST;
			break;
		case "PUT":
			httpMethod = HttpMethod.PUT;
			break;
		case "DELETE":
			httpMethod = HttpMethod.DELETE;
			break;
		default:
			httpMethod = HttpMethod.GET;
			break;
		}
		return httpMethod;
	}

	private String appendTimeStamp(String path) {
		String newUrl = url;
		if (StrUtil.isNotBlank(path) && StrUtil.contains(path, "?")) {
			newUrl = url.concat(path).concat("&_=")
					.concat(String.valueOf(DateUtil.date().getTime()));
		} else {
			newUrl = url.concat(path).concat("?_=")
					.concat(String.valueOf(DateUtil.date().getTime()));
		}
		return newUrl;
	}

	public JSONObject getAmbariApi(String path, String requestMode) {
		String newUrl = appendTimeStamp(path);
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("??????url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
			throw new BusinessException("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public JSONObject getAmbariApi(String path, String requestMode, String data) {
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
		log.info("??????url={},httpEntity={}", path, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(path,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
			throw new BusinessException("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public JSONObject getAmbariApi(String path, String requestMode,
			String data, boolean isAppendTimeStamp, Map<Integer, Object> param) {
		path = isAppendTimeStamp ? appendTimeStamp(path) : url.concat(path);
		path = replaceStr(path, "\\{\\}", param);
		return getAmbariApi(path, requestMode, data);
	}

	public JSONObject getAmbariApi(String path, String requestMode,
			String data, boolean isAppendTimeStamp) {
		path = isAppendTimeStamp ? appendTimeStamp(path) : url.concat(path);
		return getAmbariApi(path, requestMode, data);
	}

	public JSONObject getAmbariApi(String path, String requestMode,
			String data, Map<Integer, Object> param) {
		String newUrl = appendTimeStamp(path);
		newUrl = replaceStr(newUrl, "\\{\\}", param);
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
		log.info("??????url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
			throw new BusinessException("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public JSONObject getAmbariApi(String path, String requestMode,
			Map<Integer, Object> param) {
		String newUrl = appendTimeStamp(path);
		newUrl = replaceStr(newUrl, "\\{\\}", param);
		JSONObject result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("??????url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	public String getAmbariApiString(String path, String requestMode,
			Map<Integer, Object> param) {
		String newUrl = appendTimeStamp(path);
		newUrl = replaceStr(newUrl, "\\{\\}", param);
		String result = null;
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		log.info("??????url={},httpEntity={}", newUrl, httpEntity);
		ResponseEntity<String> responseEntity = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
		} else {
			result = responseEntity.getBody();
		}
		log.info("result={}", result);
		return result;
	}

	/**
	 * ??????AmbariApi??????
	 *
	 * @param path
	 *            url
	 * @param requestMode
	 *            ????????????
	 * @param data
	 *            body??????
	 * @param param
	 *            url??? {} ????????????
	 * @param isAppendTime
	 *            ????????????????????????
	 */
	public JSONObject getAmbariApi(String path, String requestMode,
			String data, Map<Integer, Object> param, boolean isAppendTime) {
		// ?????? isAppendTime ???????????????
		path = isAppendTime ? appendTimeStamp(path) : url + path;
		path = replaceStr(path, "\\{\\}", param);

		JSONObject result = new JSONObject();
		HttpEntity<Object> httpEntity;
		httpEntity = StringUtils.isNotBlank(data) ? new HttpEntity<>(data,
				headers) : new HttpEntity<>(headers);
		log.info("??????url={},httpEntity={}", path, httpEntity);

		ResponseEntity<String> responseEntity = restTemplate.exchange(path,
				getRequestMode(requestMode), httpEntity, String.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			if (JSONUtil.isJson(responseEntity.getBody())) {
				result = JSONObject.parseObject(responseEntity.getBody());
				result = result != null ? result : new JSONObject();
			} else {
				result.put("ambariData", responseEntity.getBody());
			}
		} else {
			log.error("??????ambari??????????????????");
			throw new BusinessException("??????ambari??????????????????");
		}
		log.info("result={}", result);
		return result;
	}

	/**
	 * ??????????????????????????????????????????
	 */
	public String getAmbariServerConfByConfName(String serverName,
			List<String> confNames) {
		String newUrl = url.concat("/api/v1/clusters/").concat(clusterName)
				.concat("/configurations/service_config_versions")
				.concat("?service_name.in(").concat(serverName)
				.concat(")&group_name=Default&is_current=true&fields=*")
				.concat("&_=")
				.concat(String.valueOf(DateUtil.date().getTime()));
		JSONObject apiResult = request(newUrl, HttpMethod.GET, headers,
				restTemplate, null);
		String configurationsStr = apiResult.get("items").toString();
		List<Map> itemsList = JSON.parseArray(configurationsStr, Map.class);
		// ???????????????
		HashMap<String, String> resultMap = new HashMap<>();
		if (itemsList.size() > 0) {
			for (Map map : itemsList) {
				for (Object m : map.entrySet()) {
					boolean bool = String.valueOf(((HashMap.Entry) m).getKey())
							.equalsIgnoreCase("configurations");
					if (bool) {
						List<Configuration> configurations = JSON.parseArray(
								String.valueOf(((HashMap.Entry) m).getValue()),
								Configuration.class);
						// ??????[configurations]???[confNames]????????????
						for (String confName : confNames) {
							for (Configuration configuration : configurations) {
								boolean isMatch = configuration.getType()
										.equalsIgnoreCase(confName);
								if (isMatch) {
									Map<String, String> propertiesMap = configuration
											.getProperties();
									resultMap.putAll(propertiesMap);
									// ??????????????????
									break;
								}
							}
						}
						break;
					}
				}
			}
		} else {
			// ?????????????????????????????????
			log.info("serverName:[" + serverName + "];confNames:["
					+ confNames.toString() + "]?????????????????????????????????");
			return "";
		}
		return JSON.toJSONString(resultMap);
	}

	/**
	 * ????????????????????????
	 *
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
	public JSONObject getServiceConfigVersions(Integer page_size, Integer from,
			String sortBy, String service_name, String createtime) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/configurations/service_config_versions?");
		// ????????????
		urlBuilder
				.append("page_size=")
				.append(page_size)
				.append("&from=")
				.append(from)
				.append("&sortBy=")
				.append(sortBy)
				.append("&fields=service_config_version,user,group_id,group_name,is_current,createtime,service_name,hosts,service_config_version_note,is_cluster_compatible,stack_id")
				.append("&minimal_response=true");
		// ???????????????
		if (StringUtils.isNotBlank(service_name)) {
			urlBuilder.append("&service_name=").append(service_name);
		}
		// ????????????????????????
		long currentTime = System.currentTimeMillis();
		long durationMillis = 0L;
		if (createtime != null) {
			switch (createtime) {
			case DurationConstant.ONE_HOUR:
				durationMillis = 60 * 60 * 1000L;
				break;
			case DurationConstant.ONE_DAY:
				durationMillis = 24 * 60 * 60 * 1000L;
				break;
			case DurationConstant.TWO_DAY:
				durationMillis = 2 * 24 * 60 * 60 * 1000L;
				break;
			case DurationConstant.ONE_WEEK:
				durationMillis = 7 * 24 * 60 * 60 * 1000L;
				break;
			case DurationConstant.ONE_MONTH:
				durationMillis = 30 * 24 * 60 * 60 * 1000L;
				break;
			}
			urlBuilder.append("&createtime>").append(
					currentTime - durationMillis);
		}
		// ?????????????????????
		urlBuilder.append("&_=").append(currentTime);
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getComponentAndHost(String serviceName) {
		JSONObject result = new JSONObject();
		JSONObject componentJson = getComponentByService(clusterName,
				serviceName);
		// ??????componentJson
		JSONArray componentItems = componentJson.getJSONArray("items");
		for (int i = 0; i < componentItems.size(); i++) {
			JSONObject componentItem = componentItems.getJSONObject(i);
			JSONObject serviceComponentInfo = componentItem
					.getJSONObject("ServiceComponentInfo");
			result.put(serviceComponentInfo.getString("component_name"),
					new JSONArray());
		}

		JSONObject hostJson = getHostsByService(clusterName, serviceName);
		// ??????hostJson
		JSONArray hostItems = hostJson.getJSONArray("items");
		for (int i = 0; i < hostItems.size(); i++) {
			JSONObject hostItem = hostItems.getJSONObject(i);
			// ??????host??????
			JSONObject hostJo = new JSONObject();
			JSONObject hosts = hostItem.getJSONObject("Hosts");
			// ?????????
			hostJo.put("host_name", hosts.getString("host_name"));
			// ip??????
			hostJo.put("ip", hosts.getString("ip"));
			// cpu??????
			hostJo.put("cpu_count", hosts.getInteger("cpu_count"));
			// ????????????
			hostJo.put("total_mem", hosts.getInteger("total_mem"));

			// ?????????????????????
			JSONObject metrics = hostItem.getJSONObject("metrics");
			if (metrics != null && !metrics.isEmpty()) {
				JSONObject disk = metrics.getJSONObject("disk");
				double diskRatio = disk.getDoubleValue("disk_free")
						/ disk.getDoubleValue("disk_total");
				hostJo.put("disk_ratio", getRatio(diskRatio));
				// ????????????
				hostJo.put("load",
						metrics.getJSONObject("load").getString("load_one"));
			}

			JSONArray hostComponents = hostItem.getJSONArray("host_components");
			for (int j = 0; j < hostComponents.size(); j++) {
				JSONObject hostComponent = hostComponents.getJSONObject(j);
				JSONObject hostRoles = hostComponent.getJSONObject("HostRoles");
				String componentName = hostRoles.getString("component_name");
				// ????????????????????????????????????????????????
				if (result.containsKey(componentName)) {
					JSONArray jsonArray = result.getJSONArray(componentName);
					jsonArray.add(hostJo);
				}
			}
		}
		log.info("result:{}", result);
		return result;
	}

	public static String getHostByComponentByService(String component,
			JSONObject componentAndHost) {
		JSONArray componentArray = componentAndHost.getJSONArray(component);
		String host = "";
		for (int i = 0; i < componentArray.size(); i++) {
			JSONObject jo = componentArray.getJSONObject(i);
			host = jo.getString("host_name");
			break;
		}
		return host;
	}

	/**
	 * ????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getComponentByService(String clusterName,
			String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/services/").append(serviceName)
				.append("/components?minimal_response=true").append("&_=")
				.append(System.currentTimeMillis());

		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getHostsByService(String clusterName, String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder
				.append("/api/v1/clusters/")
				.append(clusterName)
				.append("/hosts?fields=Hosts/host_name,Hosts/maintenance_state,Hosts/cpu_count,Hosts/ph_cpu_count,alerts_summary,Hosts/host_status,Hosts/host_state,Hosts/last_heartbeat_time,Hosts/ip,Hosts/total_mem,host_components/HostRoles/state,host_components/HostRoles/maintenance_state,host_components/HostRoles/service_name,host_components/HostRoles/display_name,metrics/disk/disk_free,metrics/disk/disk_total,metrics/load/load_one&sortBy=Hosts/host_name.asc")
				.append("&minimal_response=true")
				.append("&host_components/HostRoles/service_name=")
				.append(serviceName).append("&_=")
				.append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @return
	 */
	public JSONObject queryInstalledService(String clusterName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder
				.append("/api/v1/clusters/")
				.append(clusterName)
				.append("/services")
				.append("?fields=ServiceInfo/state,ServiceInfo/maintenance_state")
				.append("&minimal_response=true").append("&_=")
				.append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject configThemes(String clusterName, String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/stacks/HDP/versions/3.1")
				.append("/services/").append(serviceName).append("/themes")
				.append("?fields=*").append("&ThemeInfo/default=true")
				.append("&_=").append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getConfigInfo(String clusterName, String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/configurations/service_config_versions")
				.append("?service_name=").append(serviceName)
				.append("&fields=*").append("&is_current=true").append("&_=")
				.append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getConfigAllVersion(String clusterName, String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder
				.append("/api/v1/clusters/")
				.append(clusterName)
				.append("/configurations/service_config_versions")
				.append("?service_name=")
				.append(serviceName)
				.append("&fields=service_config_version,user,hosts,group_id,group_name,is_current,createtime,service_name,service_config_version_note,stack_id,is_cluster_compatible")
				.append("&sortBy=service_config_version.desc")
				.append("&minimal_response=true").append("&_=")
				.append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param clusterName
	 *            ?????????
	 * @param serviceName
	 *            ?????????
	 * @return
	 */
	public JSONObject getConfigGroup(String clusterName, String serviceName) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/config_groups").append("?ConfigGroup/tag=")
				.append(serviceName).append("&fields=*").append("&_=")
				.append(System.currentTimeMillis());
		return request(urlBuilder.toString(), HttpMethod.GET, headers,
				restTemplate, null);
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param bigdataCommonFegin
	 * @param clusterName
	 *            ?????????
	 * @return
	 */
	public JSONObject getConfigHostInfo(BigdataCommonFegin bigdataCommonFegin,
			String clusterName) {
		Result<SysGlobalArgs> globalParam = bigdataCommonFegin.getGlobalParam(
				"ambari", "configHostInfo");
		log.info("getConfigHostInfo globalParam:{}", globalParam);
		SysGlobalArgs globalArgs = globalParam.getData();
		String url = globalArgs.getArgValue();
		Map<Integer, Object> map = Maps.newHashMap();
		map.put(1, clusterName);
		return getAmbariApi(url, globalArgs.getArgValue(), map);
	}

	/**
	 * ???????????????
	 *
	 * @param bigdataCommonFegin
	 * @param clusterName
	 *            ?????????
	 * @param configGroup
	 *            ?????????
	 * @return
	 */
	public JSONObject updateConfigGroup(BigdataCommonFegin bigdataCommonFegin,
			String clusterName, ConfigGroup configGroup) {
		// Result<SysGlobalArgs> globalParam =
		// bigdataCommonFegin.getGlobalParam("ambari", "updateConfigGroup");
		// log.info("getConfigHostInfo globalParam:{}", globalParam);
		// SysGlobalArgs globalArgs = globalParam.getData();
		// String url = globalArgs.getArgValue();
		if (configGroup != null && configGroup.getId() != null) {
			url = url + "/" + configGroup.getId();
			configGroup.setId(null);
		}
		configGroup.setClusterId(null);
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/config_groups");
		JSONObject jo = JSON.parseObject(JSONObject.toJSONString(configGroup));
		JSONObject data = new JSONObject();
		data.put("ConfigGroup", jo);
		return request(urlBuilder.toString(), HttpMethod.POST, headers,
				restTemplate, data.toJSONString());
	}

	/**
	 * ???????????????
	 *
	 * @param bigdataCommonFegin
	 * @param clusterName
	 *            ?????????
	 * @param groupId
	 *            ?????????id
	 * @return
	 */
	public JSONObject deleteConfigGroup(BigdataCommonFegin bigdataCommonFegin,
			String clusterName, Integer groupId) {
		// Result<SysGlobalArgs> globalParam =
		// bigdataCommonFegin.getGlobalParam("ambari", "updateConfigGroup");
		// log.info("getConfigHostInfo globalParam:{}", globalParam);
		// SysGlobalArgs globalArgs = globalParam.getData();
		// String url = globalArgs.getArgValue();
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName)
				.append("/config_groups/").append(groupId);
		return request(urlBuilder.toString(), HttpMethod.DELETE, headers,
				restTemplate, null);
	}

	/**
	 * ???????????????????????????URL??????
	 *
	 * @param stackName
	 *            ????????????
	 * @param version
	 *            ??????
	 * @param osType
	 *            ??????????????????
	 * @param name
	 *            repo??????
	 * @param repositories
	 *            ????????????
	 */
	public JSONObject resourceOsUrlValidation(String stackName, String version,
			String osType, String name, JSONObject repositories) {
		String requestUrl = url + "/api/v1/stacks/" + stackName + "/versions/"
				+ version + "/operating_systems/" + osType + "/repositories/"
				+ name + "?validate_only=true";
		log.info("stackName {} version {} osType {} name {} repositories:{}",
				stackName, version, osType, name, repositories.toJSONString());
		return request(requestUrl, HttpMethod.POST, headers, restTemplate,
				repositories.toJSONString());
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param id
	 *            ??????ID
	 * @param stackName
	 *            ????????????
	 * @param stackVersion
	 *            ????????????
	 * @param repositories
	 *            ???????????????????????????
	 */
	public JSONObject clusterVersionSave(String stackName, String stackVersion,
			Integer id, JSONObject repositories) {
		String requestUrl = url + "/api/v1/stacks/" + stackName + "/versions/"
				+ stackVersion + "/repository_versions/" + id;
		log.info("stackName {} stackVersion {} id {} repositories:{}",
				stackName, stackVersion, id, repositories.toJSONString());

		JSONObject result = null;
		HttpEntity<String> httpEntity = new HttpEntity<>(
				JSONObject.toJSONString(repositories), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				requestUrl, HttpMethod.PUT, httpEntity, String.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(responseEntity.getBody());
		}
		log.info("result={}", result);
		return result;
	}

	/**
	 * ??????????????????????????????
	 */
	public JSONObject stackHistory() {
		String requestUrl = url + "/api/v1/stacks"
				+ "?fields=versions/repository_versions/RepositoryVersions&_="
				+ System.currentTimeMillis();
		return request(requestUrl, HttpMethod.GET, headers, restTemplate, null);
	}

	/**
	 * ????????????????????????
	 */
	public JSONObject clusters() {
		String requestUrl = url + "/api/v1/clusters"
				+ "?fields=Clusters/cluster_id&_=" + System.currentTimeMillis();
		return request(requestUrl, HttpMethod.GET, headers, restTemplate, null);
	}

	/**
	 * ????????????
	 *
	 * @param bigdataCommonFegin
	 * @param settings
	 *            ????????????
	 * @return
	 */
	public JSONObject configValidations(BigdataCommonFegin bigdataCommonFegin,
			JSONObject settings) {
		// Result<SysGlobalArgs> globalParam =
		// bigdataCommonFegin.getGlobalParam("ambari", "validateConfig");
		// log.info("configValidations globalParam:{}", globalParam);
		// SysGlobalArgs globalArgs = globalParam.getData();
		// return getAmbariApi(globalArgs.getArgValue(),
		// globalArgs.getArgValueDesc(), settings);

		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/stacks/HDP/versions/3.1/validations");
		log.info("settting:{}", settings.toJSONString());
		return request(urlBuilder.toString(), HttpMethod.POST, headers,
				restTemplate, settings.toJSONString());
	}

	/**
	 * ????????????
	 *
	 * @param bigdataCommonFegin
	 * @param settings
	 *            ????????????
	 * @return
	 */
	public JSONObject configRecommendations(
			BigdataCommonFegin bigdataCommonFegin, JSONObject settings) {
		// Result<SysGlobalArgs> globalParam =
		// bigdataCommonFegin.getGlobalParam("ambari", "configRecommendations");
		// log.info("configValidations globalParam:{}", globalParam);
		// SysGlobalArgs globalArgs = globalParam.getData();
		// return getAmbariApi(globalArgs.getArgValue(),
		// globalArgs.getArgValueDesc(), settings);
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/stacks/HDP/versions/3.1/recommendations");
		log.info("settting:{}", settings.toJSONString());
		return request(urlBuilder.toString(), HttpMethod.POST, headers,
				restTemplate, settings.toJSONString());
	}

	/**
	 * ??????????????????
	 *
	 * @param bigdataCommonFegin
	 * @param clusterName
	 *            ?????????
	 * @param settings
	 *            ??????????????????
	 * @return
	 */
	public JSONObject updateConfig(BigdataCommonFegin bigdataCommonFegin,
			String clusterName, JSONArray settings) {
		// ??????url
		StringBuilder urlBuilder = new StringBuilder(url);
		urlBuilder.append("/api/v1/clusters/").append(clusterName);
		log.info("settting:{}", settings.toJSONString());
		return request(urlBuilder.toString(), HttpMethod.PUT, headers,
				restTemplate, settings.toJSONString());
	}

	/**
	 * ????????????
	 *
	 * @param requestUrl
	 *            ????????????
	 * @param requestMode
	 *            ????????????
	 * @param form
	 *            ????????????
	 * @return ????????????????????????
	 */
	public JSONObject uploadFile(String username, String requestUrl,
			String requestMode, MultiValueMap form) {
		changeLoginUser(username);
		String newUrl = url.concat(requestUrl);
		HttpEntity httpEntity = new HttpEntity<>(form, headers);
		log.info("??????url={},httpEntity={}", newUrl, httpEntity);
		JSONObject result = null;
		ResponseEntity<String> exchange = restTemplate.exchange(newUrl,
				getRequestMode(requestMode), httpEntity, String.class);
		if (!exchange.getStatusCode().is2xxSuccessful()) {
			log.error("??????ambari??????????????????");
			throw new BusinessException("??????ambari??????????????????");
		} else {
			result = JSONObject.parseObject(exchange.getBody());
		}
		return result;
	}

	/**
	 * ????????????
	 *
	 * @param requestUrl
	 *            ????????????
	 * @param requestMode
	 *            ????????????
	 * @param jsonObject
	 *            ????????????
	 * @return ????????????????????????
	 */
	public JSONObject fileOperation(boolean isChangerUser, String username,
			String requestUrl, String requestMode, JSONObject jsonObject) {
		if (isChangerUser) {
			changeLoginUser(username);
		}
		headers.set("X-Requested-By", "ambari");
		HttpEntity httpEntity = new HttpEntity<>(jsonObject, headers);
		log.info("??????url={},httpEntity={}", requestUrl, httpEntity);
		log.info("jsonObject:{}", jsonObject);
		JSONObject result = null;
		ResponseEntity<String> exchange = restTemplate.exchange(url
				+ requestUrl, getRequestMode(requestMode), httpEntity,
				String.class);
		if (HttpStatus.OK.value() != exchange.getStatusCodeValue()) {
			log.error("??????ambari????????????.");
		} else {
			result = JSONObject.parseObject(exchange.getBody());
		}
		return result;
	}

	/**
	 * hdfs????????????
	 *
	 * @param requestUrl
	 *            ????????????
	 * @param requestMode
	 *            ????????????
	 * @param filePath
	 *            ?????????????????????
	 * @return ??????????????????
	 */
	public void download(String username, String requestUrl,
			String requestMode, String filePath) {
		if (StrUtil.isBlankIfStr(filePath)) {
			throw new BusinessException("path????????????:" + filePath);
		}
		changeLoginUser(username);
		String[] fileNameArr = StrUtil.splitToArray(filePath, "/");
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		String s = url.concat(requestUrl).concat("?path=").concat(filePath)
				.concat("&download=true");
		ResponseEntity<byte[]> exchange = restTemplate.exchange(s,
				getRequestMode(requestMode), httpEntity, byte[].class);
		if (HttpStatus.OK.value() != exchange.getStatusCodeValue())
			throw new BusinessException("??????ambari????????????");
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletResponse res = requestAttributes.getResponse();
		res.setContentType("application/octet-stream; charset=utf-8");
		res.setHeader("Content-Disposition", "attachment; filename="
				+ fileNameArr[fileNameArr.length - 1]);
		try {
			res.getOutputStream().write(exchange.getBody());
		} catch (IOException e) {
			log.error("?????????????????????", e);
		}
	}

	private AmbariUtil() {
	}

	public static AmbariUtil getInstance() {
		return new AmbariUtil();
	}

	public static JSONObject analysisYarnQueueConfig(JSONObject result,
			String yarnSite) {
		JSONObject jsonObject = new JSONObject();
		JSONArray items = result.getJSONArray("items");
		JSONObject item = items.getJSONObject(0);
		JSONObject yarnSiteJSON = JSONObject.parseObject(yarnSite);
		if (Objects.nonNull(item)) {
			JSONObject properties = item.getJSONObject("properties");
			List<String> queueFullNames = properties
					.keySet()
					.stream()
					.filter(data -> ReUtil.isMatch(
							BigDataConfConstants.YARN_QUEUE_FULL_NAME_REGEX,
							data))
					.map(data -> ReUtil.get(
							BigDataConfConstants.YARN_QUEUE_FULL_NAME_REGEX,
							data, 1)).collect(Collectors.toList());
			String defaultMaxApplicationsNum = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_APPLICATIONS);
			String defaultMaxAmResourcePercent = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_AM_RESOURCE_PERCENT);
			String defaultNodeLocalityDelay = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_NODE_LOCALITY_DELAY);
			String defaultMaxAllowMb = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_MB);
			String defaultMaxAllowVcores = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_VCORES);
			String defaultMinAllowVcores = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MINIMUM_ALLOCATION_VCORES);
			String defaultMinAllowMb = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MINIMUM_ALLOCATION_MB);
			List<YarnQueueConfInfo> yarnQueueConfInfos = queueFullNames
					.stream()
					.map(data -> addYarnQueueConfInfo(properties, data,
							defaultMaxApplicationsNum,
							defaultMaxAmResourcePercent, defaultMaxAllowMb,
							defaultMaxAllowVcores))
					.collect(Collectors.toList());
			yarnQueueConfInfos = treeBuilder(yarnQueueConfInfos);
			jsonObject.put("queues", yarnQueueConfInfos);
			jsonObject.put("maxAllowMb", defaultMaxAllowMb);
			jsonObject.put("maxAllowVcores", defaultMaxAllowVcores);
			jsonObject.put("minAllowVcores", defaultMinAllowVcores);
			jsonObject.put("minAllowMb", defaultMinAllowMb);
			jsonObject.put("maxApplicationsNum", defaultMaxApplicationsNum);
			jsonObject.put("maxAmResourcePercent", defaultMaxAmResourcePercent);
		}
		return jsonObject;
	}

	public static YarnQueueConfInfo addYarnQueueConfInfo(
			JSONObject application, String queueFullName,
			String defaultMaxApplicationsNum,
			String defaultMaxAmResourcePercent, String defaultMaxAllowMb,
			String defaultMaxAllowVcores) {
		String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
				.concat(queueFullName);
		String capacity = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_CAPACITY));
		String queues = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_QUEUES));
		String state = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_STATE));
		String maximumCapacity = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_CAPACITY));
		String priority = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_PRIORITY));
		String maximumAllocationMb = application
				.getString(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_MB));
		maximumAllocationMb = StrUtil.isBlank(maximumAllocationMb) ? defaultMaxAllowMb
				: maximumAllocationMb;
		String maximumAllocationVcores = application
				.getString(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_VCORES));
		maximumAllocationVcores = StrUtil.isBlank(maximumAllocationVcores) ? defaultMaxAllowVcores
				: maximumAllocationVcores;
		String userLimitFactor = application
				.getString(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_USER_LIMIT_FACTOR));
		String minimumUserLimitPercent = application
				.getString(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_MINIMUM_USER_LIMIT_PERCENT));
		String maximumApplications = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAX_APPLICATIONS));
		maximumApplications = StrUtil.isBlank(maximumApplications) ? defaultMaxApplicationsNum
				: maximumApplications;
		String maximumAmResourcePercent = application
				.getString(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAX_AM_RESOURCE_PERCENT));
		maximumAmResourcePercent = StrUtil.isBlank(maximumAmResourcePercent) ? defaultMaxAmResourcePercent
				: maximumAmResourcePercent;
		String orderingPolicy = application.getString(prefix
				.concat(BigDataConfConstants.YARN_QUEUE_CONF_ORDERING_POLICY));
		orderingPolicy = StrUtil.isBlank(orderingPolicy) ? BigDataConfConstants.YARN_QUEUE_CONF_DEFAULT_CAPACITY
				: orderingPolicy;
		YarnQueueConfInfo info = new YarnQueueConfInfo();
		info.setCapacity(capacity);
		info.setMaximumAllocationMb(maximumAllocationMb);
		info.setMaximumAllocationVcores(maximumAllocationVcores);
		info.setMaximumAmResourcePercent(maximumAmResourcePercent);
		info.setMaximumApplications(maximumApplications);
		info.setMaximumCapacity(maximumCapacity);
		info.setMinimumUserLimitPercent(minimumUserLimitPercent);
		info.setPriority(priority);
		info.setQueueFullName(queueFullName);
		List<String> arr = StrUtil.splitTrim(queueFullName, ".");
		if (1 == arr.size()) {
			info.setParentQueueFullName("H");
		} else {
			info.setParentQueueFullName(StrUtil.join(".",
					CollUtil.sub(arr, 0, arr.size() - 1)));
		}
		info.setQueueName(arr.get(arr.size() - 1));
		info.setState(state);
		if (StrUtil.isNotBlank(queues)) {
			info.setQueues(StrUtil.splitTrim(queues, ","));
		} else {
			info.setOrderingPolicy(orderingPolicy);
		}
		info.setUserLimitFactor(userLimitFactor);
		return info;
	}

	public static List<YarnQueueConfInfo> treeBuilder(
			List<YarnQueueConfInfo> yarnQueueConfInfos) {
		List<YarnQueueConfInfo> result = new ArrayList<>();
		for (YarnQueueConfInfo yarnQueueConfInfo : yarnQueueConfInfos) {
			if (StrUtil.equals("H", yarnQueueConfInfo.getParentQueueFullName())) {
				result.add(yarnQueueConfInfo);
			}
			for (YarnQueueConfInfo info : yarnQueueConfInfos) {
				if (StrUtil.equalsAnyIgnoreCase(info.getParentQueueFullName(),
						yarnQueueConfInfo.getQueueFullName())) {
					if (yarnQueueConfInfo.getSubQueues() == null) {
						yarnQueueConfInfo.setSubQueues(new ArrayList<>());
					}
					yarnQueueConfInfo.getSubQueues().add(info);
				}
			}
		}
		return result;
	}

	private static void checkBody(List<YarnQueueConfInfo> infos,
			String defaultMaxAllowMb, String defaultMaxAllowVcores) {
		double sum = infos.stream()
				.mapToDouble(info -> Double.valueOf(info.getCapacity())).sum();
		if (sum != 100) {
			throw new BusinessException("???????????????????????????100,???????????????");
		}
		Integer maxAllowMb = Integer.valueOf(defaultMaxAllowMb);
		Integer maxAllowVcores = Integer.valueOf(defaultMaxAllowVcores);
		Long count = infos
				.stream()
				.filter(info -> {
					if (StrUtil.isBlank(info.getMaximumAllocationMb())) {
						info.setMaximumAllocationMb(defaultMaxAllowMb);
					}
					if (StrUtil.isBlank(info.getMaximumAllocationVcores())) {
						info.setMaximumAllocationVcores(defaultMaxAllowVcores);
					}
					return (Integer.valueOf(info.getMaximumAllocationMb()) > maxAllowMb
							|| Integer.valueOf(info
									.getMaximumAllocationVcores()) > maxAllowVcores || Integer
							.valueOf(info.getMaximumAllocationMb()) % 1024 != 0);
				}).count();
		if (count.intValue() > 0) {
			throw new BusinessException("??????????????????????????????????????????????????????????????????,?????????????????????1024?????????");
		}
	}

	public static JSONObject getUpdateYarnQueueBody(
			List<YarnQueueConfInfo> infos, JSONObject sourceResult) {
		JSONArray items = sourceResult.getJSONArray("items");
		JSONObject item = items.getJSONObject(0);
		if (Objects.nonNull(item)) {
			String type = item.getString("type");
			JSONObject properties = item.getJSONObject("properties");
			infos.forEach(info -> {
				String queueFullName = info.getQueueFullName();
				String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
						.concat(queueFullName);
				String state = info.getState();
				if (StrUtil.isNotBlank(state)
						&& BigDataConfConstants.YARN_QUEUE_STATE_LIST
								.contains(state)) {
					properties.put(
							prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_STATE),
							state.toUpperCase());
				}
			});
			JSONObject desiredConfigJSON = new JSONObject();
			desiredConfigJSON.put("type", type);
			desiredConfigJSON
					.put("tag", "version".concat(String.valueOf(DateUtil.date()
							.getTime())));
			desiredConfigJSON.put("service_config_version_note", "");
			desiredConfigJSON.put("properties", properties);
			JSONObject clustersJSON = new JSONObject();
			clustersJSON.put("desired_config", desiredConfigJSON);
			JSONObject result = new JSONObject();
			result.put("Clusters", clustersJSON);
			return result;
		}
		return null;
	}

	public static JSONObject getUpdateYarnQueueBody(
			List<YarnQueueConfInfo> infos, JSONObject sourceResult,
			String yarnSite) {
		JSONArray items = sourceResult.getJSONArray("items");
		JSONObject item = items.getJSONObject(0);
		JSONObject yarnSiteJSON = JSONObject.parseObject(yarnSite);
		if (Objects.nonNull(item)) {
			String type = item.getString("type");
			JSONObject properties = item.getJSONObject("properties");
			String defaultMaxApplicationsNum = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_APPLICATIONS);
			String defaultMaxAmResourcePercent = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_AM_RESOURCE_PERCENT);
			String defaultNodeLocalityDelay = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_NODE_LOCALITY_DELAY);
			String defaultMaxAllowMb = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_MB);
			String defaultMaxAllowVcores = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_VCORES);
			checkBody(infos, defaultMaxAllowMb, defaultMaxAllowVcores);
			appendUpdateYarnQueueBody(infos, properties,
					defaultMaxApplicationsNum, defaultMaxAmResourcePercent,
					defaultMaxAllowMb, defaultMaxAllowVcores);
			JSONObject desiredConfigJSON = new JSONObject();
			desiredConfigJSON.put("type", type);
			desiredConfigJSON
					.put("tag", "version".concat(String.valueOf(DateUtil.date()
							.getTime())));
			desiredConfigJSON.put("service_config_version_note", "");
			desiredConfigJSON.put("properties", properties);
			JSONObject clustersJSON = new JSONObject();
			clustersJSON.put("desired_config", desiredConfigJSON);
			JSONObject result = new JSONObject();
			result.put("Clusters", clustersJSON);
			return result;
		}
		return null;
	}

	private static void appendUpdateYarnQueueBody(
			List<YarnQueueConfInfo> infos, JSONObject properties,
			String defaultMaxApplicationsNum,
			String defaultMaxAmResourcePercent, String defaultMaxAllowMb,
			String defaultMaxAllowVcores) {
		infos.forEach(info -> {
			String queueFullName = info.getQueueFullName();
			String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
					.concat(queueFullName);
			String capacity = info.getCapacity();
			String maximumAllocationMb = info.getMaximumAllocationMb();
			String maximumAllocationVcores = info.getMaximumAllocationVcores();
			String maximumAmResourcePercent = info
					.getMaximumAmResourcePercent();
			String maximumApplications = info.getMaximumApplications();
			String maximumCapacity = info.getMaximumCapacity();
			String minimumUserLimitPercent = info.getMinimumUserLimitPercent();
			String priority = info.getPriority();
			String orderingPolicy = info.getOrderingPolicy();
			String userLimitFactor = info.getUserLimitFactor();
			String state = info.getState();
			if (StrUtil.isNotBlank(state)
					&& BigDataConfConstants.YARN_QUEUE_STATE_LIST
							.contains(state)) {
				properties.put(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_STATE),
						state.toUpperCase());
			}
			if (StrUtil.isNotBlank(capacity)) {
				properties.put(prefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_CAPACITY),
						capacity);
			}
			String maximumAllocationMbKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_MB);
			if (!StrUtil.equals(defaultMaxAllowMb, maximumAllocationMb)) {
				properties.put(maximumAllocationMbKey, maximumAllocationMb);
			} else {
				properties.remove(maximumAllocationMbKey);
			}
			String maximumAllocationVcoresKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_CAPACITY);
			if (!StrUtil.equals(defaultMaxAllowVcores, maximumAllocationVcores)) {
				properties.put(maximumAllocationVcoresKey,
						maximumAllocationVcores);
			} else {
				properties.remove(maximumAllocationVcoresKey);
			}
			String maximumApplicationsKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAX_APPLICATIONS);
			if (!StrUtil.equals(defaultMaxApplicationsNum, maximumApplications)) {
				properties.put(maximumApplicationsKey, maximumApplications);
			} else {
				properties.remove(maximumApplicationsKey);
			}
			String maximumAmResourcePercentKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAX_AM_RESOURCE_PERCENT);
			if (!StrUtil.equals(defaultMaxAmResourcePercent,
					maximumAmResourcePercent)) {
				properties.put(maximumAmResourcePercentKey,
						maximumAmResourcePercent);
			} else {
				properties.remove(maximumAmResourcePercentKey);
			}
			String maximumCapacityKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_CAPACITY);
			if (StrUtil.isNotBlank(properties.getString(maximumCapacityKey))) {
				properties.put(maximumCapacityKey, maximumCapacity);
			}
			String minimumUserLimitPercentKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_MINIMUM_USER_LIMIT_PERCENT);
			if (StrUtil.isNotBlank(minimumUserLimitPercent)) {
				properties.put(minimumUserLimitPercentKey,
						minimumUserLimitPercent);
			}
			String priorityKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_PRIORITY);
			if (StrUtil.isNotBlank(priority)) {
				properties.put(priorityKey, priority);
			}
			String orderingPolicyKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_ORDERING_POLICY);
			if (StrUtil.isNotBlank(orderingPolicy)
					&& !StrUtil
							.equalsIgnoreCase(
									orderingPolicy,
									BigDataConfConstants.YARN_QUEUE_CONF_DEFAULT_CAPACITY)) {
				properties.put(orderingPolicyKey, orderingPolicy);
			}
			String userLimitFactorKey = prefix
					.concat(BigDataConfConstants.YARN_QUEUE_CONF_USER_LIMIT_FACTOR);
			if (StrUtil.isNotBlank(userLimitFactor)) {
				properties.put(userLimitFactorKey, userLimitFactor);
			}

		});

	}

	public static JSONObject getDeleteYarnQueueBody(JSONObject sourceResult,
			List<YarnQueueConfInfo> infos) {
		checkDeleteYarnQueueBody(infos);
		Set<String> queueSet = infos
				.stream()
				.filter(info -> Objects.nonNull(info.getIsDelete())
						&& info.getIsDelete())
				.map(YarnQueueConfInfo::getQueueFullName)
				.collect(Collectors.toSet());
		Set<YarnQueueConfInfo> delInfos = infos.stream()
				.filter(YarnQueueConfInfo::getIsDelete)
				.collect(Collectors.toSet());
		Set<YarnQueueConfInfo> queueInfos = infos
				.stream()
				.filter(info -> (!info.getIsDelete() || Objects.isNull(info
						.getIsDelete()))).collect(Collectors.toSet());
		JSONArray items = sourceResult.getJSONArray("items");
		JSONObject item = items.getJSONObject(0);
		if (Objects.nonNull(item)) {
			String type = item.getString("type");
			JSONObject properties = item.getJSONObject("properties");
			Set<String> deleteKeyList = properties
					.keySet()
					.stream()
					.filter(key -> queueSet.stream().anyMatch(
							name -> StrUtil.contains(key, name)))
					.collect(Collectors.toSet());
			deleteKeyList.forEach(key -> properties.remove(key));
			delInfos.forEach(info -> {
				String parentQueueFullName = info.getParentQueueFullName();
				String queueName = info.getQueueName();
				String parentPrefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
						.concat(parentQueueFullName);
				String parentQueuesKey = parentPrefix
						.concat(BigDataConfConstants.YARN_QUEUE_CONF_QUEUES);
				String queues = properties.getString(parentQueuesKey);
				if (StrUtil.isNotBlank(queues)) {
					Set<String> queuesSet = (Set<String>) Convert.toCollection(
							HashSet.class, String.class, queues);
					queuesSet.remove(queueName);
					if (CollUtil.isEmpty(queuesSet)) {
						properties.remove(parentQueuesKey);
					} else {
						properties.put(parentQueuesKey,
								StrUtil.join(",", queuesSet));
					}
				}
			});
			queueInfos
					.forEach(info -> {
						String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
								.concat(info.getQueueFullName());
						if (StrUtil.isNotBlank(info.getOrderingPolicy())
								&& !StrUtil.equalsIgnoreCase(
										info.getOrderingPolicy(),
										BigDataConfConstants.YARN_QUEUE_CONF_DEFAULT_CAPACITY)) {
							properties.put(
									prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_ORDERING_POLICY),
									info.getOrderingPolicy());
						}
						properties.put(
								prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_CAPACITY),
								info.getCapacity());
					});
			JSONObject desiredConfigJSON = new JSONObject();
			desiredConfigJSON.put("type", type);
			desiredConfigJSON
					.put("tag", "version".concat(String.valueOf(DateUtil.date()
							.getTime())));
			desiredConfigJSON.put("service_config_version_note", "");
			desiredConfigJSON.put("properties", properties);
			JSONObject clustersJSON = new JSONObject();
			clustersJSON.put("desired_config", desiredConfigJSON);
			JSONObject result = new JSONObject();
			result.put("Clusters", clustersJSON);
			return result;
		}
		return null;
	}

	private static void checkDeleteYarnQueueBody(List<YarnQueueConfInfo> infos) {
		double sum = infos
				.stream()
				.filter(info -> (Objects.isNull(info.getIsDelete()) || !info
						.getIsDelete()))
				.mapToDouble(info -> Double.valueOf(info.getCapacity())).sum();
		if (100 != sum) {
			if (infos
					.stream()
					.filter(info -> (Objects.isNull(info.getIsDelete()) || !info
							.getIsDelete())
							&& CollUtil.isNotEmpty(info.getQueues())).count() == 0)
				throw new BusinessException("???????????????????????????100");
		}
		long count = infos
				.stream()
				.filter(info -> ((Objects.isNull(info.getIsDelete()) || !info
						.getIsDelete()) && StrUtil.isBlank(info.getCapacity())))
				.count();
		if (!Objects.equals(0L, count)) {
			throw new BusinessException("capacity????????????");
		}
	}

	public static JSONObject getInsertYarnQueueBody(JSONObject sourceResult,
			String yarnSite, List<YarnQueueConfInfo> infos) {
		checkInsertYarnQueueBody(infos);
		JSONArray items = sourceResult.getJSONArray("items");
		JSONObject item = items.getJSONObject(0);
		JSONObject yarnSiteJSON = JSONObject.parseObject(yarnSite);
		if (Objects.nonNull(item)) {
			String type = item.getString("type");
			JSONObject properties = item.getJSONObject("properties");
			String defaultMaxAllowMb = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_MB);
			String defaultMaxAllowVcores = yarnSiteJSON
					.getString(BigDataConfConstants.YARN_SCHEDULER_MAXIMUM_ALLOCATION_VCORES);
			String defaultMaxApplicationsNum = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_APPLICATIONS);
			String defaultMaxAmResourcePercent = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_AM_RESOURCE_PERCENT);
			String defaultNodeLocalityDelay = properties
					.getString(BigDataConfConstants.YARN_QUEUE_CONF_NODE_LOCALITY_DELAY);
			Map<Boolean, List<YarnQueueConfInfo>> queueMap = infos
					.stream()
					.collect(Collectors.groupingBy(YarnQueueConfInfo::getIsNew));
			if (CollUtil.isNotEmpty(queueMap.get(Boolean.FALSE))) {
				queueMap.get(Boolean.FALSE)
						.forEach(
								info -> {
									String capacity = info.getCapacity();
									String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
											.concat(info.getQueueFullName());
									if (StrUtil.isNotBlank(capacity)) {
										properties.put(
												prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_CAPACITY),
												capacity);
									}
								});
			}
			queueMap.get(Boolean.TRUE)
					.forEach(
							info -> {
								String parentQueueFullName = info
										.getParentQueueFullName();
								String queueName = info.getQueueName();
								String capacity = info.getCapacity();
								String prefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
										.concat(info.getQueueFullName());
								String parentPrefix = BigDataConfConstants.YARN_QUEUE_CONF_PREFIX
										.concat(parentQueueFullName);
								String maximumAllocationMb = info
										.getMaximumAllocationMb();
								String maximumAllocationVcores = info
										.getMaximumAllocationVcores();
								String maximumAmResourcePercent = info
										.getMaximumAmResourcePercent();
								String maximumApplications = info
										.getMaximumApplications();
								String maximumCapacity = info
										.getMaximumCapacity();
								String minimumUserLimitPercent = info
										.getMinimumUserLimitPercent();
								String priority = info.getPriority();
								String orderingPolicy = info
										.getOrderingPolicy();
								String userLimitFactor = info
										.getUserLimitFactor();
								String parentQueuesKey = parentPrefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_QUEUES);
								properties.remove(parentPrefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_ORDERING_POLICY));
								String queues = properties
										.getString(parentQueuesKey);
								if (StrUtil.isBlank(queues)) {
									properties.put(parentQueuesKey, queueName);
								} else {
									properties.put(parentQueuesKey, queues
											.concat(",").concat(queueName));
								}
								properties.put(
										prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_STATE),
										"RUNNING");
								properties.put(
										prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_ACL_ADMINISTER_QUEUE),
										"*");
								properties.put(
										prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_ACL_SUMBIT_APPLICATIONS),
										"*");
								properties.put(
										prefix.concat(BigDataConfConstants.YARN_QUEUE_CONF_CAPACITY),
										capacity);
								String maximumAllocationMbKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_MB);
								if (!StrUtil.equals(defaultMaxAllowMb,
										maximumAllocationMb)) {
									properties.put(maximumAllocationMbKey,
											maximumAllocationMb);
								} else {
									properties.remove(maximumAllocationMbKey);
								}
								String maximumAllocationVcoresKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_VCORES);
								if (!StrUtil.equals(defaultMaxAllowVcores,
										maximumAllocationVcores)) {
									properties.put(maximumAllocationVcoresKey,
											maximumAllocationVcores);
								} else {
									properties
											.remove(maximumAllocationVcoresKey);
								}
								String maximumApplicationsKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_CAPACITY);
								if (!StrUtil.equals(defaultMaxApplicationsNum,
										maximumApplications)) {
									properties.put(maximumApplicationsKey,
											maximumApplications);
								} else {
									properties.remove(maximumApplicationsKey);
								}
								String maximumAmResourcePercentKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAX_AM_RESOURCE_PERCENT);
								if (!StrUtil.equals(
										defaultMaxAmResourcePercent,
										maximumAmResourcePercent)) {
									properties.put(maximumAmResourcePercentKey,
											maximumAmResourcePercent);
								} else {
									properties
											.remove(maximumAmResourcePercentKey);
								}
								String maximumCapacityKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MAXIMUM_CAPACITY);
								if (StrUtil.isNotBlank(properties
										.getString(maximumCapacityKey))) {
									properties.put(maximumCapacityKey,
											maximumCapacity);
								} else {
									properties
											.put(maximumCapacityKey, capacity);
								}
								String minimumUserLimitPercentKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_MINIMUM_USER_LIMIT_PERCENT);
								if (StrUtil.isNotBlank(minimumUserLimitPercent)) {
									properties.put(minimumUserLimitPercentKey,
											minimumUserLimitPercent);
								} else {
									properties.put(minimumUserLimitPercentKey,
											"100");
								}
								String priorityKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_PRIORITY);
								if (StrUtil.isNotBlank(priority)) {
									properties.put(priorityKey, priority);
								} else {
									properties.put(priorityKey, "0");
								}
								String orderingPolicyKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_ORDERING_POLICY);
								if (StrUtil.isNotBlank(orderingPolicy)
										&& !StrUtil
												.equalsIgnoreCase(
														orderingPolicy,
														BigDataConfConstants.YARN_QUEUE_CONF_DEFAULT_CAPACITY)) {
									properties.put(orderingPolicyKey,
											orderingPolicy);
								} else {
									properties.put(orderingPolicyKey, "fifo");
								}
								String userLimitFactorKey = prefix
										.concat(BigDataConfConstants.YARN_QUEUE_CONF_USER_LIMIT_FACTOR);
								if (StrUtil.isNotBlank(userLimitFactor)) {
									properties.put(userLimitFactorKey,
											userLimitFactor);
								} else {
									properties.put(userLimitFactorKey, "1");
								}

							});
			JSONObject desiredConfigJSON = new JSONObject();
			desiredConfigJSON.put("type", type);
			desiredConfigJSON
					.put("tag", "version".concat(String.valueOf(DateUtil.date()
							.getTime())));
			desiredConfigJSON.put("service_config_version_note", "");
			desiredConfigJSON.put("properties", properties);
			JSONObject clustersJSON = new JSONObject();
			clustersJSON.put("desired_config", desiredConfigJSON);
			JSONObject result = new JSONObject();
			result.put("Clusters", clustersJSON);
			return result;
		}
		return null;
	}

	private static void checkInsertYarnQueueBody(List<YarnQueueConfInfo> infos) {
		double sum = infos.stream()
				.mapToDouble(info -> Double.valueOf(info.getCapacity())).sum();
		if (100 != sum) {
			throw new BusinessException("???????????????????????????100");
		}
		long count = infos.stream()
				.filter(info -> (StrUtil.isBlank(info.getCapacity()))).count();
		if (!Objects.equals(0L, count)) {
			throw new BusinessException("capacity????????????");
		}
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param username
	 *            ?????????
	 * @param requestUrl
	 *            ????????????
	 * @param requestMode
	 *            ????????????
	 * @param jsonObject
	 *            ????????????
	 * @return ??????????????????
	 */
	public JSONObject moveFile(String username, String requestUrl,
			String requestMode, JSONObject jsonObject) {
		JSONObject result = null;
		try {
			changeLoginUser(username);
			headers.set("Accept",
					"application/json, text/javascript, */*; q=0.01");
			headers.set("Accept-Language", "zh-CN,zh;q=0.9");
			headers.set("X-Requested-With", "XMLHttpRequest");
			HttpEntity httpEntity = new HttpEntity<>(jsonObject, headers);
			log.info("??????moveFile???url={},httpEntity={}", url + requestUrl,
					httpEntity);
			log.info("jsonObject:{}", jsonObject);
			ResponseEntity<String> exchange = restTemplate.exchange(url
					+ requestUrl, getRequestMode(requestMode), httpEntity,
					String.class);
			if (HttpStatus.OK.value() != exchange.getStatusCodeValue()) {
				log.error("??????ambari????????????.");
			} else {
				result = JSONObject.parseObject(exchange.getBody());
			}
		} catch (Exception e) {
			log.error("??????ambari????????????????????????", e);
			result = new JSONObject();
			result.put("success", false);
			result.put("message", e.getMessage());
		}
		return result;
	}

	public static JSONArray analysisAmbariUsersInfo(JSONObject result) {
		if (Objects.isNull(result)) {
			return new JSONArray();
		}
		JSONArray users = result.getJSONArray("items");
		return users;
	}

	/**
	 * ??????ambari???kerberos??????
	 *
	 * @param sysGlobalArgs
	 * @return
	 * @throws Exception
	 */
	public List<SdpServerKeytab> getKeytabs(SysGlobalArgs sysGlobalArgs)
			throws Exception {
		// ??????url
		String argValue = sysGlobalArgs.getArgValue();
		argValue = argValue.replace("{}", getClusterName());
		String downloadUrl = appendTimeStamp(argValue);
		log.info("downloadUrl:{}", downloadUrl);
		ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(
				downloadUrl, byte[].class);
		if (responseEntity == null
				|| responseEntity.getStatusCodeValue() != 200) {
			throw new Exception("??????ambari??????keytab?????????????????????" + downloadUrl);
		}
		byte[] data = responseEntity.getBody();

		String clusterName = getClusterName();
		List<SdpServerKeytab> list = Lists.newArrayList();
		int i = 0;
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
				InputStreamReader input = new InputStreamReader(inputStream);
				BufferedReader bf = new BufferedReader(input)) {
			String line;
			while ((line = bf.readLine()) != null) {
				i++;
				if (i == 1 || StringUtils.isBlank(line)) {
					continue;
				}
				String[] split = line.split(",");
				SdpServerKeytab serverKeytab = SdpServerKeytab
						.builder()
						.clusterId(getClusterId())
						.clusterName(clusterName)
						.host(split[0])
						.description(split[1])
						.principalName(split[2])
						.principalType(split[3])
						.localUsername(split[4])
						.keytabFilePath(split[5])
						.keytabFileName(
								split[5].substring(split[5].lastIndexOf("/") + 1))
						.keytabFileOwner(split[6]).keytabFileGroup(split[8])
						.keytabFileMode(split[10])
						.keytabFileInstalled(Boolean.valueOf(split[11]))
						.component(SdpServerKeytab.convertComponent(split[1]))
						.build();
				list.add(serverKeytab);
			}
		} catch (Exception e) {
			log.error("?????????????????????", e);
			throw new Exception("?????????????????????");
		}

		return list;
	}

	public static String getComponentName(String owner) {
		if (owner.contains("ambari_infra")) {
			return "infra-solr";
		}
		if (owner.contains("ams")) {
			return "ambari-metrics";
		}
		if (owner.contains("logfeeder")) {
			return "logsearch";
		}
		return owner;
	}

	public static Result<JSONObject> analysisServerKerberosInfo(
			String resultStr, Map<String, String> ipHostMap, Integer clusterId) {
		JSONObject jsonObject = new JSONObject();
		if (StrUtil.isBlank(resultStr)) {
			return Result.failed(jsonObject, "??????kerberos??????");
		}
		resultStr = resultStr.trim();
		List<String> bodyList = StrUtil.split(resultStr, "\r\n");
		bodyList = CollUtil.sub(bodyList, 1, bodyList.size());
		List<ServerKerberos> serverKerberosList = CollUtil.newArrayList();
		bodyList.forEach(data -> {
			List<String> dataList = StrUtil.split(data, ',');
			String host = dataList.get(0).trim();
			String ip = ipHostMap.get(host);
			String owner = dataList.get(6);
			if (StrUtil.isBlank(owner) || owner.equalsIgnoreCase("root")) {
				owner = dataList.get(1);
			}
			String principalName = dataList.get(2);
			String keytabName = dataList.get(5);
			serverKerberosList.add(ServerKerberos.builder().ip(ip).host(host)
					.componentName(getComponentName(owner))
					.keytabName(keytabName).principalName(principalName)
					.build());
		});

		jsonObject.put(clusterId.toString(), serverKerberosList);
		return Result.succeed(jsonObject, "????????????");
	}

}
