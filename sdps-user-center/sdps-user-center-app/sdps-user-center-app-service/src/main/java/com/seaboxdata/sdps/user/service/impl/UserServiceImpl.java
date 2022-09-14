package com.seaboxdata.sdps.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.lock.DistributedLock;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.UrlUserVo;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncOperatorEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncStatusEnum;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.user.api.IOrganizationService;
import com.seaboxdata.sdps.user.api.IRoleService;
import com.seaboxdata.sdps.user.api.IUserGroupService;
import com.seaboxdata.sdps.user.api.IUserRoleItemService;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.api.ServerLogin;
import com.seaboxdata.sdps.user.enums.TypeEnum;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.user.feign.UserSyncCenterFegin;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserDto;
import com.seaboxdata.sdps.user.mybatis.mapper.RoleMenuMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsUserSyncInfoMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysGroupMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysUserMapper;
import com.seaboxdata.sdps.user.mybatis.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;
import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;
import com.seaboxdata.sdps.user.vo.user.CreateUserVo;
import com.seaboxdata.sdps.user.vo.user.ImportUserCsvVo;
import com.seaboxdata.sdps.user.vo.user.PageUserRequest;
import com.seaboxdata.sdps.user.vo.user.UserVo;

@Service
public class UserServiceImpl extends SuperServiceImpl<SysUserMapper, SysUser>
		implements IUserService {
	@Autowired
	private KerberosProperties kerberosProperties;

	private final static String LOCK_KEY_USERNAME = "username:";

	private static final String login_cert_redis_key = "server_login_cert:";

	private static final String redis_join = ":";
	@Autowired
	private RedisRepository redisRepository;
	@Autowired
	private final Map<String, ServerLogin> serverLoginMap = new ConcurrentHashMap<>();
	@Autowired
	private DistributedLock lock;
	@Autowired
	private IUserGroupService userGroupService;
	@Autowired
	private IUserRoleItemService userRoleItemService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IOrganizationService organizationService;
	@Autowired
	private SysGroupMapper groupMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleMenuMapper roleMenuMapper;
	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;
	@Autowired
	private SdpsServerInfoMapper rangerInfoMapper;
	@Autowired
	private BigdataCommonFegin bigdataCommonFegin;
	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private UserSyncCenterFegin userSyncCenterFegin;
	@Autowired
	private SdpsUserSyncInfoMapper userSyncInfoMapper;

	private SysUser createUserVoToSysUser(CreateUserVo createUserVo) {
		SysUser user = new SysUser();
		user.setEmail(createUserVo.getEmail());
		user.setUsername(createUserVo.getUsername());
		user.setNickname(createUserVo.getNickname());
		user.setPassword(passwordEncoder.encode(createUserVo.getPassword()));
		user.setSex(createUserVo.getSex());
		user.setEnabled(!createUserVo.getSyncUser());
		user.setDesc(createUserVo.getDesc());
		user.setSyncClusterIds(CollUtil.join(createUserVo.getClusterIds(), ","));
		if (createUserVo.getSyncUser()) {
			user.setSyncUserStatus(UserSyncStatusEnum.SYNCING.getCode());
		} else {
			user.setSyncUserStatus(UserSyncStatusEnum.NOT.getCode());
		}

		return user;
	}

	@Override
	public PageResult<SysUser> findUsers(PageUserRequest request) {
//		SysRole sysRole = roleService.getOne(new QueryWrapper<SysRole>().eq(
//				"code", "tenantManager"));
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SysUser> users = baseMapper.findUsersInfo(request.getParam());
		if (CollUtil.isNotEmpty(users.getResult())) {
			List<Long> userIds = users.stream().map(SysUser::getId)
					.collect(Collectors.toList());
			List<SysRole> roles = roleService.findRoleByUserIds(userIds,
					TypeEnum.S.getCode().toString());
			Map<Long, List<SysRole>> roleUserMap = roles.stream().collect(
					Collectors.groupingBy(SysRole::getUserId));

			users.forEach(user -> {
				user.setRoles(roleUserMap.get(user.getId()));
			});
		}
		return PageResult.<SysUser> builder().code(0).data(users)
				.count(users.getTotal()).msg("操作成功").build();
	}

	private SdpsServerInfo getSdpsRangerInfo(String username, String password) {
		SdpsServerInfo sdpsServerInfo = new SdpsServerInfo();
		sdpsServerInfo.setServerId(0);
		sdpsServerInfo.setHost("0.0.0.0");
		sdpsServerInfo.setPort("0");
		sdpsServerInfo.setUser(username);
		sdpsServerInfo.setPasswd(password);
		sdpsServerInfo.setType(ServerTypeEnum.C.name());
		return sdpsServerInfo;
	}

	@Transactional
	private SysUser createUserInfo(CreateUserVo createUserVo) {
		SysUser user = createUserVoToSysUser(createUserVo);
		save(user);
		Long userId = user.getId();
		SysRole sysRole = roleService.getOne(new QueryWrapper<SysRole>().eq(
				"code", "tenantManager"));
		if (CollUtil.isNotEmpty(createUserVo.getIds())) {
			List<UserRoleItem> userRoleItems = createUserVo.getIds().stream()
					.map(id -> {
						UserRoleItem userRoleItem = new UserRoleItem();
						userRoleItem.setUserId(userId);
						userRoleItem.setRoleId(id);
						return userRoleItem;
					}).collect(Collectors.toList());
			createUserVo.getTenantIds().stream().map(id -> {
				UserRoleItem userRoleItem = new UserRoleItem();
				userRoleItem.setUserId(userId);
				userRoleItem.setRoleId(sysRole.getId());
				userRoleItem.setTenantId(id);
				return userRoleItem;
			}).forEach(userRoleItems::add);
			userRoleItemService.insertBatch(userRoleItems);
		}
		rangerInfoMapper.insert(getSdpsRangerInfo(user.getUsername(),
				getEncryptPassword(createUserVo.getPassword())));
		return user;
	}

	@Override
	public void createUserAllInfo(CreateUserVo createUserVo) {
		if (!createUserVo.getPassword().matches(SecurityConstants.PASS_REG)) {
			throw new BusinessException("密码必须包含数字字母特殊字符");
		}
		SysUser user = createUserInfo(createUserVo);
		try {
			UserSyncRequest request = new UserSyncRequest();
			request.setOperation(UserSyncOperatorEnum.ADD.getCode());
			request.setSyncTypes(UserSyncEnum.getAllCode());
			request.setUserId(user.getId());
			request.setClusterIds(createUserVo.getClusterIds());
			if (createUserVo.getSyncUser()
					&& CollUtil.isEmpty(createUserVo.getClusterIds())) {
				throw new BusinessException("同步集群不能为空");
			}
			if (createUserVo.getSyncUser()
					&& CollUtil.isNotEmpty(createUserVo.getClusterIds())) {
				Result result = userSyncCenterFegin.userSynOperator(request);
				if (result.isFailed()) {
					throw new BusinessException("调用用户同步微服务报错");
				}
			}
		} catch (Exception e) {
			log.error("同步用户异常", e);
			if (user != null && user.getId() != null) {
				removeById(user.getId());
			}
		}

	}

	private void deleteUsersInfo(List<Long> ids, List<String> usernames) {
		removeByIds(ids);
		userRoleItemService.remove(new QueryWrapper<UserRoleItem>().in(
				"user_id", ids));
		userGroupService.remove(new QueryWrapper<UserGroup>()
				.in("user_id", ids));
		rangerInfoMapper.delete(new UpdateWrapper<SdpsServerInfo>()
				.eq("server_id", 0).eq("type", ServerTypeEnum.C.name())
				.in("user", usernames));
	}

	@Override
	@Transactional
	public void deleteUserAllInfo(List<Long> ids) {
		if (CollUtil.isNotEmpty(ids)) {
			List<SysUser> sysUsers = this.list(new QueryWrapper<SysUser>()
					.select("username", "id").in("id", ids));
			sysUsers.forEach(user -> user.setEnabled(false));
			this.updateBatchById(sysUsers, 100);
			List<SdpsUserSyncInfo> syncUserInfos = userSyncInfoMapper
					.selectList(new QueryWrapper<SdpsUserSyncInfo>()
							.select("user_id",
									"group_concat(cluster_id) as clusterIds")
							.in("user_id", ids)
							.eq("operator", UserSyncOperatorEnum.ADD.getCode())
							.eq("sync_result", true).groupBy("user_id"));
			List<String> usernames = sysUsers.stream()
					.map(SysUser::getUsername).collect(Collectors.toList());
			if (CollUtil.isEmpty(syncUserInfos)) {
				deleteUsersInfo(ids, usernames);
			}
			syncUserInfos
					.forEach(info -> {
						if (StrUtil.isBlank(info.getClusterIds())) {
							deleteUsersInfo(ids, usernames);
						} else {
							UserSyncRequest request = new UserSyncRequest();
							request.setOperation(UserSyncOperatorEnum.DELETE
									.getCode());
							request.setSyncTypes(UserSyncEnum.getAllCode());
							request.setUserIds(ids);
							List<Integer> clusterIds = (List<Integer>) Convert.toCollection(
									ArrayList.class, Integer.class,
									info.getClusterIds());
							request.setClusterIds(clusterIds);
							Result result = userSyncCenterFegin
									.userSynOperator(request);
							if (result.isFailed()) {
								throw new BusinessException("调用异步删除组件用户接口失败");
							}
						}
					});
		}
	}

	@Override
	@Transactional
	public void insertUserItemRole(UserVo userVo) {
		if (Objects.isNull(userVo.getItemId())
				&& Objects.isNull(userVo.getTenantId())) {
			throw new BusinessException("租户id或者项目id不能为空");
		}
		userVo.getRoles().forEach(role -> {
			role.setItemId(userVo.getItemId());
			role.setUserId(userVo.getUserId());
			role.setTenantId(userVo.getTenantId());
		});
		userRoleItemService.insertBatch(userVo.getRoles());
	}

	@Override
	@Transactional
	public void updateUserItemRole(UserVo userVo) {
		if (StrUtil.equalsIgnoreCase("item", userVo.getType())) {
			userRoleItemService.remove(new QueryWrapper<UserRoleItem>().eq(
					"item_id", userVo.getItemId()).eq("user_id",
					userVo.getUserId()));
		} else if (StrUtil.equalsIgnoreCase("tenant", userVo.getType())) {
			userRoleItemService.remove(new QueryWrapper<UserRoleItem>().eq(
					"tenant_id", userVo.getTenantId()).eq("user_id",
					userVo.getUserId()));
		} else {
			throw new BusinessException("修改类型不合法");
		}
		userVo.getRoles().forEach(role -> {
			role.setItemId(userVo.getItemId());
			role.setUserId(userVo.getUserId());
			role.setTenantId(userVo.getTenantId());
		});
		userRoleItemService.insertBatch(userVo.getRoles());
	}

	@Override
	@Transactional
	public void removeUserFromGroups(UserRequest request) {
		userGroupService.remove(new QueryWrapper<UserGroup>().eq("user_id",
				request.getId()).in("group_id", request.getIds()));
	}

	@Override
	public void addUserToGroup(List<UserGroup> userGroup) {
		userGroupService.insertBatch(userGroup);
	}

	@Override
	public List<SysUser> findUserList(UserRequest sysUser) {
		return baseMapper.findUsersByExample(sysUser);
	}

	@Override
	@Transactional
	public void updateUserInfo(SysUser sysUser) {
		if (StrUtil.isNotBlank(sysUser.getPassword())) {
			if (!sysUser.getPassword().matches(SecurityConstants.PASS_REG)) {
				throw new BusinessException("密码必须包含数字字母特殊字符");
			}
			sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
		}
		update(sysUser, new UpdateWrapper<SysUser>().eq("id", sysUser.getId()));
		if (CollUtil.isNotEmpty(sysUser.getRoleIds())) {
			Set<Long> sysRoleIds = roleService
					.list(new QueryWrapper<SysRole>().eq("type", TypeEnum.S
							.getCode().toString())).stream()
					.map(SysRole::getId).collect(Collectors.toSet());
			userRoleItemService.remove(new QueryWrapper<UserRoleItem>().in(
					"role_id", sysRoleIds).eq("user_id", sysUser.getId()));

			userRoleItemService.insertBatch(sysUser.getRoleIds().stream()
					.map(id -> {
						UserRoleItem userRoleItem = new UserRoleItem();
						userRoleItem.setUserId(sysUser.getId());
						userRoleItem.setRoleId(id);
						return userRoleItem;
					}).collect(Collectors.toList()));
		}
		if (CollUtil.isNotEmpty(sysUser.getTenantList())) {
			Set<Long> sysRoleIds = roleService
					.list(new QueryWrapper<SysRole>().eq("type", TypeEnum.T
							.getCode().toString())).stream()
					.map(SysRole::getId).collect(Collectors.toSet());
			Long tenantRoleId = sysRoleIds.iterator().next();
			userRoleItemService.remove(new QueryWrapper<UserRoleItem>().in(
					"role_id", sysRoleIds).eq("user_id", sysUser.getId()));

			userRoleItemService.insertBatch(sysUser.getTenantList().stream()
					.map(id -> {
						UserRoleItem userRoleItem = new UserRoleItem();
						userRoleItem.setUserId(sysUser.getId());
						userRoleItem.setRoleId(tenantRoleId);
						userRoleItem.setTenantId(id);
						return userRoleItem;
					}).collect(Collectors.toList()));
		}

	}

	@Override
	@Transactional
	public void batchImportUserInfo(List<ImportUserCsvVo> rows) {
		if (CollUtil.isEmpty(rows)) {
			return;
		}
		List<SysUser> users = CollUtil.newArrayList();
		// List<Integer> clusterIds =
		// rangerInfoMapper.selectClusterIdHasRanger();
		List<ImportUserCsvVo> filterList = rows
				.stream()
				.filter(row -> {
					if (StrUtil.isNotBlank(row.getUsername())
							&& StrUtil.isNotBlank(row.getNickname())
							&& StrUtil.isNotBlank(row.getEmail())) {
						if (count(new QueryWrapper<SysUser>().eq("username",
								row.getUsername())) > 0) {
							return false;
						}
						return true;
					}
					return false;
				}).collect(Collectors.toList());
		// List<VXUsers> vxUsers = CollUtil.newArrayList();
		// List<AmbariUser> ambariUsers = CollUtil.newArrayList();
		List<SdpsServerInfo> rangerInfos = CollUtil.newArrayList();
		List<UserRoleItem> userRoleItems = CollUtil.newArrayList();
		filterList.forEach(row -> {
			SysUser user = new SysUser();
			user.setUsername(row.getUsername());
			user.setPassword(passwordEncoder
					.encode(CommonConstant.DEF_USER_PASSWORD));
			user.setNickname(row.getNickname());
			user.setSex(row.getSex());
			user.setEmail(row.getEmail());
			user.setDesc(row.getDesc());
			user.setEnabled(false);
			users.add(user);
			// vxUsers.add(VXUsers.builder().firstName(user.getUsername())
			// .name(user.getUsername())
			// .password(CommonConstant.DEF_USER_PASSWORD).build());
			// ambariUsers.add(AmbariUser.builder().active(true).admin(false)
			// .password(CommonConstant.DEF_USER_PASSWORD)
			// .user_name(user.getUsername())
			// .permission_name(AmbariRoleEnum.USER.getCode().toString())
			// .build());
				rangerInfos.add(getSdpsRangerInfo(row.getUsername(),
						getEncryptPassword(CommonConstant.DEF_USER_PASSWORD)));
				userRoleItems.addAll(CollUtil
						.newArrayList(StrUtil.split(row.getRoles(), "|"))
						.stream().map(role -> {
							UserRoleItem userRoleItem = new UserRoleItem();
							userRoleItem.setRoleId(Long.valueOf(role));
							userRoleItem.setUsername(user.getUsername());
							return userRoleItem;
						}).collect(Collectors.toList()));
			});
		this.baseMapper.insertBatchSomeColumn(users);
		Map<String, List<SysUser>> userMap = users.stream().collect(
				Collectors.groupingBy(SysUser::getUsername));
		userRoleItems.forEach(item -> {
			item.setUserId(userMap.get(item.getUsername()).get(0).getId());
		});
		List<Long> userIds = users.stream().map(SysUser::getId)
				.collect(Collectors.toList());
		rangerInfoMapper.insertBatchSomeColumn(rangerInfos);
		userRoleItemService.insertBatch(userRoleItems);
		UserSyncRequest request = new UserSyncRequest();
		request.setOperation(UserSyncOperatorEnum.ADD.getCode());
		request.setSyncTypes(UserSyncEnum.getAllCode());
		request.setUserIds(userIds);
		List<Integer> clusterIds = (List<Integer>) Convert.toCollection(
				ArrayList.class, Integer.class, rows.get(0).getClusterIds());
		request.setClusterIds(clusterIds);
		Result result = userSyncCenterFegin.userSynOperator(request);
		if (result.isFailed()) {
			throw new BusinessException("调用用户同步微服务报错");
		}
		// clusterIds.forEach(id -> {
		// bigdataCommonFegin.addRangerUser(id, vxUsers);
		// ambariUsers.forEach(user -> {
		// bigdataCommonFegin.addAmbariUser(id, user);
		// });
		// });
	}

	@Override
	public LoginAppUser findByUsername(String username) {
		SysUser sysUser = this.selectByUsername(username);
		return getLoginAppUser(sysUser);
	}

	@Override
	public LoginAppUser getLoginAppUser(SysUser sysUser) {
		if (sysUser != null) {
			LoginAppUser loginAppUser = new LoginAppUser();
			BeanUtils.copyProperties(sysUser, loginAppUser);

			List<SysRole> sysRoles = userRoleItemService.findRolesByUserId(
					sysUser.getId(), TypeEnum.S.name());
			// 设置角色
			loginAppUser.setRoles(sysRoles);

			if (!CollectionUtils.isEmpty(sysRoles)) {
				Set<Long> roleIds = sysRoles.stream().map(SuperEntity::getId)
						.collect(Collectors.toSet());
				List<SysMenu> menus = roleMenuMapper.findMenusByRoleIds(
						roleIds, CommonConstant.PERMISSION);
				if (!CollectionUtils.isEmpty(menus)) {
					Set<String> permissions = menus.stream()
							.map(p -> p.getPath()).collect(Collectors.toSet());
					// 设置权限集合
					loginAppUser.setPermissions(permissions);
				}
			}
			return loginAppUser;
		}
		return null;
	}

	@Override
	public SysUser selectByUsername(String username) {
		List<SysUser> users = baseMapper.selectList(new QueryWrapper<SysUser>()
				.eq("username", username));
		return getUser(users);
	}

	private SysUser getUser(List<SysUser> users) {
		SysUser user = null;
		if (users != null && !users.isEmpty()) {
			user = users.get(0);
		}
		return user;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Result saveOrUpdateUser(SysUser sysUser) throws Exception {
		sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
		sysUser.setEnabled(Boolean.TRUE);
		String username = sysUser.getUsername();
		boolean result = super.saveOrUpdateIdempotency(sysUser, lock,
				LOCK_KEY_USERNAME + username,
				new QueryWrapper<SysUser>().eq("username", username), username
						+ "已存在");
		// 更新角色
		if (result && !CollectionUtils.isEmpty(sysUser.getRoleIds())) {
			Set<Long> sysRoleIds = roleService
					.list(new QueryWrapper<SysRole>().eq("type", TypeEnum.S
							.getCode().toString())).stream()
					.map(SysRole::getId).collect(Collectors.toSet());
			userRoleItemService.remove(new QueryWrapper<UserRoleItem>().in(
					"role_id", sysRoleIds).eq("user_id", sysUser.getId()));
			if (CollUtil.isNotEmpty(sysUser.getRoleIds())) {
				userRoleItemService.insertBatch(sysUser.getRoleIds().stream()
						.map(id -> {
							UserRoleItem userRoleItem = new UserRoleItem();
							userRoleItem.setUserId(sysUser.getId());
							userRoleItem.setRoleId(id);
							return userRoleItem;
						}).collect(Collectors.toList()));
			}

		}
		return result ? Result.succeed(sysUser, "操作成功") : Result.failed("操作失败");
	}

	@Transactional
	private void updatePassword(SysUser sysUser, String newPassword,
			Boolean syncUser) {
		SysUser user = new SysUser();
		user.setId(sysUser.getId());
		user.setPassword(passwordEncoder.encode(newPassword));
		baseMapper.updateById(user);
		if (syncUser) {
			SdpsServerInfo rangerInfo = getSdpsRangerInfo(
					sysUser.getUsername(), getEncryptPassword(newPassword));
			rangerInfoMapper.update(
					rangerInfo,
					new UpdateWrapper<SdpsServerInfo>()
							.eq("user", sysUser.getUsername())
							.eq("server_id", 0)
							.eq("type", ServerTypeEnum.C.name()));
		}
	}

	@Override
	public Result updatePassword(Long id, String oldPassword,
			String newPassword, Boolean syncUser) {
		SysUser sysUser = baseMapper.selectById(id);
		if (Objects.isNull(syncUser)) {
			syncUser = false;
		}
		if (StrUtil.isNotBlank(oldPassword)) {
			if (!passwordEncoder.matches(oldPassword, sysUser.getPassword())) {
				return Result.failed("旧密码错误");
			}
		}
		if (StrUtil.isBlank(newPassword)) {
			newPassword = CommonConstant.DEF_USER_PASSWORD;
		}
		updatePassword(sysUser, newPassword, syncUser);
		if (syncUser) {
			SdpsUserSyncInfo syncUserInfo = userSyncInfoMapper
					.selectOne(new QueryWrapper<SdpsUserSyncInfo>()
							.select("user_id",
									"group_concat(cluster_id) as clusterIds")
							.eq("user_id", id)
							.eq("operator", UserSyncOperatorEnum.ADD.getCode())
							.eq("sync_result", true).groupBy("user_id"));
			List<Integer> clusterIds = (List<Integer>) Convert.toCollection(
					ArrayList.class, Integer.class,
					syncUserInfo.getClusterIds());
			UserSyncRequest request = new UserSyncRequest();
			request.setOperation(UserSyncOperatorEnum.UPDATE.getCode());
			request.setSyncTypes(UserSyncEnum.getAllCode());
			request.setClusterIds(clusterIds);
			request.setUserId(id);
			Result result = userSyncCenterFegin.userSynOperator(request);
			if (result.isFailed()) {
				throw new BusinessException("调用异步用户同步服务失败");
			}
		}
		return Result.succeed("修改成功");
	}

	private String getEncryptPassword(String pass) {
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "publicKey"));
		return RsaUtil.encrypt(pass, sysGlobalArgs.getArgValue());
	}

	private String getDecryptPassword(String pass) {
		if (StrUtil.isBlank(pass))
			return null;
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		return RsaUtil.decrypt(pass, sysGlobalArgs.getArgValue());
	}

	@Override
	public SdpsServerInfo selectServerUserInfo(String username, String type,
			String argType) {
		SdpsServerInfo sdpsServerInfo = rangerInfoMapper
				.selectOne(new QueryWrapper<SdpsServerInfo>()
						.eq("user", username).eq("server_id", 0)
						.eq("type", type));
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		sdpsServerInfo.setPasswd(RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
				sysGlobalArgs.getArgValue()));
		return sdpsServerInfo;
	}

	private static String appendRedisKey(String clusterId, String serverType,
			String username) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(login_cert_redis_key).append(clusterId)
				.append(redis_join).append(serverType).append(redis_join)
				.append(username);
		String redis_key = stringBuffer.toString();
		return redis_key.toString();
	}

	@Override
	public Map<String, String> serverLogin(String clusterId, String type,
			String username, Boolean isCache) {
		if (isCache) {
			String redis_key = appendRedisKey(clusterId, type, username);
			Object object = redisRepository.get(redis_key);
			if (Objects.nonNull(object) && object instanceof Map) {
				Map<String, String> cert = (Map<String, String>) object;
				return cert;
			}
		}
		SdpsServerInfo sdpsServerInfo = rangerInfoMapper.selectServerInfo(
				Integer.valueOf(clusterId), type);
		sdpsServerInfo
				.setPasswd(getDecryptPassword(sdpsServerInfo.getPasswd()));
		if (serverLoginMap.containsKey(ServerTypeEnum.getCodeByName(type))) {
			return serverLoginMap.get(ServerTypeEnum.getCodeByName(type))
					.login(sdpsServerInfo, username);
		} else {
			throw new BusinessException("该类型的登录未实现");
		}

	}

	@Override
	public SysUser findUserByEmail(String email) {
		SysUser sysUser = this.getOne(new QueryWrapper<SysUser>().eq("email",
				email));
		return sysUser;
	}

	@Override
	public UrlUserVo getUrlUsers(Long urlId, SysUser user) {
		QueryWrapper<SysUser> sysUserQuery = new QueryWrapper<>();
		UrlUserVo res = new UrlUserVo();
		List<SysUser> sysUsers = userMapper.selectList(sysUserQuery);
		Set<String> usersByUrlId = bigdataCommonFegin.getUsersByUrlId(urlId);

		sysUsers.forEach(u -> {

			if (!StringUtils.isEmpty(u.getUsername())) {
				if (usersByUrlId.contains(u.getUsername())) {
					res.getCheckUsers().add(u);
				} else {
					res.getUnCheckUsers().add(u);
				}
			}

		});
		return res;
	}

	@Override
	public List<SdpsUserSyncInfo> getUserSyncInfo(Long userId) {
		List<SdpsUserSyncInfo> infos = userSyncInfoMapper
				.selectUserSyncInfo(userId);
		return infos;
	}

	@Override
	public SysUser selectByUserId(String userId) {
		List<SysUser> users = baseMapper.selectList(new QueryWrapper<SysUser>()
				.eq("id", userId));
		return getUser(users);
	}

	@Override
	public JSONObject getRolesByUserId(Long userId) {
		List<UserDto> userDtos = this.baseMapper.selectRolesByUserId(userId);
		if (CollUtil.isEmpty(userDtos)) {
			return null;
		}
		UserDto userDto = userDtos.get(0);
		Map<String, List<UserDto>> dtoTypeMap = userDtos.stream().collect(
				Collectors.groupingBy(UserDto::getType));
		JSONObject result = new JSONObject();
		result.put("username", userDto.getUsername());
		result.put("id", userDto.getId());
		result.put("nickname", userDto.getNickname());
		dtoTypeMap.forEach((k, v) -> {
			JSONArray jsonArray = new JSONArray();
			if (StrUtil.equalsIgnoreCase(TypeEnum.S.getCode().toString(), k)) {
				v.forEach(value -> {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("roleId", value.getRoleId());
					jsonObject.put("roleName", value.getRoleName());
					jsonArray.add(jsonObject);
				});

			} else if (StrUtil.equalsIgnoreCase(
					TypeEnum.O.getCode().toString(), k)) {
				Map<Long, List<UserDto>> dtoIdMap = v.stream().collect(
						Collectors.groupingBy(UserDto::getItemId));
				dtoIdMap.forEach((x, y) -> {
					JSONObject itemInfo = new JSONObject();
					JSONArray array = new JSONArray();
					itemInfo.put("roles", array);
					itemInfo.put("itemId", x);
					jsonArray.add(itemInfo);
					y.forEach(obj -> {
						itemInfo.put("itemName", obj.getItemName());
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("roleId", obj.getRoleId());
						jsonObject.put("roleName", obj.getRoleName());
						array.add(jsonObject);
					});
				});
			} else if (StrUtil.equalsIgnoreCase(
					TypeEnum.T.getCode().toString(), k)) {
				Map<Long, List<UserDto>> dtoIdMap = v.stream().collect(
						Collectors.groupingBy(UserDto::getTenantId));
				dtoIdMap.forEach((x, y) -> {
					JSONObject tenantInfo = new JSONObject();
					JSONArray array = new JSONArray();
					tenantInfo.put("roles", array);
					tenantInfo.put("tenantId", x);
					jsonArray.add(tenantInfo);
					y.forEach(obj -> {
						tenantInfo.put("tenantName", obj.getTenantName());
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("roleId", obj.getRoleId());
						jsonObject.put("roleName", obj.getRoleName());
						array.add(jsonObject);
					});
				});
			}
			result.put(k, jsonArray);
		});
		return result;
	}

	@Override
	public void insertTenantUsers(UserVo userVo) {
		userRoleItemService.remove(new QueryWrapper<UserRoleItem>()
				.eq("tenant_id", userVo.getTenantId())
				.in("user_id", userVo.getUserIds())
				.in("role_id", userVo.getRoleIds()));
		List<UserRoleItem> userRoleItems = CollUtil.newArrayList();
		userVo.getUserIds().forEach(id -> {
			userVo.getRoleIds().forEach(obj -> {
				UserRoleItem userRoleItem = new UserRoleItem();
				userRoleItem.setUserId(id);
				userRoleItem.setRoleId(obj);
				userRoleItem.setTenantId(userVo.getTenantId());
				userRoleItems.add(userRoleItem);
			});
		});
		userRoleItemService.insertBatch(userRoleItems);
	}
}