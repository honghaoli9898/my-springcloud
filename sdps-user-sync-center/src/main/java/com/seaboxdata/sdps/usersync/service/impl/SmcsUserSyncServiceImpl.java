package com.seaboxdata.sdps.usersync.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariRoleEnum;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncOperatorEnum;
import com.seaboxdata.sdps.usersync.feign.BigDataCommonProxyFeign;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsUserSyncInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.usersync.mapper.SysUserMapper;
import com.seaboxdata.sdps.usersync.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.usersync.service.UserSyncService;
import com.seaboxdata.sdps.usersync.utils.UserSyncUtil;

@Slf4j
@Service("smcsUserSyncService")
public class SmcsUserSyncServiceImpl implements UserSyncService {
	@Autowired
	private SysUserMapper sysUserMapper;

	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;

	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	private BigDataCommonProxyFeign bigdataCommonFegin;

	@Autowired
	private SdpsUserSyncInfoMapper userSyncInfoMapper;

	@Override
	public boolean userSyncAdd(UserSyncRequest userSyncRequest) {
		List<Integer> clusterIds = userSyncRequest.getClusterIds();
		AmbariUser ambariUser = null;
		SysUser sysUser = new SysUser();
		try {
			sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
			for (int i = 0; i < 5; i++) {
				if (Objects.nonNull(sysUser)) {
					break;
				}
				Thread.sleep(20);
				sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
			}
			if (Objects.isNull(sysUser)) {
				throw new BusinessException("未查到该用户id="
						+ userSyncRequest.getUserId());
			}
			SdpsServerInfo sdpsServerInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>()
							.eq("user", sysUser.getUsername())
							.eq("type", ServerTypeEnum.C.name())
							.eq("server_id", 0));
			SysGlobalArgs sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			String pass = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
					sysGlobalArgs.getArgValue());

			ambariUser = AmbariUser.builder().active(true).admin(false)
					.password(pass).user_name(sysUser.getUsername())
					.permission_name(AmbariRoleEnum.USER.getCode().toString())
					.build();
		} catch (Exception e) {
			log.error("同步用户id={},获取ambari用户信息失败", userSyncRequest.getUserId(),
					e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("ambari服务组件");
					userSyncInfo.setAssType("smcs");
					userSyncInfo.setClusterId(clusterId);
					userSyncInfo.setSyncResult(false);
					userSyncInfo.setUserId(userSyncRequest.getUserId());
					userSyncInfo.setUpdateTime(DateUtil.date());
					userSyncInfo
							.setOperator(UserSyncOperatorEnum.ADD.getCode());
					userSyncInfo.setUsername(sysUser.getUsername());
					userSyncInfo.setInfo(e.getMessage());
					try {
						userSyncInfoMapper
								.delete(new UpdateWrapper<SdpsUserSyncInfo>()
										.eq("user_id",
												userSyncRequest.getUserId())
										.eq("cluster_id", clusterId)
										.eq("operator",
												UserSyncOperatorEnum.ADD
														.getCode())
										.eq("ass_type", "smcs"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}
		boolean isSuccess = true;
		for (Integer clusterId : clusterIds) {
			Result result = null;
			try {
				result = bigdataCommonFegin.getAmbariUsers(clusterId);
				if (result.isSuccess()) {
					JSONArray users = (JSONArray) result.getData();
					boolean isExist = false;
					for (Object user : users) {
						JSONObject json = new JSONObject(
								(Map<String, Object>) user);
						if (StrUtil
								.equalsAnyIgnoreCase(
										json.getJSONObject("Users").getString(
												"user_name"),
										ambariUser.getUser_name())) {
							isExist = true;
							break;
						}
					}
					if (isExist) {
						result = bigdataCommonFegin.deleteAmbariUser(clusterId,
								ambariUser.getUser_name());
					}
				}
				result = bigdataCommonFegin
						.addAmbariUser(clusterId, ambariUser);
			} catch (Exception e) {
				log.error("调用增加ambari用户接口失败", e);
				result = Result.failed("调用增加ambari用户接口失败");
			}
			log.info("集群id={},ambari同步用户id={},调用大数据公共代理结果:{}", clusterId,
					userSyncRequest.getUserId(), result.isSuccess());
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("ambari服务组件");
			userSyncInfo.setAssType("smcs");
			userSyncInfo.setClusterId(clusterId);
			userSyncInfo.setSyncResult(result.isSuccess());
			userSyncInfo.setUserId(userSyncRequest.getUserId());
			userSyncInfo.setUpdateTime(DateUtil.date());
			userSyncInfo.setOperator(UserSyncOperatorEnum.ADD.getCode());
			userSyncInfo.setInfo(result.getMsg());
			userSyncInfo.setUsername(sysUser.getUsername());
			try {
				userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
						.eq("user_id", userSyncRequest.getUserId())
						.eq("cluster_id", clusterId)
						.eq("operator", UserSyncOperatorEnum.ADD.getCode())
						.eq("ass_type", "smcs"));
				userSyncInfoMapper.insert(userSyncInfo);
			} catch (Exception e) {
				log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	@Override
	public boolean userSyncDelete(UserSyncRequest userSyncRequest) {
		List<Integer> clusterIds = userSyncRequest.getClusterIds();
		SysUser sysUser = new SysUser();
		try {
			sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
		} catch (Exception e) {
			log.error("删除用户id={},获取ambari集群信息失败", userSyncRequest.getUserId(),
					e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("ambari服务组件");
					userSyncInfo.setAssType("smcs");
					userSyncInfo.setClusterId(clusterId);
					userSyncInfo.setSyncResult(false);
					userSyncInfo.setUserId(userSyncRequest.getUserId());
					userSyncInfo.setUpdateTime(DateUtil.date());
					userSyncInfo.setOperator(UserSyncOperatorEnum.DELETE
							.getCode());
					userSyncInfo.setUsername(sysUser.getUsername());
					userSyncInfo.setInfo(e.getMessage());
					try {
						userSyncInfoMapper
								.delete(new UpdateWrapper<SdpsUserSyncInfo>()
										.eq("user_id",
												userSyncRequest.getUserId())
										.eq("cluster_id", clusterId)
										.eq("operator",
												UserSyncOperatorEnum.DELETE
														.getCode())
										.eq("ass_type", "smcs"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}
		Result result;
		boolean isSuccess = true;
		for (Integer clusterId : clusterIds) {
			try {
				result = bigdataCommonFegin.getAmbariUsers(clusterId);
				if (result.isSuccess()) {
					JSONArray users = (JSONArray) result.getData();
					boolean isExist = false;
					for (Object user : users) {
						JSONObject json = new JSONObject(
								(Map<String, Object>) user);
						if (StrUtil.equalsAnyIgnoreCase(
								json.getJSONObject("Users").getString(
										"user_name"), sysUser.getUsername())) {
							isExist = true;
							break;
						}
					}
					if (isExist) {
						result = bigdataCommonFegin.deleteAmbariUser(clusterId,
								sysUser.getUsername());
					}
				}
			} catch (Exception e) {
				log.error("调用删除ambari用户接口失败", e);
				result = Result.failed("调用删除ambari用户接口失败");
			}
			log.info("集群id={},ambari删除用户id={},调用大数据公共代理结果:{}", clusterId,
					userSyncRequest.getUserId(), result.isSuccess());
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("ambari服务组件");
			userSyncInfo.setAssType("smcs");
			userSyncInfo.setUsername(sysUser.getUsername());
			userSyncInfo.setClusterId(clusterId);
			userSyncInfo.setSyncResult(result.isSuccess());
			userSyncInfo.setUserId(userSyncRequest.getUserId());
			userSyncInfo.setUpdateTime(DateUtil.date());
			userSyncInfo.setOperator(UserSyncOperatorEnum.DELETE.getCode());
			userSyncInfo.setInfo(result.getMsg());
			try {
				userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
						.eq("user_id", userSyncRequest.getUserId())
						.eq("cluster_id", clusterId)
						.eq("operator", UserSyncOperatorEnum.DELETE.getCode())
						.eq("ass_type", "smcs"));
				userSyncInfoMapper.insert(userSyncInfo);
			} catch (Exception e) {
				log.error("插入usersyncinfo={},信息失败", userSyncInfo, e);
				isSuccess = false;

			}
		}
		return isSuccess;
	}

	@Override
	public boolean userSyncUpdate(UserSyncRequest userSyncRequest) {
		String pass = null;
		List<Integer> clusterIds = userSyncRequest.getClusterIds();
		SysUser sysUser = new SysUser();
		try {
			Thread.sleep(1000);
			sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
			SdpsServerInfo sdpsServerInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>()
							.eq("user", sysUser.getUsername())
							.eq("type", ServerTypeEnum.C.name())
							.eq("server_id", 0));
			SysGlobalArgs sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			pass = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
					sysGlobalArgs.getArgValue());
		} catch (Exception e) {
			log.error("更改用户id={},获取ranger用户信息失败", userSyncRequest.getUserId(),
					e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("ambari服务组件");
					userSyncInfo.setAssType("smcs");
					userSyncInfo.setClusterId(clusterId);
					userSyncInfo.setUsername(sysUser.getUsername());
					userSyncInfo.setSyncResult(false);
					userSyncInfo.setUserId(userSyncRequest.getUserId());
					userSyncInfo.setUpdateTime(DateUtil.date());
					userSyncInfo.setOperator(UserSyncOperatorEnum.UPDATE
							.getCode());
					userSyncInfo.setInfo(e.getMessage());
					try {
						userSyncInfoMapper
								.delete(new UpdateWrapper<SdpsUserSyncInfo>()
										.eq("user_id",
												userSyncRequest.getUserId())
										.eq("cluster_id", clusterId)
										.eq("operator",
												UserSyncOperatorEnum.UPDATE
														.getCode())
										.eq("ass_type", "smcs"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}

		Result result = null;

		boolean isSuccess = true;
		for (Integer clusterId : clusterIds) {
			try {
				result = bigdataCommonFegin.updateAmbariUserPassword(
						clusterId,
						AmbariUser.builder().password(pass)
								.user_name(sysUser.getUsername())
								.old_password(pass).build());
			} catch (Exception e) {
				result = Result.failed(e.getMessage());
			}
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("ambari服务组件");
			userSyncInfo.setAssType("smcs");
			userSyncInfo.setClusterId(clusterId);
			userSyncInfo.setSyncResult(result.isSuccess());
			userSyncInfo.setUserId(userSyncRequest.getUserId());
			userSyncInfo.setUpdateTime(DateUtil.date());
			userSyncInfo.setUsername(sysUser.getUsername());
			userSyncInfo.setOperator(UserSyncOperatorEnum.UPDATE.getCode());
			userSyncInfo.setInfo(result.getMsg());
			try {
				userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
						.eq("user_id", userSyncRequest.getUserId())
						.eq("cluster_id", clusterId)
						.eq("operator", UserSyncOperatorEnum.UPDATE.getCode())
						.eq("ass_type", "smcs"));
				userSyncInfoMapper.insert(userSyncInfo);
				if (StrUtil.equalsAnyIgnoreCase(sysUser.getUsername(),
						CommonConstant.ADMIN_USER_NAME)) {
					SdpsServerInfo sdpsServerInfo = new SdpsServerInfo();
					sdpsServerInfo.setServerId(clusterId);
					sdpsServerInfo.setUser(sysUser.getUsername());
					sdpsServerInfo.setPasswd(UserSyncUtil
							.getEncryptPassword(pass));
					sdpsServerInfo.setType(ServerTypeEnum.A.name());
					serverInfoMapper.update(
							sdpsServerInfo,
							new UpdateWrapper<SdpsServerInfo>()
									.eq("user", sysUser.getUsername())
									.eq("server_id", clusterId)
									.eq("type", ServerTypeEnum.A.name()));
				}
			} catch (Exception e) {
				log.error("插入usersyncinfo={},信息失败", userSyncInfo, e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

}
