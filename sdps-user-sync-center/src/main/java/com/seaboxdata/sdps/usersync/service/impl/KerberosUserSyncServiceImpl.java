package com.seaboxdata.sdps.usersync.service.impl;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.core.utils.Base64Util;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncOperatorEnum;
import com.seaboxdata.sdps.usersync.feign.SeaboxProxyFegin;
import com.seaboxdata.sdps.usersync.kadmin.KadminCommandResult;
import com.seaboxdata.sdps.usersync.kadmin.KadminCommandsRunner;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminCreateKeytabCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminCreatePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminDeletePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminGetPrincipalsCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminUpdatePasswordCommand;
import com.seaboxdata.sdps.usersync.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerKeytabMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsUserSyncInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.usersync.mapper.SysUserMapper;
import com.seaboxdata.sdps.usersync.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.usersync.service.UserSyncService;

@Slf4j
@Service("kerberosUserSyncService")
public class KerberosUserSyncServiceImpl implements UserSyncService {
	private RestTemplate restTemplate = new RestTemplate(
			RestTemplateUtil.generateHttpsRequestFactory());
	@Autowired
	private KerberosProperties kerberosProperties;

	@Autowired
	private SdpsClusterMapper clusterMapper;

	@Autowired
	private SysUserMapper sysUserMapper;

	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;

	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	private SdpsUserSyncInfoMapper userSyncInfoMapper;

	@Autowired
	private SdpsServerKeytabMapper serverKeytabMapper;

	@Autowired
	private SeaboxProxyFegin seaboxProxyFegin;

	@Override
	public boolean userSyncAdd(UserSyncRequest userSyncRequest) {
		List<Integer> clusterIds = userSyncRequest.getClusterIds();
		SysUser sysUser = new SysUser();
		SysGlobalArgs sysGlobalArgs = null;
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
			sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			String pass = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
					sysGlobalArgs.getArgValue());

			sysUser.setPassword(pass);
		} catch (Exception e) {
			log.error("同步用户id={},获取kerberos用户信息失败",
					userSyncRequest.getUserId(), e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("ambari服务组件");
					userSyncInfo.setAssType("kerberos");
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
										.eq("ass_type", "kerberos"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}
		boolean isSuccess = true;
		Result result = Result.succeed("该集群未开启kerberos");
		for (Integer clusterId : clusterIds) {
			SdpsCluster sdpsCluster = clusterMapper
					.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
							clusterId));
			if (kerberosProperties.getEnable() && sdpsCluster.getKerberos()) {
				try {
					SdpsServerInfo serverInfo = serverInfoMapper
							.selectOne(new QueryWrapper<SdpsServerInfo>().eq(
									"server_id", sdpsCluster.getServerId()).eq(
									"type", ServerTypeEnum.KDC.name()));
					String pass = RsaUtil.decrypt(serverInfo.getPasswd(),
							sysGlobalArgs.getArgValue());
					KadminCommandsRunner kadminCommandsRunner = new KadminCommandsRunner(
							kerberosProperties.getAdminPrincipal(),
							kerberosProperties.getAdminKeytab(),
							serverInfo.getHost(), serverInfo.getUser(), pass,
							Integer.valueOf(serverInfo.getPort()));
					KadminCommandResult kadminCommandResult = kadminCommandsRunner
							.runCommand(
									new KadminGetPrincipalsCommand(sysUser
											.getUsername()), 1);
					if (kadminCommandResult.getOutput().contains(
							sysUser.getUsername())) {
						kadminCommandResult = kadminCommandsRunner.runCommand(
								new KadminDeletePrincipalCommand(sysUser
										.getUsername()), 1);
						if (!kadminCommandResult.getOutput()
								.contains("deleted")) {
							throw new BusinessException("刪除用失败");
						}
					}
					kadminCommandResult = kadminCommandsRunner.runCommand(
							new KadminCreatePrincipalCommand(sysUser
									.getUsername(), sysUser.getPassword()), 1);
					String keytabName = clusterId.toString().concat(".")
							.concat(sysUser.getUsername())
							.concat(".headless.keytab");
					if (kadminCommandResult.getOutput().contains("created")) {
						kadminCommandResult = kadminCommandsRunner
								.runCommand(
										new KadminCreateKeytabCommand(
												kerberosProperties
														.getKdcKeytabPath()
														.concat("/")
														.concat(keytabName),
												sysUser.getUsername()
														.concat(kerberosProperties
																.getUserSuffix())),
										1);
						if (kadminCommandResult.getOutput().contains("Entry")) {
							result = Result.succeed("调用kamin shell创建keytab成功");
							RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
									serverInfo.getHost(), serverInfo.getUser(),
									pass, Integer.valueOf(serverInfo.getPort()));
							String userSyncPath = kerberosProperties
									.getUserSyncKeytabPath();

							String userSyncFilePath = userSyncPath.concat("/")
									.concat(keytabName);
							isSuccess = remoteShellExecutorUtil.sftpDownload(
									kerberosProperties.getKdcKeytabPath()
											.concat("/").concat(keytabName),
									userSyncPath);
							result = upsertServerKeytab(sysUser.getUsername(),
									sdpsCluster, userSyncFilePath, keytabName,
									serverInfo.getHost());
							if (isSuccess) {
								result = Result
										.succeed("调用kamin shell创建keytab成功,并且拷贝keytab成功");
							} else {
								result = Result
										.failed("调用kamin shell创建keytab成功,但是拷贝keytab失败");
							}
						} else {
							result = Result.failed("调用kamin shell创建keytab失败");
						}

					} else {
						result = Result.failed("调用kamin shell创建用户失败");
					}
				} catch (Exception e) {
					log.error("调用kamin shell失败", e);
					result = Result.failed("调用kamin shell失败");
				}
			}
			log.info("集群id={},kerberos同步用户id={},调用kadmin shell结果:{}",
					clusterId, userSyncRequest.getUserId(), result.isSuccess());
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("kerberos服务组件");
			userSyncInfo.setAssType("kerberos");
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
						.eq("ass_type", "kerberos"));
				userSyncInfoMapper.insert(userSyncInfo);
			} catch (Exception e) {
				log.error("插入usersyncinfo={},信息失败", userSyncInfo, e);
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
			log.error("删除用户id={},获取kerberos集群信息失败",
					userSyncRequest.getUserId(), e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("kerberos服务组件");
					userSyncInfo.setAssType("kerberos");
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
										.eq("ass_type", "kerberos"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}
		Result result = Result.succeed("该集群未开启kerberos");
		boolean isSuccess = true;
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		for (Integer clusterId : clusterIds) {
			SdpsCluster sdpsCluster = clusterMapper
					.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
							clusterId));
			if (kerberosProperties.getEnable() && sdpsCluster.getKerberos()) {
				try {
					SdpsServerInfo serverInfo = serverInfoMapper
							.selectOne(new QueryWrapper<SdpsServerInfo>().eq(
									"server_id", sdpsCluster.getServerId()).eq(
									"type", ServerTypeEnum.KDC.name()));
					String pass = RsaUtil.decrypt(serverInfo.getPasswd(),
							sysGlobalArgs.getArgValue());
					KadminCommandsRunner kadminCommandsRunner = new KadminCommandsRunner(
							kerberosProperties.getAdminPrincipal(),
							kerberosProperties.getAdminKeytab(),
							serverInfo.getHost(), serverInfo.getUser(), pass,
							Integer.valueOf(serverInfo.getPort()));
					KadminCommandResult kadminCommandResult = kadminCommandsRunner
							.runCommand(
									new KadminGetPrincipalsCommand(sysUser
											.getUsername()), 1);
					if (kadminCommandResult.getOutput().contains(
							sysUser.getUsername())) {
						kadminCommandResult = kadminCommandsRunner.runCommand(
								new KadminDeletePrincipalCommand(sysUser
										.getUsername()), 1);
						if (kadminCommandResult.getOutput().contains("deleted")) {
							result = Result
									.succeed("调用kadmin shell删除用户:".concat(
											sysUser.getUsername()).concat("成功"));
						} else {
							result = Result
									.failed("调用kadmin shell删除用户:".concat(
											sysUser.getUsername()).concat("失败"));
						}
					} else {
						result = Result.succeed("用户:".concat(
								sysUser.getUsername()).concat("已经被删除"));
					}
					serverKeytabMapper
							.delete(new UpdateWrapper<SdpServerKeytab>()
									.eq("cluster_id",
											sdpsCluster.getClusterId())
									.eq("principal_name",
											sysUser.getUsername().concat(
													kerberosProperties
															.getUserSuffix()))
									.eq("principal_type",
											SecurityConstants.SDPS_USER_PRINCIPAL_TYPE));

				} catch (Exception e) {
					log.error("调用kadmin shell删除用户{}失败", sysUser.getUsername(),
							e);
					result = Result.failed("调用kadmin shell删除用户:".concat(
							sysUser.getUsername()).concat("失败"));
				}
			}
			log.info("集群id={},kerberos删除用户id={},调用kadmin shell结果:{}",
					clusterId, userSyncRequest.getUserId(), result.isSuccess());
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("kerberos服务组件");
			userSyncInfo.setAssType("kerberos");
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
						.eq("ass_type", "kerberos"));
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
		SysGlobalArgs sysGlobalArgs = null;
		try {
			Thread.sleep(1000);
			sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
			SdpsServerInfo sdpsServerInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>()
							.eq("user", sysUser.getUsername())
							.eq("type", ServerTypeEnum.C.name())
							.eq("server_id", 0));
			sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			pass = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
					sysGlobalArgs.getArgValue());
		} catch (Exception e) {
			log.error("更改用户id={},获取kerberos用户信息失败",
					userSyncRequest.getUserId(), e);
			if (CollUtil.isNotEmpty(clusterIds)) {
				for (Integer clusterId : clusterIds) {
					SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
					userSyncInfo.setAssName("kerberos服务组件");
					userSyncInfo.setAssType("kerberos");
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
										.eq("ass_type", "kerberos"));
						userSyncInfoMapper.insert(userSyncInfo);
					} catch (Exception e1) {
						log.error("插入或删除usersyncinfo={},信息失败", userSyncInfo, e1);
					}
				}
			}
			return false;
		}

		Result result = Result.succeed("该集群未开启kerberos");
		boolean isSuccess = true;
		for (Integer clusterId : clusterIds) {
			SdpsCluster sdpsCluster = clusterMapper
					.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
							clusterId));
			if (kerberosProperties.getEnable() && sdpsCluster.getKerberos()) {
				try {
					SdpsServerInfo serverInfo = serverInfoMapper
							.selectOne(new QueryWrapper<SdpsServerInfo>().eq(
									"server_id", sdpsCluster.getServerId()).eq(
									"type", ServerTypeEnum.KDC.name()));
					String rootPass = RsaUtil.decrypt(serverInfo.getPasswd(),
							sysGlobalArgs.getArgValue());
					KadminCommandsRunner kadminCommandsRunner = new KadminCommandsRunner(
							kerberosProperties.getAdminPrincipal(),
							kerberosProperties.getAdminKeytab(),
							serverInfo.getHost(), serverInfo.getUser(),
							rootPass, Integer.valueOf(serverInfo.getPort()));
					KadminCommandResult kadminCommandResult = kadminCommandsRunner
							.runCommand(
									new KadminUpdatePasswordCommand(sysUser
											.getUsername(), pass), 1);
					if (kadminCommandResult.getOutput().contains("changed")) {
						result = Result.succeed("调用kadmin shell修改用户:".concat(
								sysUser.getUsername()).concat("的密码成功"));
						String keytabName = clusterId.toString().concat(".")
								.concat(sysUser.getUsername())
								.concat(".headless.keytab");
						kadminCommandResult = kadminCommandsRunner
								.runCommand(
										new KadminCreateKeytabCommand(
												kerberosProperties
														.getKdcKeytabPath()
														.concat("/")
														.concat(keytabName),
												sysUser.getUsername()
														.concat(kerberosProperties
																.getUserSuffix())),
										1);
						if (kadminCommandResult.getOutput().contains("Entry")) {
							result = Result.succeed("调用kamin shell修改用户:"
									.concat(sysUser.getUsername())
									.concat("的密码成功").concat(",并且创建keytab成功"));
							RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
									serverInfo.getHost(), serverInfo.getUser(),
									pass, Integer.valueOf(serverInfo.getPort()));
							String keyTabFilePath = kerberosProperties
									.getUserSyncKeytabPath();
							isSuccess = remoteShellExecutorUtil.sftpDownload(
									kerberosProperties.getKdcKeytabPath()
											.concat("/").concat(keytabName),
									keyTabFilePath);
							result = upsertServerKeytab(sysUser.getUsername(),
									sdpsCluster, keyTabFilePath, keytabName,
									serverInfo.getHost());
							if (isSuccess) {
								result = Result.succeed("调用kamin shell修改用户:"
										.concat(sysUser.getUsername())
										.concat("的密码成功").concat(",创建keytab成功")
										.concat("并且拷贝keytab成功"));
							} else {
								result = Result.failed("调用kamin shell修改用户:"
										.concat(sysUser.getUsername())
										.concat("的密码成功").concat(",创建keytab成功")
										.concat("但是拷贝keytab失败"));
							}
						} else {
							result = Result.failed("调用kamin shell修改用户:"
									.concat(sysUser.getUsername())
									.concat("的密码成功").concat(",但是创建keytab失败"));
						}
					} else {
						result = Result.failed("调用kadmin shell修改用户:".concat(
								sysUser.getUsername()).concat("的密码失败"));
					}
				} catch (Exception e) {
					log.error("调用kadmin shell修改用户{}的密码失败",
							sysUser.getUsername(), e);
					result = Result.failed("调用kadmin shell修改用户:".concat(
							sysUser.getUsername()).concat("的密码失败"));
				}
			}
			SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
			userSyncInfo.setAssName("kerberos服务组件");
			userSyncInfo.setAssType("kerberos");
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
						.eq("ass_type", "kerberos"));
				userSyncInfoMapper.insert(userSyncInfo);
				// if (StrUtil.equalsAnyIgnoreCase(sysUser.getUsername(),
				// CommonConstant.ADMIN_USER_NAME)) {
				// SdpsServerInfo sdpsServerInfo = new SdpsServerInfo();
				// sdpsServerInfo.setServerId(clusterId);
				// sdpsServerInfo.setUser(sysUser.getUsername());
				// sdpsServerInfo.setPasswd(UserSyncUtil
				// .getEncryptPassword(pass));
				// sdpsServerInfo.setType(ServerTypeEnum.A.name());
				// serverInfoMapper.update(
				// sdpsServerInfo,
				// new UpdateWrapper<SdpsServerInfo>()
				// .eq("user", sysUser.getUsername())
				// .eq("server_id", clusterId)
				// .eq("type", ServerTypeEnum.A.name()));
				// }
			} catch (Exception e) {
				log.error("插入usersyncinfo={},信息失败", userSyncInfo, e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	private Result upsertServerKeytab(String username, SdpsCluster sdpsCluster,
			String keytabPath, String keytabName, String host) {
		Result result = null;
		long cnt = serverKeytabMapper
				.selectCount(new QueryWrapper<SdpServerKeytab>()
						.eq("cluster_id", sdpsCluster.getClusterId())
						.eq("principal_name",
								username.concat(kerberosProperties
										.getUserSuffix()))
						.eq("principal_type",
								SecurityConstants.SDPS_USER_PRINCIPAL_TYPE));
		boolean isCreate = cnt == 0;
		SdpServerKeytab sdpServerKeytab = SdpServerKeytab
				.builder()
				.clusterId(sdpsCluster.getClusterId())
				.clusterName(sdpsCluster.getClusterName())
				.createTime(isCreate ? DateUtil.date() : null)
				.updateTime(DateUtil.date())
				.keytabFileName(keytabName)
				.keytabFilePath(keytabPath)
				.keytabContent(Base64Util.convertFileToStr(keytabPath))
				.component(SecurityConstants.SDPS_USER_PRINCIPAL_TYPE)
				.description(username)
				.keytabFileMode("400")
				.keytabFileOwner(SecurityConstants.SDPS_USER_PRINCIPAL_TYPE)
				.keytabFileGroup(SecurityConstants.SDPS_USER_PRINCIPAL_TYPE)
				.localUsername(username)
				.host(host)
				.principalName(
						username.concat(kerberosProperties.getUserSuffix()))
				.principalType(SecurityConstants.SDPS_USER_PRINCIPAL_TYPE)
				.keytabFileInstalled(true).build();
		if (isCreate) {
			serverKeytabMapper.insert(sdpServerKeytab);
		} else {
			serverKeytabMapper
					.update(sdpServerKeytab,
							new UpdateWrapper<SdpServerKeytab>()
									.eq("cluster_id",
											sdpsCluster.getClusterId())
									.eq("principal_name",
											username.concat(kerberosProperties
													.getUserSuffix()))
									.eq("principal_type",
											SecurityConstants.SDPS_USER_PRINCIPAL_TYPE));
		}
		SpringClientFactory springClientFactory = SpringUtil
				.getBean(SpringClientFactory.class);
		ILoadBalancer loadBalancerUserSync = springClientFactory
				.getLoadBalancer(ServiceNameConstants.USER_SYNC_SERVER);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<Server> serversUserSync = loadBalancerUserSync
				.getReachableServers();
		for (Server server : serversUserSync) {
			String url = "http://".concat(server.getHostPort()).concat(
					"/usersync/updateKeytab");
			HttpEntity<Object> httpEntity = new HttpEntity<>(
					JSONArray.toJSONString(CollUtil
							.newArrayList(sdpServerKeytab)), headers);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, String.class);
			if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
				return Result.failed("调用usersync同步keytab文件报错,url=".concat(url));
			} else {
				result = JSONObject.parseObject(responseEntity.getBody(),
						Result.class);
				if (result.isFailed()) {
					return result;
				}
			}
		}
		ILoadBalancer loadBalancerSeabox = springClientFactory
				.getLoadBalancer(ServiceNameConstants.SEABOX_PROXY_SERVICE);
		List<Server> serversSeabox = loadBalancerSeabox.getReachableServers();
		sdpServerKeytab.setKeytabFilePath(kerberosProperties
				.getSeaboxKeytabPath().concat("/").concat(keytabName));
		for (Server server : serversSeabox) {
			String url = "http://".concat(server.getHostPort()).concat(
					"/seaboxKeytab/updateKeytab");
			HttpEntity<Object> httpEntity = new HttpEntity<>(
					JSONArray.toJSONString(CollUtil
							.newArrayList(sdpServerKeytab)), headers);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, String.class);
			if (HttpStatus.OK.value() != responseEntity.getStatusCodeValue()) {
				return Result.failed("调用seabox同步keytab文件报错,url=".concat(url));
			} else {
				result = JSONObject.parseObject(responseEntity.getBody(),
						Result.class);
				if (result.isFailed()) {
					return result;
				}
			}
		}
		return Result.succeed("同步用户keytab成功");

	}

}
