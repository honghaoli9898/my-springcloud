package com.seaboxdata.sdps.seaboxProxy.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.framework.bean.ranger.*;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.utils.excelutil.RestTemplateUtil;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RangerUtil {

	/**
	 * 增删改Ranger用户接口地址
	 */
	@Value("${ranger.user.cudUrl}")
	private String rangerCUDUserUrl;
	/**
	 * 查询Ranger用户接口地址
	 */
	@Value("${ranger.user.queryUrl}")
	private String rangerQueryUserUrl;
	/**
	 * 精确查询Ranger用户根据用户名接口地址
	 */
	@Value("${ranger.user.queryUrlByUserName}")
	private String rangerQueryUserByUserNameUrl;
	/**
	 * 根据用户组查询用户接口地址
	 */
	@Value("${ranger.user.queryUserByGroupName}")
	private String rangerQueryUsersByGroupNameUrl;

	/**
	 * 增删改Ranger用户组接口地址
	 */
	@Value("${ranger.group.cudUrl}")
	private String rangerCUDGroupUrl;

	/**
	 * 查询Ranger用户组接口地址
	 */
	@Value("${ranger.group.queryUrl}")
	private String rangerQueryGroupUrl;
	/**
	 * 精确查询Ranger用户组根据用户组名接口地址
	 */
	@Value("${ranger.group.queryUrlByGroupName}")
	private String rangerGetGroupByNameUrl;
	/**
     *
     */
	@Value("${ranger.group.deleteUserFromGroupUrl}")
	private String rangerDeleteUserFromGroupUrl;

	@Value("${ranger.policy.addUrl}")
	private String rangerPolicyUrl;

	@Value("${ranger.policy.queryUrl}")
	private String rangerPolicyQueryUrl;

	@Autowired
	@Qualifier("SeaboxRestTemplate")
	private RestTemplate restTemplate;

	private HttpHeaders headers;
	private String rangerBaseUrl;

	/**
	 * 初始化
	 *
	 * @param clusterId
	 */
	public void init(Integer clusterId) {

		BigdataCommonFegin bigdataCommonFegin = SpringBeanUtil
				.getBean(BigdataCommonFegin.class);
		SdpsServerInfo sdpsRangerInfo = bigdataCommonFegin
				.queryClusterServerInfo(clusterId, ServerTypeEnum.R.name());

		rangerBaseUrl = "http://" + sdpsRangerInfo.getHost() + ":"
				+ sdpsRangerInfo.getPort();

		String plainCreds = sdpsRangerInfo.getUser() + ":"
				+ AmbariUtil.getDecryptPassword(sdpsRangerInfo.getPasswd());
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * 根据用户名查询Ranger用户
	 *
	 * @param userName
	 * @return
	 */
	public VXUsers getUserByName(String userName) {
		VXUsers rangerUserObj = new VXUsers();
		String newUrl = rangerBaseUrl + rangerQueryUserByUserNameUrl;
		newUrl = newUrl.concat("/").concat(userName);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<VXUsers> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.GET, httpEntity, VXUsers.class);
			if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			} else {
				rangerUserObj = responseEntity.getBody();
			}
		} catch (Exception e) {
			log.error("调用Ranger根据用户名获取用户信息接口失败", e);
		}
		return rangerUserObj;
	}

	/**
	 * 根据用户Id查询Ranger用户
	 *
	 * @param userId
	 * @return
	 */
	public VXUsers getUserById(Integer userId) {
		VXUsers rangerUserObj = new VXUsers();
		String newUrl = rangerBaseUrl + rangerQueryUserUrl;
		newUrl = newUrl.concat("/").concat(Integer.toString(userId));
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<VXUsers> responseEntity = restTemplate.exchange(newUrl,
				HttpMethod.GET, httpEntity, VXUsers.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger根据用户ID获取用户信息接口失败");
		} else {
			rangerUserObj = responseEntity.getBody();
		}
		return rangerUserObj;
	}

	/**
	 * 根据用户名模糊查询Ranger用户
	 *
	 * @param userName
	 * @return
	 */
	public RangerPageUser likeUserByName(String userName) {
		RangerPageUser rangerPageUser = new RangerPageUser();
		String newUrl = rangerBaseUrl + rangerQueryUserUrl;
		Map<String, Object> params = Maps.newHashMap();
		params.put(BigDataConfConstants.RANGER_QUERY_USER_API_PARAMS_USERNAME,
				userName);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = RestTemplateUtil.restGet(
				restTemplate, newUrl, httpEntity, String.class, params);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger搜索用户信息接口失败");
		} else {
			rangerPageUser = JSON.parseObject(responseEntity.getBody(),
					RangerPageUser.class);
		}
		return rangerPageUser;
	}

	/**
	 * 添加用户
	 *
	 * @param rangerUserObj
	 * @return
	 */
	public Boolean addRangerUser(VXUsers rangerUserObj) {
		try {
			String newUrl = rangerBaseUrl + rangerCUDUserUrl;
			HttpEntity<String> httpEntity = new HttpEntity<>(
					JSONObject.toJSONString(rangerUserObj), headers);
			ResponseEntity<Object> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.POST, httpEntity, Object.class);
			if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			log.error("调用Ranger添加用户接口失败",e);
			return false;
		}
	}

	/**
	 * 更新用户信息
	 *
	 * @param rangerUserObj
	 * @return
	 */
	public Boolean updateRangerUser(VXUsers rangerUserObj) {
		String newUrl = rangerBaseUrl + rangerCUDUserUrl;
		newUrl = newUrl.concat("/").concat(
				String.valueOf(rangerUserObj.getId()));
		HttpEntity<String> httpEntity = new HttpEntity<>(
				JSONObject.toJSONString(rangerUserObj), headers);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(newUrl,
				HttpMethod.PUT, httpEntity, Object.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger修改用户接口失败");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 删除用户
	 *
	 * @param userName
	 * @return
	 */
	public Boolean deleteRangerUser(String userName) {
		Boolean flag = false;
		try {
			String newUrl = rangerBaseUrl + rangerCUDUserUrl;
			newUrl = newUrl.concat("/").concat(userName);
			Map<String, Object> params = Maps.newHashMap();
			params.put(
					BigDataConfConstants.RANGER_DELETE_USER_API_PARAMS_FORCE,
					"true");
			HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<Object> responseEntity = RestTemplateUtil
					.restDelete(restTemplate, newUrl, httpEntity, Object.class,
							params);
			if (HttpStatus.NO_CONTENT.value() != responseEntity
					.getStatusCodeValue()) {
				log.error("调用Ranger删除用户接口失败");
				return false;
			} else {
				flag = true;
			}
		} catch (Exception e) {
			log.error("调用Ranger删除用户接口失败:", e);
		}
		return flag;
	}

	/**
	 * 添加用户组
	 *
	 * @param rangerGroupObj
	 * @return
	 */
	public Boolean addRangerGroup(VXGroups rangerGroupObj) {
		String newUrl = rangerBaseUrl + rangerCUDGroupUrl;
		HttpEntity<String> httpEntity = new HttpEntity<>(
				JSONObject.toJSONString(rangerGroupObj), headers);
		log.info("httpEntity={},url={}",httpEntity,newUrl);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(newUrl,
				HttpMethod.POST, httpEntity, Object.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger添加用户组接口失败");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据用户组名查询Ranger用户组
	 *
	 * @param groupName
	 * @return
	 */
	public VXGroups getGroupByName(String groupName) {
		VXGroups rangerGroupObj = new VXGroups();
		String newUrl = rangerBaseUrl + rangerGetGroupByNameUrl;
		newUrl = newUrl.concat("/").concat(groupName);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<VXGroups> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.GET, httpEntity, VXGroups.class);
			if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			} else {
				rangerGroupObj = responseEntity.getBody();
			}
		} catch (Exception e) {
			log.error("调用Ranger获取用户组信息接口失败", e);
		}
		return rangerGroupObj;
	}

	/**
	 * 删除用户组
	 *
	 * @param groupName
	 * @return
	 */
	public Boolean deleteRangerGroup(String groupName) {
		Boolean flag = false;
		try {
			String newUrl = rangerBaseUrl + rangerCUDGroupUrl;
			newUrl = newUrl.concat("/").concat(groupName).concat("?forceDelete=true");
			HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
			log.info("httpEntity={},url={}",httpEntity,newUrl);
			ResponseEntity<Object> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.DELETE, httpEntity, Object.class);
			if (HttpStatus.NO_CONTENT.value() != responseEntity
					.getStatusCodeValue()) {
				log.error("调用Ranger删除用户组接口失败");
				return false;
			} else {
				flag = true;
			}
		} catch (Exception e) {
			if(e.getMessage().contains("is Not Found")){
				flag = true;
			}
			log.error("调用Ranger删除用户组接口失败:", e);
		}
		return flag;
	}

	/**
	 * 更新用户组信息
	 *
	 * @param rangerGroupObj
	 * @return
	 */
	public Boolean updateRangerGroup(VXGroups rangerGroupObj) {
		String newUrl = rangerBaseUrl + rangerCUDGroupUrl;
		newUrl = newUrl.concat("/").concat(
				String.valueOf(rangerGroupObj.getId()));
		HttpEntity<String> httpEntity = new HttpEntity<>(
				JSONObject.toJSONString(rangerGroupObj), headers);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(newUrl,
				HttpMethod.PUT, httpEntity, Object.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger修改用户组接口失败");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据用户组名模糊查询Ranger用户组
	 *
	 * @param groupName
	 * @return
	 */
	public RangerPageGroup likeGroupByName(String groupName) {
		RangerPageGroup rangerPageGroup = new RangerPageGroup();
		String newUrl = rangerBaseUrl + rangerQueryGroupUrl;
		Map<String, Object> params = Maps.newHashMap();
		params.put(
				BigDataConfConstants.RANGER_QUERY_GROUP_API_PARAMS_GROUPNAME,
				groupName);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = RestTemplateUtil.restGet(
				restTemplate, newUrl, httpEntity, String.class, params);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger搜索用户信息接口失败");
		} else {
			rangerPageGroup = JSON.parseObject(responseEntity.getBody(),
					RangerPageGroup.class);
		}
		return rangerPageGroup;
	}

	/**
	 * 根据用户组查询用户
	 *
	 * @param groupName
	 *            用户组名称
	 * @return
	 */
	public RangerGroupUser getUsersByGroupName(String groupName) {
		RangerGroupUser rangerGroupUser = new RangerGroupUser();
		String newUrl = rangerBaseUrl + rangerQueryUsersByGroupNameUrl;
		newUrl = newUrl.concat("/").concat(groupName);
		HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<RangerGroupUser> responseEntity = restTemplate.exchange(
				newUrl, HttpMethod.GET, httpEntity, RangerGroupUser.class);
		if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
			log.error("调用Ranger根据用户ID获取用户信息接口失败");
		} else {
			rangerGroupUser = responseEntity.getBody();
		}
		return rangerGroupUser;
	}

	/**
	 * 添加一批用户到用户组
	 *
	 * @param groupName
	 *            用户组名称
	 * @param rangerUsers
	 *            用户名称列表
	 * @return
	 */
	public Boolean addUsersToGroup(String groupName,
			List<String> rangerUsers) {
		Boolean bool = false;
		try {
			ArrayList<VXUsers> newRangerUserList = new ArrayList<>();
			VXGroups rangerGroup = getGroupByName(groupName);
			for (String rangerUserName : rangerUsers) {
				VXUsers vxUsers = getUserByName(rangerUserName);
				VXUsers rangerUser = getUserById(vxUsers.getId());
				List<Integer> groupIdList = rangerUser.getGroupIdList();

				if (groupIdList == null) {
					groupIdList = new ArrayList<>();
					groupIdList.add(rangerGroup.getId());
					rangerUser.setGroupIdList(groupIdList);
					newRangerUserList.add(rangerUser);
				} else {
					boolean isExists = groupIdList
							.contains(rangerGroup.getId());
					if (!isExists) {
						groupIdList.add(rangerGroup.getId());
						rangerUser.setGroupIdList(groupIdList);
						newRangerUserList.add(rangerUser);
					}
				}
			}
			if (newRangerUserList.size() > 0) {
				for (VXUsers vxUsers : newRangerUserList) {
					updateRangerUser(vxUsers);
				}
				bool = true;
			} else {
				log.info("Ranger用户[" + rangerUsers.toString() + "]在["
						+ groupName + "]组中都存在,无需添加");
				return false;
			}
		} catch (Exception e) {
			log.error("添加Ranger用户[" + rangerUsers.toString() + "]到["
					+ groupName + "]组异常:", e);
		}
		return bool;
	}

	/**
	 * 从用户组中删除用户
	 *
	 * @param groupName
	 *            用户组名称
	 * @param usersName
	 *            用户名称
	 * @return
	 */
	public Boolean deleteUserToGroup(String groupName, String usersName) {
		Boolean flag = false;
		try {
			String newRangerDeleteUserFromGroupUrl = String.format(
					rangerDeleteUserFromGroupUrl, groupName, usersName);
			String newUrl = rangerBaseUrl + newRangerDeleteUserFromGroupUrl;
			HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<Object> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.DELETE, httpEntity, Object.class);
			if (HttpStatus.NO_CONTENT.value() != responseEntity
					.getStatusCodeValue()) {
				log.error("调用Ranger删除用户组接口失败");
				return false;
			} else {
				flag = true;
			}
		} catch (Exception e) {
			log.error(
					"删除Ranger用户[" + usersName + "]从[" + groupName + "]用户组异常:",
					e);
		}
		return flag;
	}

	/**
	 * 从用户组中删除一批用户
	 *
	 * @param groupName
	 *            用户组名称
	 * @param usersNames
	 *            用户名称列表
	 * @return
	 */
	public Boolean deleteUsersToGroup(String groupName,
			List<String> usersNames) {
		Boolean bool = false;
		try {
			ArrayList<String> deleteUserNames = new ArrayList<>();
			RangerGroupUser rangerGroupUser = getUsersByGroupName(groupName);
			List<VXUsers> xuserInfoList = rangerGroupUser.getXuserInfo();
			for (VXUsers rangerUser : xuserInfoList) {
				String name = rangerUser.getName();
				if (usersNames.contains(name)) {
					deleteUserNames.add(name);
				}
			}
			if (deleteUserNames.size() > 0) {
				for (String deleteUserName : deleteUserNames) {
					deleteUserToGroup(groupName, deleteUserName);
				}
				bool = true;
			} else {
				log.info("Ranger用户[" + usersNames.toString() + "]在["
						+ groupName + "]组中都不存在,无需删除");
				return false;
			}
		} catch (Exception e) {
			log.error("删除Ranger用户[" + usersNames.toString() + "]从[" + groupName
					+ "]用户组异常:", e);
		}
		return bool;
	}


	/**
	 * 新增Ranger策略
	 *
	 * @param
	 * @return
	 */
	public Boolean addRangerPolicy(RangerPolicy rangerPolicy) {
		try {
			String newUrl = rangerBaseUrl + rangerPolicyUrl;
			HttpEntity<String> httpEntity = new HttpEntity<>(
					JSONObject.toJSONString(rangerPolicy), headers);
			ResponseEntity<Object> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.POST, httpEntity, Object.class);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("调用Ranger添加策略接口失败",e);
			return false;
		}
	}

	/**
	 * 新增Ranger策略
	 *
	 * @param
	 * @return
	 */
	public String queryRangerPolicy(String clusterName, String serviceType,String policyName) {

		String result = "";
		String newUrl = rangerBaseUrl.concat(rangerPolicyQueryUrl).concat("/").concat(clusterName).concat("_");
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("pageSize","999999");
			params.put("policyNamePartial",policyName);
			if("hdfs".equalsIgnoreCase(serviceType)){
				newUrl = newUrl.concat(BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HDFS);
				params.put("serviceType","hdfs");
			}else if("hive".equalsIgnoreCase(serviceType)){
				newUrl = newUrl.concat(BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HIVE);
				params.put("serviceType","hive");
			}else if("hbase".equalsIgnoreCase(serviceType)){
				newUrl = newUrl.concat(BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_HBASE);
				params.put("serviceType","hbase");
			}else if("yarn".equalsIgnoreCase(serviceType)){
				newUrl = newUrl.concat(BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_YARN);
				params.put("serviceType","yarn");
			}else if("kafka".equalsIgnoreCase(serviceType)){
				newUrl = newUrl.concat(BigDataConfConstants.RANGER_SERVER_TYPE_SUFFIX_KAFKA);
				params.put("serviceType","kafka");
			}else {
				throw new RuntimeException("查询Ranger策略失败[服务类型serviceType无法匹配]");
			}
			HttpEntity<String> httpEntity = new HttpEntity<>(headers);
			newUrl = newUrl.concat("?policyNamePartial={policyNamePartial}&serviceType={serviceType}&pageSize={pageSize}");
			ResponseEntity<Object> responseEntity = restTemplate.exchange(
					newUrl, HttpMethod.GET, httpEntity, Object.class,params);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JSONObject resultJsonObj = JSON.parseObject(JSON.toJSONString(responseEntity.getBody()));
				result = resultJsonObj.getString("policies");
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("查询Ranger策略异常:",e);
			Result.failed("查询Ranger策略异常:",e.getMessage());
		}
		return result;
	}

	/**
	 * 删除Ranger策略
	 *
	 * @param
	 * @return
	 */
	public Boolean deleteRangerPolicy(String policyId) {
		Boolean flag = false;
		String newUrl = rangerBaseUrl.concat(rangerPolicyUrl).concat("/").concat(policyId);
		try {
			HttpEntity<String> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<Object> responseEntity = restTemplate.exchange(newUrl, HttpMethod.DELETE, httpEntity, Object.class);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				flag = true;
			}
		} catch (Exception e) {
			log.error("删除Ranger策略异常:",e);
		}
		return flag;
	}
}
