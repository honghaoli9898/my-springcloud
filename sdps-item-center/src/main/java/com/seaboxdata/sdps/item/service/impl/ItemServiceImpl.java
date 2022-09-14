package com.seaboxdata.sdps.item.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.accesses.RangerHdfsAccesses;
import com.seaboxdata.sdps.item.anotation.DataPermission;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.feign.BigDataCommonProxyFeign;
import com.seaboxdata.sdps.item.mapper.DatasourceItemMapper;
import com.seaboxdata.sdps.item.mapper.RoleGroupItemMapper;
import com.seaboxdata.sdps.item.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatabaseMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsItemMapper;
import com.seaboxdata.sdps.item.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.item.mapper.SdpsTableMapper;
import com.seaboxdata.sdps.item.mapper.SdpsTenantMapper;
import com.seaboxdata.sdps.item.mapper.SdpsTenantResourceMapper;
import com.seaboxdata.sdps.item.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.item.mapper.SysRoleMapper;
import com.seaboxdata.sdps.item.mapper.SysUserMapper;
import com.seaboxdata.sdps.item.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.item.model.DatasourceItem;
import com.seaboxdata.sdps.item.model.RoleGroupItem;
import com.seaboxdata.sdps.item.model.UserRoleItem;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.item.DataHistogramRequest;
import com.seaboxdata.sdps.item.vo.item.ItemReourceRequest;
import com.seaboxdata.sdps.item.vo.item.ItemRequest;

@Service
public class ItemServiceImpl extends SuperServiceImpl<SdpsItemMapper, SdpsItem>
		implements IItemService {
	@Autowired
	private RoleGroupItemMapper roleGroupItemMapper;
	@Autowired
	private UserRoleItemMapper userRoleItemMapper;
	@Autowired
	private SysRoleMapper roleMapper;
	@Autowired
	private BigDataCommonProxyFeign bigDataCommonProxyFeign;
	@Autowired
	private SdpsClusterMapper clusterMapper;
	@Autowired
	private DatasourceItemMapper datasourceItemMapper;
	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private SdpsDatasourceMapper datasourceMapper;
	@Autowired
	private SdpsDatabaseMapper databaseMapper;
	@Autowired
	private SdpsTableMapper tableMapper;
	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;
	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;
	@Autowired
	private SdpsTenantResourceMapper tenantResourceMapper;

	@Autowired
	private SdpsTenantMapper tenantMapper;

	@Override
	@Transactional
	public boolean insertResources(ItemReourceRequest resources) {
		return saveResources(resources);
	}

	private boolean saveResources(ItemReourceRequest resources) {
		tenantResourceMapper.insert(resources);
		HdfsSetDirObj hdfsSetDirObj = new HdfsSetDirObj();
		hdfsSetDirObj.setClusterId(resources.getAssClusterId().intValue());
		hdfsSetDirObj.setHdfsPath(resources.getHdfsDir());
		hdfsSetDirObj.setQuotaNum(Long.valueOf(resources.getFileNumQuota()));
		hdfsSetDirObj.setSpaceQuotaNum(Long.valueOf(resources.getSpaceQuota()));
		Result result = bigDataCommonProxyFeign
				.createHdfsQNAndSQNAndOwner(hdfsSetDirObj);
		if (Objects.isNull(result) || !Objects.equals(0, result.getCode())) {
			throw new BusinessException("调用大数据代理服务失败");
		}
		String iden = StrUtil.replace(hdfsSetDirObj.getHdfsPath(), "/project/",
				"").replaceAll("/", "");
		result = bigDataCommonProxyFeign
				.createItemResource(iden, hdfsSetDirObj);
		if (result.isFailed()) {
			throw new BusinessException("添加ranger组失败");
		}
		RangerPolicyObj rangerPolicyObj = new RangerPolicyObj();
		rangerPolicyObj.setAccesses(CollUtil.newArrayList(
				RangerHdfsAccesses.WRITE.getAccessesName(),
				RangerHdfsAccesses.READ.getAccessesName(),
				RangerHdfsAccesses.EXECUTE.getAccessesName()));
		rangerPolicyObj.setClusterId(resources.getAssClusterId().intValue());
		rangerPolicyObj.setGroups(CollUtil.newArrayList(iden.concat("-ALL")));
		rangerPolicyObj.setUsers(CollUtil.newArrayList("admin"));
		rangerPolicyObj.setResourcePaths(CollUtil.newArrayList(resources
				.getHdfsDir().endsWith("/") ? resources.getHdfsDir().substring(
				0, resources.getHdfsDir().length() - 1) : resources
				.getHdfsDir()));
		rangerPolicyObj.setServiceType("hdfs");
		rangerPolicyObj.setPolicyName(iden.concat("-ALL"));
		result = bigDataCommonProxyFeign.addRangerPolicy(rangerPolicyObj);
		if (result.isFailed()) {
			throw new BusinessException("添加ranger策略失败");
		}
		Set<String> usernames = this.baseMapper
				.selectAllMembersByItemId(resources.getItemId()).stream()
				.map(ItemDto::getName).collect(Collectors.toSet());
		if (CollUtil.isNotEmpty(usernames)) {
			result = bigDataCommonProxyFeign.addUsersToGroup(resources
					.getAssClusterId().intValue(), iden.concat("-ALL"),
					CollUtil.newArrayList(usernames));
			if (result.isFailed()) {
				throw new BusinessException("向ranger用户组添加用户失败");
			}
		}
		result = bigDataCommonProxyFeign.insertYarnQueueConfigurate(resources
				.getAssClusterId().intValue(), resources.getInfos());
		if (result.isFailed()) {
			throw new BusinessException("创建yarn资源失败");
		}
		return true;
	}

	@Override
	@Transactional
	public Long insertItem(SdpsItem item) {
		return saveItem(item);
	}

	private Long saveItem(SdpsItem item) {
		save(item);
		Long itemId = item.getId();
		SysRole role = roleMapper.selectOne(new QueryWrapper<SysRole>().select(
				"id").eq("code", "projectManager"));
		List<UserRoleItem> userRoleItems = item.getIds().stream().map(id -> {
			UserRoleItem userRoleItem = new UserRoleItem();
			userRoleItem.setItemId(itemId);
			userRoleItem.setRoleId(role.getId());
			userRoleItem.setUserId(id);
			userRoleItem.setTenantId(item.getTenantId());
			return userRoleItem;
		}).collect(Collectors.toList());
		userRoleItemMapper.insertBatchSomeColumn(userRoleItems);
		return itemId;
	}

	@Override
	@Transactional
	public void insertItemAndResource(SdpsItem item,
			ItemReourceRequest resources) {
		Long itemId = saveItem(item);
		resources.setItemId(itemId);
		tenantResourceMapper.insert(resources);
		saveResources(resources);
	}

	@Override
	@Transactional
	public void updateResourcesById(SdpsTenantResource resources) {
		SdpsTenantResource resultResources = tenantResourceMapper
				.selectById(resources.getId());
		tenantResourceMapper.updateById(resources);

		HdfsSetDirObj hdfsSetDirObj = new HdfsSetDirObj();
		hdfsSetDirObj
				.setClusterId(resultResources.getAssClusterId().intValue());
		hdfsSetDirObj.setHdfsPath(resultResources.getHdfsDir());
		hdfsSetDirObj.setQuotaNum(resources.getFileNumQuota());
		hdfsSetDirObj.setSpaceQuotaNum(resources.getSpaceQuota());
		Result result = bigDataCommonProxyFeign
				.updataHdfsQNAndSQNAndOwner(hdfsSetDirObj);
		if (Objects.isNull(result) || !Objects.equals(0, result.getCode())) {
			throw new BusinessException("调用集群代理失败");
		}
	}

	@Override
	@DataPermission(joinName = "item.id")
	public PageResult<ItemDto> findItems(Integer page, Integer size,
			ItemRequest param) {
		PageHelper.startPage(page, size);
		Page<ItemDto> itemResults = this.baseMapper.findItemsByExample(param);
		if (CollUtil.isNotEmpty(itemResults)) {
			List<Long> itemIds = itemResults.stream().map(ItemDto::getId)
					.collect(Collectors.toList());
			List<UserRoleItem> userRoleItems = userRoleItemMapper
					.selectItemUserCnt(itemIds, false);
			SysRole role = roleMapper.selectOne(new QueryWrapper<SysRole>()
					.select("id").eq("code", "projectManager"));
			List<ItemDto> managerResults = this.baseMapper
					.findItemManagerByItemIds(itemIds, role.getId());
			Map<Long, List<UserRoleItem>> members = userRoleItems.stream()
					.collect(Collectors.groupingBy(UserRoleItem::getItemId));
			Map<Long, List<ItemDto>> managers = managerResults.stream()
					.collect(Collectors.groupingBy(ItemDto::getId));
			itemResults.getResult().forEach(
					result -> {
						List<UserRoleItem> memberList = members.get(result
								.getId());
						result.setMembers(CollUtil.isEmpty(memberList) ? 0
								: memberList.get(0).getUserId().intValue());
						List<ItemDto> itemDtos = managers.get(result.getId());
						result.setDatas(CollUtil.isEmpty(itemDtos) ? CollUtil
								.newArrayList() : itemDtos);
					});
		}
		return PageResult.<ItemDto> builder().code(0)
				.data(itemResults.getResult()).count(itemResults.getTotal())
				.msg("操作成功").build();
	}

	@Override
	@Transactional
	public void deleteItems(ItemRequest request) {
		List<SdpsTenantResource> resources = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>().in(
						"item_id", request.getIds()));
		if (CollUtil.isNotEmpty(resources)) {
			Map<Long, List<SdpsTenantResource>> clusterResourcesMap = resources
					.stream()
					.collect(
							Collectors
									.groupingBy(SdpsTenantResource::getAssClusterId));
			if (clusterResourcesMap.size() > 1) {
				throw new BusinessException("只能删除相同集群下的项目");
			}
		}
		List<SdpsItem> items = this.listByIds(request.getIds());
		removeByIds(request.getIds());
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().in(
				"item_id", request.getIds()));
		roleGroupItemMapper.delete(new QueryWrapper<RoleGroupItem>().in(
				"item_id", request.getIds()));
		datasourceItemMapper.delete(new QueryWrapper<DatasourceItem>().in(
				"item_id", request.getIds()));
		if (Objects.isNull(request.getIsDel()) ? false : request.getIsDel()) {
			if (CollUtil.isNotEmpty(resources)) {
				Result result = null;
				List<String> hdfsPathList = resources.stream()
						.map(SdpsTenantResource::getHdfsDir)
						.collect(Collectors.toList());
				result = bigDataCommonProxyFeign.deleteItemFile(resources
						.get(0).getAssClusterId().intValue(), hdfsPathList);
				if (result.isFailed()) {
					JSONObject jsonObject = new JSONObject(
							(Map) result.getData());
					String message = jsonObject.getString("message");
					if (StrUtil.isBlank(message)) {
						throw new BusinessException("删除项目目录失败");
					}
					if (!message.contains("not exist")) {
						throw new BusinessException("删除项目目录失败");
					}
				}
				Integer clusterId = resources.get(0).getAssClusterId()
						.intValue();
				for (SdpsItem item : items) {
					result = bigDataCommonProxyFeign.deleteRangerGroupByName(
							clusterId, item.getIden().concat("-ALL"));
					if (result.isFailed()) {
						throw new BusinessException("删除ranger用户组失败");
					}
					result = bigDataCommonProxyFeign.deleteRangerPolicy(
							clusterId, "hdfs", item.getIden().concat("-ALL"));
					if (result.isFailed()) {
						if (!result.getMsg().contains("结果为null或空")) {
							throw new BusinessException("删除ranger策略失败");
						}
					}

				}
				result = bigDataCommonProxyFeign.deleteYarnQueueConfigurate(
						clusterId, request.getInfos());
				if (result.isFailed()) {
					throw new BusinessException("删除yarn队列失败");
				}
				List<Long> resourcesIds = resources.stream()
						.map(SdpsTenantResource::getId)
						.collect(Collectors.toList());
				tenantResourceMapper.deleteBatchIds(resourcesIds);
			}

		}

	}

	@Override
	public PageResult<ItemDto> findItemMembers(PageRequest<ItemRequest> request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<ItemDto> userResults = this.baseMapper.findItemMembers(request
				.getParam());
		if (CollUtil.isNotEmpty(userResults.getResult())) {
			List<Long> userIds = userResults.stream().map(ItemDto::getUserId)
					.collect(Collectors.toList());
			List<ItemDto> itemUserRoles = roleMapper.selectRolesByIds(userIds,
					request.getParam().getId());
			Map<Long, List<ItemDto>> userRoleMap = itemUserRoles.stream()
					.collect(Collectors.groupingBy(ItemDto::getUserId));
			userResults
					.getResult()
					.forEach(
							user -> {
								List<ItemDto> roles = userRoleMap.get(user
										.getUserId());
								user.setDatas(CollUtil.isEmpty(roles) ? CollUtil
										.newArrayList() : roles);
							});
		}
		return PageResult.<ItemDto> builder().code(0)
				.data(userResults.getResult()).msg("操作成功")
				.count(userResults.getTotal()).build();
	}

	@Override
	public PageResult<ItemDto> findGroups(PageRequest<ItemRequest> request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<ItemDto> groupResults = this.baseMapper.findItemGroups(request
				.getParam());
		if (CollUtil.isNotEmpty(groupResults.getResult())) {
			List<Long> groupIds = groupResults.stream().map(ItemDto::getId)
					.collect(Collectors.toList());
			List<ItemDto> itemUserRoles = roleGroupItemMapper.selectRolesByIds(
					groupIds, request.getParam().getId());
			Map<Long, List<ItemDto>> groupRoleMap = itemUserRoles.stream()
					.collect(Collectors.groupingBy(ItemDto::getGroupId));
			groupResults.getResult().forEach(
					group -> {
						List<ItemDto> roles = groupRoleMap.get(group.getId());
						group.setDatas(CollUtil.isEmpty(roles) ? CollUtil
								.newArrayList() : roles);
					});
		}
		return PageResult.<ItemDto> builder().code(0)
				.data(groupResults.getResult()).msg("操作成功")
				.count(groupResults.getTotal()).build();
	}

	@Override
	@Transactional
	public void updateItem(SdpsItem item) {
		SdpsItem sdpsItem = this.getById(item.getId());
		this.updateById(item);
		if (CollUtil.isNotEmpty(item.getIds())) {
			SysRole role = roleMapper.selectOne(new QueryWrapper<SysRole>()
					.select("id").eq("code", "projectManager"));
			userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
					"item_id", item.getId()).eq("role_id", role.getId()));
			List<UserRoleItem> userRoleItems = item.getIds().stream()
					.map(id -> {
						UserRoleItem userRoleItem = new UserRoleItem();
						userRoleItem.setItemId(item.getId());
						userRoleItem.setRoleId(role.getId());
						userRoleItem.setUserId(id);
						userRoleItem.setTenantId(sdpsItem.getTenantId());
						return userRoleItem;
					}).collect(Collectors.toList());
			userRoleItemMapper.insertBatchSomeColumn(userRoleItems);
		}
	}

	@Override
	public List<SdpsTenantResource> findItemResourcesById(ItemRequest request) {
		List<SdpsTenantResource> resources = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>().eq(
						"item_id", request.getId()));
		Long tenantId = this.getById(request.getId()).getTenantId();

		if (CollUtil.isNotEmpty(resources)) {
			resources.forEach(res -> {
				res.setTenantId(tenantId);
				Result<HdfsDirObj> result = bigDataCommonProxyFeign
						.selectHdfsQNAndSQN(res.getAssClusterId().intValue(),
								res.getHdfsDir());
				if (Objects.isNull(result)
						|| !Objects.equals(0, result.getCode())) {
					throw new BusinessException("调用集群代理服务失败");
				}
				HdfsDirObj hdfsDirObj = (HdfsDirObj) result.getData();
				res.setSpaceQuota(hdfsDirObj.getSpaceQuotaNum());
				res.setFileNumQuota(hdfsDirObj.getQuotaNum());
				res.setUsedSpace(hdfsDirObj.getSpaceConsumed());
			});

		}

		return resources;

	}

	@Override
	@Transactional
	public void insertMemberByItemId(ItemRequest request) {
		insertBatchMemberToItem(request);
	}

	private void insertBatchMemberToItem(ItemRequest request) {

		SdpsTenantResource resource = tenantResourceMapper
				.selectOne(new QueryWrapper<SdpsTenantResource>().select(
						"ass_cluster_id").eq("item_id", request.getId()));
		if (Objects.nonNull(resource)) {
			String rangerGroupName = this.getById(request.getId()).getIden()
					.concat("-ALL");
			List<String> usernames = userMapper
					.selectList(
							new QueryWrapper<SysUser>().select("username").in(
									"id", request.getIds())).stream()
					.map(SysUser::getUsername).collect(Collectors.toList());
			Result result = bigDataCommonProxyFeign.addUsersToGroup(resource
					.getAssClusterId().intValue(), rangerGroupName, usernames);
			if (result.isFailed()) {
				throw new BusinessException("将用户添加到ranger用户组失败");
			}
		}

		SdpsItem item = this.getById(request.getId());
		List<UserRoleItem> userRoleItems = request.getIds().stream()
				.map(userId -> {
					return request.getRoleIds().stream().map(roleId -> {
						UserRoleItem userRoleItem = new UserRoleItem();
						userRoleItem.setItemId(request.getId());
						userRoleItem.setRoleId(roleId);
						userRoleItem.setTenantId(item.getTenantId());
						userRoleItem.setUserId(userId);
						return userRoleItem;
					}).collect(Collectors.toList());

				}).reduce((a, b) -> {
					a.addAll(b);
					return a;
				}).get();
		userRoleItemMapper.insertBatchSomeColumn(userRoleItems);

	}

	@Override
	@Transactional
	public void insertGroupByItemId(ItemRequest request) {
		insertBatchGroupToItem(request);
	}

	private void insertBatchGroupToItem(ItemRequest request) {
		Set<String> usernames = userMapper
				.findExistMemberByGroup(request.getIds()).stream()
				.map(SysUser::getUsername).collect(Collectors.toSet());
		if (CollUtil.isNotEmpty(usernames)) {
			String rangerGroupName = this.getById(request.getId()).getIden()
					.concat("-ALL");
			Integer clusterId = tenantResourceMapper
					.selectOne(
							new QueryWrapper<SdpsTenantResource>().select(
									"ass_cluster_id").eq("item_id",
									request.getId())).getAssClusterId()
					.intValue();
			Result result = bigDataCommonProxyFeign.addUsersToGroup(clusterId,
					rangerGroupName, CollUtil.newArrayList(usernames));
			if (result.isFailed()) {
				throw new BusinessException("将用户添加到ranger用户组失败");
			}
		}
		List<RoleGroupItem> userRoleItems = request.getIds().stream()
				.map(groupId -> {
					return request.getRoleIds().stream().map(roleId -> {
						RoleGroupItem roleGroupItem = new RoleGroupItem();
						roleGroupItem.setItemId(request.getId());
						roleGroupItem.setRoleId(roleId);
						roleGroupItem.setGroupId(groupId);
						return roleGroupItem;
					}).collect(Collectors.toList());

				}).reduce((a, b) -> {
					a.addAll(b);
					return a;
				}).get();
		roleGroupItemMapper.insertBatchSomeColumn(userRoleItems);
	}

	@Override
	@Transactional
	public void deleteGroupByItemId(ItemRequest request) {
		Set<String> usernames = userMapper
				.findExistMemberByGroup(request.getIds()).stream()
				.map(SysUser::getUsername).collect(Collectors.toSet());
		String rangerGroupName = this.getById(request.getId()).getIden()
				.concat("-ALL");
		Integer clusterId = tenantResourceMapper
				.selectOne(
						new QueryWrapper<SdpsTenantResource>().select(
								"ass_cluster_id")
								.eq("item_id", request.getId()))
				.getAssClusterId().intValue();
		Result result = bigDataCommonProxyFeign.deleteUsersToGroup(clusterId,
				rangerGroupName, CollUtil.newArrayList(usernames));
		if (result.isFailed()) {
			throw new BusinessException("从ranger用户组删除用户失败");
		}
		roleGroupItemMapper.delete(new QueryWrapper<RoleGroupItem>().eq(
				"item_id", request.getId()).in("group_id", request.getIds()));
	}

	@Override
	@Transactional
	public void deleteMembersByItemId(ItemRequest request) {
		String rangerGroupName = this.getById(request.getId()).getIden()
				.concat("-ALL");
		Integer clusterId = tenantResourceMapper
				.selectOne(
						new QueryWrapper<SdpsTenantResource>().select(
								"ass_cluster_id")
								.eq("item_id", request.getId()))
				.getAssClusterId().intValue();
		List<String> usernames = userMapper
				.selectList(
						new QueryWrapper<SysUser>().select("username").in("id",
								request.getIds())).stream()
				.map(SysUser::getUsername).collect(Collectors.toList());
		Result result = bigDataCommonProxyFeign.deleteUsersToGroup(clusterId,
				rangerGroupName, usernames);
		if (result.isFailed()) {
			throw new BusinessException("从ranger用户组删除用户失败");
		}
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
				"item_id", request.getId()).in("user_id", request.getIds()));
	}

	@Override
	@Transactional
	public void updateMembersByItemId(ItemRequest request) {
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
				"item_id", request.getId()).in("user_id", request.getIds()));
		insertBatchMemberToItem(request);

	}

	@Override
	@Transactional
	public void updateGroupByItemId(ItemRequest request) {
		roleGroupItemMapper.delete(new QueryWrapper<RoleGroupItem>().eq(
				"item_id", request.getId()).in("group_id", request.getIds()));
		insertBatchGroupToItem(request);
	}

	@Override
	public List<ItemDto> selectAllMembersByItemId(ItemRequest request) {
		return this.baseMapper.selectAllMembersByItemId(request.getId());
	}

	@Override
	public List<ItemDto> selectAllGroupByItemId(ItemRequest request) {
		return this.baseMapper.selectAllGroupByItemId(request.getId());
	}

	@Override
	@DataPermission(joinName = "item.id")
	public List<ItemDto> selectItemByExample(ItemRequest request) {
		return this.baseMapper.findItemsByExample(request);
	}

	@Override
	public List<ItemDto> selectItemByUser(Long userId, boolean isManager) {
		Long roleId = null;
		if (isManager) {
			SysRole role = roleMapper.selectOne(new QueryWrapper<SysRole>()
					.select("id").eq("code", "projectManager"));
			if (Objects.isNull(role) || Objects.isNull(role.getId())) {
				throw new BusinessException("系统缺少项目管理员角色");
			}
			roleId = role.getId();
		}
		return this.baseMapper.selectItemByUser(userId, roleId);
	}

	@Override
	public List<ItemDto> selectNotExisitMemberByItemId(Long itemId) {
		return this.baseMapper.selectNotExisitMemberByItemId(itemId);
	}

	@Override
	public List<ItemDto> selectNotExisitGroupByItemId(Long itemId) {
		return this.baseMapper.selectNotExisitGroupByItemId(itemId);
	}

	@Override
	public List<SdpsCluster> selectClusterByItemId(Long id) {
		List<SdpsTenantResource> resources = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>().eq(
						"item_id", id));
		if (CollUtil.isEmpty(resources)) {
			throw new BusinessException("此项目未关联集群");
		}
		return clusterMapper.selectList(new QueryWrapper<SdpsCluster>().in(
				"cluster_id",
				resources.stream().map(SdpsTenantResource::getAssClusterId)
						.collect(Collectors.toList())));
	}

	@Override
	public List<ItemDto> selectCurrentUserHasItemInfo(SysUser sysUser) {
		ItemRequest request = new ItemRequest();
		request.setEnabled(1);
		if (!isAdmin(sysUser)) {
			request.setId(sysUser.getId());
		}
		List<ItemDto> itemDtos = this.baseMapper
				.selectCurrentUserHasItemInfo(request);
		if (CollUtil.isEmpty(itemDtos)) {
			return null;
		}
		List<Long> itemIds = itemDtos.stream().map(ItemDto::getId)
				.collect(Collectors.toList());
		List<UserRoleItem> users = userRoleItemMapper.selectItemUserCnt(
				itemIds, false);
		Map<Long, Integer> userCntMap = MapUtil.newHashMap();
		users.forEach(map -> {
			userCntMap.put(Long.valueOf(map.getItemId()), map.getUserId()
					.intValue());
		});

		Map<String, List<ItemDto>> map = itemDtos.stream().collect(
				Collectors.groupingBy(ItemDto::getName));
		List<ItemDto> result = CollUtil.newArrayList();
		map.forEach((k, v) -> {
			ItemDto dto = v.get(0);
			dto.setIds(CollUtil.newArrayList());
			v.forEach(obj -> {
				dto.getIds().add(obj.getClusterId());
			});
			dto.setClusterId(null);
			dto.setUserCnt(userCntMap.get(dto.getId()));
			result.add(dto);
		});
		return result;
	}

	private static boolean isAdmin(SysUser sysUser) {
		List<String> roleList = sysUser.getRoles().stream()
				.map(SysRole::getCode).collect(Collectors.toList());
		if (roleList.contains("admin") || roleList.contains("sysOperation")) {
			return true;
		}
		return false;
	}

	@Override
	public List<ItemDto> findItemsByUser(ItemRequest request) {
		if (!Objects.equals(request.getUsername(), "admin")) {
			List<ItemDto> itemDtos = selectItemByUser(
					Long.valueOf(request.getUserId()), false);
			Set<Long> itemIds = itemDtos.stream().map(ItemDto::getId)
					.collect(Collectors.toSet());
			if (CollUtil.isNotEmpty(itemIds)) {

				StringBuffer sb = new StringBuffer();
				sb.append("item.id").append(" in(");
				itemIds.forEach(id -> {
					sb.append("'").append(id).append("',");
				});
				if (CollUtil.isNotEmpty(itemIds)) {
					sb.deleteCharAt(sb.length() - 1);
				} else {
					sb.append("''");
				}
				sb.append(")");
				request.setDataPermissionSql(sb.toString());
			}
		}
		return this.baseMapper.findItemsByExample(request);
	}

	@Override
	public JSONObject selectItemRankInfo() {
		List<UserRoleItem> users = userRoleItemMapper.selectItemUserCnt(null,
				true);
		Map<Long, Integer> userCntMap = MapUtil.newHashMap();
		users.forEach(map -> {
			userCntMap.put(Long.valueOf(map.getItemId()), map.getUserId()
					.intValue());
		});
		List<Long> itemIds = users.stream().map(UserRoleItem::getItemId)
				.collect(Collectors.toList());
		List<SdpsItem> items = this.list(new QueryWrapper<SdpsItem>().select(
				"id", "name").in("id", itemIds));
		List<ItemDto> itemDto = CollUtil.newArrayList();
		items.forEach(item -> {
			ItemDto dto = new ItemDto();
			dto.setName(item.getName());
			dto.setId(item.getId());
			dto.setUserCnt(userCntMap.get(dto.getId()));
			itemDto.add(dto);
		});
		itemDto.sort(Comparator.comparing(ItemDto::getUserCnt));
		JSONObject result = new JSONObject();
		result.put("itemUserRank", itemDto);
		List<SdpsTenantResource> resources = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>()
						.select("ass_cluster_id", "count(1) as cnt")
						.groupBy("ass_cluster_id").orderByDesc("cnt")
						.last("limit 10"));
		List<Long> clusterIds = resources.stream()
				.map(SdpsTenantResource::getAssClusterId)
				.collect(Collectors.toList());
		List<SdpsCluster> clusters = clusterMapper
				.selectList(new QueryWrapper<SdpsCluster>().select(
						"cluster_id", "cluster_name", "cluster_show_name").in(
						"cluster_id", clusterIds));
		Map<Integer, List<SdpsCluster>> map = clusters.stream().collect(
				Collectors.groupingBy(SdpsCluster::getClusterId));
		resources.forEach(data -> {
			data.setAssClusterName(map.get(data.getAssClusterId()).get(0)
					.getClusterShowName());
		});
		result.put("itemClusterRank", resources);
		return result;
	}

	@Override
	public JSONObject selectDataRankInfo() {
		JSONObject result = new JSONObject();
		Long datasourceCnt = datasourceMapper.selectCount(null);
		Long databaseCnt = databaseMapper.selectCount(null);
		Long tableCnt = tableMapper.selectCount(null);
		result.put("datasourceCnt", datasourceCnt);
		result.put("databaseCnt", databaseCnt);
		result.put("tableCnt", tableCnt);
		result.put("hdfsFileCnt", 0L);
		List<SdpsCluster> clusters = clusterMapper
				.selectList(new QueryWrapper<SdpsCluster>()
						.select("cluster_id").eq("is_use", true)
						.eq("is_running", true));
		clusters.forEach(cluster -> {
			try {
				Result<List<FileStatsDTO>> data = bigDataCommonProxyFeign
						.getStatsByType(cluster.getClusterId(), "temp");
				if (data.isSuccess()) {
					data.getData().forEach(
							obj -> {
								result.put(
										"hdfsFileCnt",
										Long.valueOf(result.get("hdfsFileCnt")
												.toString())
												+ obj.getTypeValueNum());
							});
				}
			} catch (Exception e) {
				log.error("调用大数据代理失败", e);
			}
		});
		return result;
	}

	private String getDecryptPassword(String pass) {
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		return RsaUtil.decrypt(pass, sysGlobalArgs.getArgValue());
	}

	@Override
	public JSONObject getHistogram(DataHistogramRequest request)
			throws Exception {
		JSONObject result = getHdfsFileRank();
		result.put("histogram", getHistogramByRequest(request));
		return result;
	}

	private JSONObject getHdfsFileRank() throws Exception {
		JSONObject result = new JSONObject();
		List<SdpsCluster> clusters = clusterMapper
				.selectList(new QueryWrapper<SdpsCluster>().select("server_id")
						.eq("is_use", true).eq("is_running", true));
		if (CollUtil.isNotEmpty(clusters)) {
			long hdfsFileCnt = 0L;
			List<SdpsServerInfo> sdpsServerInfos = serverInfoMapper
					.selectList(new QueryWrapper<SdpsServerInfo>().in(
							"server_id",
							clusters.stream().map(SdpsCluster::getServerId)
									.collect(Collectors.toList())).eq("type",
							"spark"));
			Map<String, Integer> map = MapUtil.newHashMap();
			for (SdpsServerInfo sdpsServerInfo : sdpsServerInfos) {
				sdpsServerInfo.setPasswd(getDecryptPassword(sdpsServerInfo
						.getPasswd()));
				RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
						sdpsServerInfo.getHost(), sdpsServerInfo.getUser(),
						sdpsServerInfo.getPasswd(),
						Integer.valueOf(sdpsServerInfo.getPort()));
				remoteShellExecutorUtil
						.commonExec(" hdfs dfs -ls -R /project | grep '^-' | grep '\\-ALL' | awk '{print $4}'| uniq -c | awk '{print $1\"|\"$2}'");
				String shellResult = remoteShellExecutorUtil
						.getOutStrStringBuffer().toString();
				List<String> strList = StrUtil.split(shellResult.trim(), '\n');

				strList.forEach(str -> {
					List<String> list = StrUtil.split(str, '|');
					map.put(list.get(1).replace("-ALL", ""),
							Integer.valueOf(list.get(0)));
				});

			}
			List<Map.Entry<String, Integer>> infos = CollUtil.newArrayList(map
					.entrySet());

			// 根据value排序
			Collections.sort(infos,
					new Comparator<Map.Entry<String, Integer>>() {
						public int compare(Map.Entry<String, Integer> o1,
								Map.Entry<String, Integer> o2) {
							return (o2.getValue() - o1.getValue());
						}
					});
			JSONArray jsonArray = new JSONArray();
			if (infos.size() > 10) {
				CollUtil.sub(infos, 0, 10);
			}
			for (int i = 0; i < infos.size(); i++) {
				Entry<String, Integer> entry = infos.get(i);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("name", entry.getKey());
				jsonObject.put("value", entry.getValue());
				hdfsFileCnt += entry.getValue();
				jsonArray.add(jsonObject);
			}

			result.put("hdfsFileCnt", hdfsFileCnt);
			result.put("hdfsFileRank", infos);
		}
		return result;
	}

	private JSONArray getItemAssDataRank(DataHistogramRequest request) {
		String type = request.getType();
		switch (type) {
		case "datasource":

			break;

		default:
			break;
		}
		return null;
	}

	private JSONArray getHistogramByRequest(DataHistogramRequest request) {
		String dateType = request.getDateType();
		DateTime currentTime = DateUtil.date();
		DateTime current = DateUtil.parseDate(currentTime
				.toString(CommonConstant.DATE_FORMAT));
		DateTime nextDay = DateUtil.offsetDay(current, 1);
		DateTime lastDay = DateUtil.offsetDay(current, -1);
		List<DateTime> dateTimeList;
		request.setEndTime(nextDay);
		String parseDateTime;
		if ("year".equals(dateType)) {
			DateTime lastYear = DateUtil.offsetMonth(current,
					-current.getMonth());
			lastYear.setDate(1);
			request.setStartTime(lastYear);
			dateTimeList = DateUtil.rangeToList(lastYear, current,
					DateField.MONTH);
			parseDateTime = "yyyy/MM";
		} else if ("month".equals(dateType)) {
			DateTime currentMonth = DateUtil.offsetDay(nextDay,
					-current.getField(DateField.DAY_OF_MONTH));
			request.setStartTime(currentMonth);
			dateTimeList = DateUtil.rangeToList(currentMonth, lastDay,
					DateField.DAY_OF_MONTH);
			parseDateTime = "MM/dd";
		} else if ("week".equals(dateType)) {
			int dayOfWeek = current.dayOfWeek();
			if (dayOfWeek == 1) {
				dayOfWeek = 8;
			}
			DateTime currentWeek = DateUtil.offsetDay(current, -dayOfWeek + 2);
			request.setStartTime(currentWeek);
			dateTimeList = DateUtil.rangeToList(currentWeek, lastDay,
					DateField.DAY_OF_MONTH);
			parseDateTime = "MM/dd";
		} else if ("hour".equals(dateType)) {
			request.setStartTime(current);
			request.setEndTime(currentTime);
			dateTimeList = DateUtil.rangeToList(current, currentTime,
					DateField.HOUR);
			parseDateTime = "HH:mm";
		} else {
			request.setStartTime(DateUtil.parseDate(DateUtil.format(
					request.getStartTime(), CommonConstant.DATE_FORMAT)));
			request.setEndTime(DateUtil.offsetDay(
					DateUtil.parseDate(DateUtil.format(request.getStartTime(),
							CommonConstant.DATE_FORMAT)), 1));
			dateTimeList = DateUtil.rangeToList(request.getStartTime(),
					request.getEndTime(), DateField.HOUR);
			parseDateTime = "yyyy/MM/dd";
		}

		List<DataSourceDto> results = datasourceMapper.selectHistogram(request);
		JSONArray jsonArray = new JSONArray();
		for (DateTime dateTime : dateTimeList) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", dateTime.toString(parseDateTime));
			jsonObject.put("value", "0");
			for (DataSourceDto dataSourceDto : results) {
				if (dateTime.toString().contains(dataSourceDto.getName())) {
					jsonObject.put("value", dataSourceDto.getValue());
					break;
				}
			}
			jsonArray.add(jsonObject);

		}
		return jsonArray;

	}

	@Transactional
	@Override
	public void updateItemYarnResources(ItemReourceRequest resources) {

		tenantResourceMapper.updateById(resources);
		if (CollUtil.isNotEmpty(resources.getInfos())) {
			SdpsTenantResource resultResources = tenantResourceMapper
					.selectById(resources.getId());
			Result result = bigDataCommonProxyFeign.updateYarnQueueConfigurate(
					resultResources.getAssClusterId().intValue(),
					resources.getInfos());
			if (Objects.isNull(result) || !Objects.equals(0, result.getCode())) {
				throw new BusinessException("调用集群代理失败");
			}
		}

	}
}
