package com.seaboxdata.sdps.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsTenant;
import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.api.ITenantService;
import com.seaboxdata.sdps.user.enums.TypeEnum;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.user.mybatis.dto.tenant.TenantDto;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsItemMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsTenantMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsTenantResourceMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysRoleMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysUserMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserRoleItem;
import com.seaboxdata.sdps.user.vo.tenant.TenantResourceVo;

@Service
public class TenantServiceImpl extends
		SuperServiceImpl<SdpsTenantMapper, SdpsTenant> implements
		ITenantService {

	@Autowired
	private SysRoleMapper roleMapper;

	@Autowired
	private BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	private SysUserMapper userMapper;

	@Autowired
	private UserRoleItemMapper userRoleItemMapper;

	@Autowired
	private SdpsTenantResourceMapper tenantResourceMapper;

	@Autowired
	private SdpsItemMapper itemMapper;

	private Map<String, TenantDto> sumTenantsQuota(List<TenantDto> tenantDtos) {
		Map<String, List<TenantDto>> resourceMap = tenantDtos.stream().collect(
				Collectors.groupingBy(TenantDto::getAncestors));
		Map<String, TenantDto> tenantDtoMap = MapUtil.newHashMap();
		resourceMap.forEach((k, v) -> {
			v.clear();
		});
		Set<String> keys = resourceMap.keySet();
		for (TenantDto tenantDto : tenantDtos) {
			keys.stream().filter(str -> {
				return tenantDto.getAncestors().contains(str);
			}).forEach(key -> {
				resourceMap.get(key).add(tenantDto);
			});
		}
		resourceMap.forEach((k, v) -> {
			List<String> list = StrUtil.split(k, ',');
			String key = list.get(list.size() - 1);
			TenantDto tenantDto = new TenantDto();
			v.stream()
					.map(obj -> {
						String itemIdsStr = obj.getItemIds();
						String userIdsStr = obj.getUserIds();
						if (StrUtil.isNotBlank(itemIdsStr)) {
							List<String> itemIdsList = StrUtil.split(
									itemIdsStr, ",");
							tenantDto.getItemIdSet().addAll(itemIdsList);
						}
						if (StrUtil.isNotBlank(userIdsStr)) {
							List<String> userIdsList = StrUtil.split(
									userIdsStr, ",");
							tenantDto.getUserIdSet().addAll(userIdsList);
						}
						return obj;
					})
					.filter(obj -> StrUtil.equalsIgnoreCase(obj.getAncestors(),
							k))
					.forEach(
							obj -> {
								tenantDto.setUsedSpace(Long.sum(
										tenantDto.getUsedSpace(),
										obj.getSpaceQuota()));
							});
			tenantDtoMap.put(key, tenantDto);
		});
		return tenantDtoMap;
	}

	@Override
	public List<TenantDto> selectTenants(Long currTenantId) {
		List<TenantDto> tenantDtos = this.baseMapper
				.selectTenants(currTenantId);
		Map<String, TenantDto> tenantDtoMap = sumTenantsQuota(tenantDtos);
		return treeBuilder(tenantDtos, tenantDtoMap, true, currTenantId);
	}

	@Transactional
	@Override
	public SdpsTenant insertTenant(SdpsTenant sdpsTenant) {
		SdpsTenant tenant = getOne(new QueryWrapper<SdpsTenant>().eq("id",
				sdpsTenant.getParentId()));
		List<SdpsTenantResource> tenantResources = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>().select(
						"distinct ass_cluster_id,ass_cluster_name").eq(
						"tenant_id", tenant.getId()));
		String ancestors = tenant.getAncestors().concat(",")
				.concat(sdpsTenant.getParentId().toString());
		int level = StrUtil.split(ancestors, ",").size();
		sdpsTenant.setLevel(level);
		sdpsTenant.setAncestors(ancestors);
		// sdpsTenant.setCreater(LoginUserContextHolder.getUser().getUsername());
		// sdpsTenant.setCreaterId(LoginUserContextHolder.getUser().getId());
		sdpsTenant.setCreateTime(DateUtil.date());
		save(sdpsTenant);
		List<UserRoleItem> roles = CollUtil.newArrayList();
		sdpsTenant.getUserIdList().forEach(obj -> {
			UserRoleItem userRoleItem = new UserRoleItem();
			userRoleItem.setRoleId(sdpsTenant.getLeaderRoleId());
			userRoleItem.setTenantId(sdpsTenant.getId());
			userRoleItem.setUserId(obj);
			roles.add(userRoleItem);
		});
		userRoleItemMapper.insertBatchSomeColumn(roles);
		tenant = new SdpsTenant();
		tenant.setIden(sdpsTenant.getIden());
		tenant.setName(sdpsTenant.getName());
		tenant.setId(sdpsTenant.getId());
		tenant.setTenantResources(tenantResources);
		return tenant;
	}

	public static List<TenantDto> treeBuilder(List<TenantDto> tenantDtos,
			Map<String, TenantDto> tenantDtoMap, boolean isCalc, Long tenantId) {
		List<TenantDto> dtos = new ArrayList<>();
		for (TenantDto tenantDto : tenantDtos) {
			if(StrUtil.equalsIgnoreCase("项目", tenantDto.getType())){
				continue;
			}
			if (Objects.equals(tenantId, tenantDto.getId())) {
				dtos.add(tenantDto);
			}
			if (isCalc) {

				TenantDto sumDto = tenantDtoMap.get(tenantDto.getId()
						.toString());
				String userIdsStr = tenantDto.getUserIds();
				String itemIdsStr = tenantDto.getItemIds();
				List<String> userIdList = CollUtil.newArrayList();
				List<String> itemIdList = CollUtil.newArrayList();
				if (StrUtil.isNotBlank(userIdsStr)) {
					userIdList = StrUtil.split(userIdsStr, ",");
				}
				if (StrUtil.isNotBlank(itemIdsStr)) {
					itemIdList = StrUtil.split(itemIdsStr, ",");
				}
				long space = tenantDto.getItemSpaceSum();
				if (Objects.nonNull(sumDto)) {
					sumDto.getUserIdSet().addAll(userIdList);
					sumDto.getItemIdSet().addAll(itemIdList);
					tenantDto.setUserCnt(sumDto.getUserIdSet().size());
					tenantDto.setItemCnt(sumDto.getItemIdSet().size());
					tenantDto.setUsedSpace(Long.sum(sumDto.getUsedSpace(),
							space));
				} else {
					tenantDto.setUserCnt(userIdList.size());
					tenantDto.setItemCnt(itemIdList.size());
					tenantDto.setUsedSpace(space);
				}
				tenantDto.setUserIds(null);
				tenantDto.setItemIds(null);
				tenantDto.setUserIdSet(null);
				tenantDto.setItemIdSet(null);
			}
			for (TenantDto tenant : tenantDtos) {
				if (tenant.getParentId().equals(tenantDto.getId())) {
					if (tenantDto.getSubDto() == null) {
						tenantDto.setSubDto(CollUtil.newArrayList());
					}
					tenantDto.getSubDto().add(tenant);
				}
			}
		}
		return dtos;
	}

	@Override
	public TenantDto selectTenantById(Long tenantId) {
		TenantDto tenantDto = new TenantDto();
		SdpsTenant sdpsTenant = this.getById(tenantId);
		BeanUtil.copyProperties(sdpsTenant, tenantDto, false);
		SdpsTenant parentTenant = this.getById(sdpsTenant.getParentId());
		SdpsTenantResource resource = tenantResourceMapper
				.selectOne(new QueryWrapper<SdpsTenantResource>().select(
						"ass_cluster_id", "ass_cluster_name").eq("tenant_id",
						tenantId));
		tenantDto.setParentTenant(parentTenant);
		tenantDto.setAssClusterId(resource.getAssClusterId());
		tenantDto.setAssClusterName(resource.getAssClusterName());
		List<UserRoleItem> userTenantRoles = userRoleItemMapper
				.selectList(new QueryWrapper<UserRoleItem>().eq("tenant_id",
						sdpsTenant.getId()));
		if (CollUtil.isNotEmpty(userTenantRoles)) {
			List<Long> userIds = userTenantRoles.stream()
					.map(UserRoleItem::getUserId).collect(Collectors.toList());
			List<SysUser> managers = userMapper
					.selectList(new QueryWrapper<SysUser>().select("id",
							"username", "nickname").in("id", userIds));
			Set<String> usernames = managers.stream().map(SysUser::getNickname)
					.collect(Collectors.toSet());
			tenantDto.setUsers(StrUtil.join(",", usernames));
		}
		return tenantDto;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PageResult<SysUser> selectMembersByTenantId(
			PageRequest<SdpsTenant> request) {
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SysUser> sysUsers = this.baseMapper
				.selectMembersByTenantId(request.getParam().getId());
		return (PageResult<SysUser>) PageResult.success(sysUsers.getTotal(),
				sysUsers);
	}

	@Override
	@Transactional
	public void createResource(TenantResourceVo tenantResource) {
		tenantResourceMapper.insert(tenantResource);
		Result result = bigdataCommonFegin.insertYarnQueueConfigurate(
				tenantResource.getAssClusterId().intValue(),
				tenantResource.getInfos());
		if (result.isFailed()) {
			throw new BusinessException("创建租户资源失败");
		}

	}

	@Override
	public TenantDto getHdfsByTenantId(Long tenantId) {
		SdpsTenant sdpsTenant = this.getById(tenantId);
		if (Objects.isNull(sdpsTenant)) {
			return null;
		}
		List<TenantDto> items = this.baseMapper
				.selectItemsByTenantId(sdpsTenant.getId());
		Integer level = sdpsTenant.getLevel() + 2;
		List<TenantDto> tenantDtos = this.baseMapper.selectHdfsByTenantId(
				tenantId, level);
		Map<String, List<TenantDto>> resourceMap = tenantDtos.stream().collect(
				Collectors.groupingBy(TenantDto::getAncestors));
		Map<String, Long> map = MapUtil.newHashMap();
		resourceMap.forEach((k, v) -> {
			Long spaceSum = v.stream().mapToLong(TenantDto::getSpaceQuota)
					.sum();
			List<String> keys = StrUtil.split(k, ',');
			map.put(keys.get(keys.size() - 1), spaceSum);
		});
		tenantDtos = tenantDtos.stream().filter(obj -> {
			return obj.getLevel() < level;
		}).map(obj -> {
			obj.setType("租户");
			obj.setResourceType("HDFS");
			Long spaceSum = map.get(obj.getId().toString());
			spaceSum = Objects.isNull(spaceSum) ? 0L : spaceSum;
			obj.setUsedSpace(Long.sum(spaceSum, obj.getItemSpaceSum()));
			return obj;
		}).collect(Collectors.toList());
		tenantDtos = treeBuilder(tenantDtos, null, false, tenantId);
		TenantDto tenantDto = tenantDtos.get(0);
		if (CollUtil.isEmpty(tenantDto.getSubDto())) {
			tenantDto.setSubDto(CollUtil.newArrayList());
		}
		tenantDto.getSubDto().addAll(items);
		return tenantDto;
	}

	@Override
	public TenantDto getYarnByTenantId(Long tenantId) {
		SdpsTenant sdpsTenant = this.getById(tenantId);
		if (Objects.isNull(sdpsTenant)) {
			return null;
		}
		List<TenantDto> tenantDtos = this.baseMapper
				.selectYarnByTenantId(sdpsTenant.getId());
		tenantDtos = treeBuilder(tenantDtos, null, false, tenantId);
		return tenantDtos.get(0);
	}

	@Override
	@Transactional
	public boolean deleteTenantById(Long tenantId, TenantResourceVo resourceVo) {
		SdpsTenantResource resource = tenantResourceMapper
				.selectOne(new QueryWrapper<SdpsTenantResource>().eq(
						"tenant_id", tenantId));
		if (CollUtil.isNotEmpty(resourceVo.getInfos())) {

			Result delQueues = bigdataCommonFegin.deleteYarnQueueConfigurate(
					resource.getAssClusterId().intValue(),
					resourceVo.getInfos());
			if (delQueues.isFailed()) {
				throw new BusinessException("刪除yarn队列失败");
			}
		}
		this.removeById(tenantId);
		tenantResourceMapper.delete(new QueryWrapper<SdpsTenantResource>().eq(
				"tenant_id", tenantId));
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
				"tenant_id", tenantId));
		return true;
	}

	@Override
	@Transactional
	public boolean updateYarnResource(TenantResourceVo tenantResourceVo) {
		SdpsTenantResource resource = tenantResourceMapper
				.selectById(tenantResourceVo.getId());

		Long clusterId = resource.getAssClusterId();
		bigdataCommonFegin.updateYarnQueueConfigurate(clusterId.intValue(),
				tenantResourceVo.getInfos());
		SdpsTenantResource tenantResource = BeanUtil.copyProperties(
				tenantResourceVo, SdpsTenantResource.class, "assClusterId",
				"assClusterName", "tenantId", "hdfsDir", "resourceFullName",
				"resourceName", "itemId");
		tenantResourceMapper.updateById(tenantResource);
		return true;
	}

	@Override
	public List<SdpsTenant> findNoHasTenantsByUserId(Long userId) {
		List<UserRoleItem> tenants = userRoleItemMapper
				.selectList(new QueryWrapper<UserRoleItem>()
						.select("tenant_id").eq("user_id", userId)
						.isNotNull("tenant_id"));
		if (CollUtil.isEmpty(tenants)) {
			return this.list(new QueryWrapper<SdpsTenant>()
					.select("id", "name"));
		}
		List<Long> ids = tenants.stream().map(UserRoleItem::getTenantId)
				.collect(Collectors.toList());
		return this.list(new QueryWrapper<SdpsTenant>().select("id", "name")
				.notIn("id", ids));
	}

	@Override
	public List<SdpsTenant> findHasTenantsByUserId(Long userId) {
		List<UserRoleItem> tenants = userRoleItemMapper
				.selectList(new QueryWrapper<UserRoleItem>()
						.select("tenant_id").eq("user_id", userId)
						.isNotNull("tenant_id"));
		if (CollUtil.isEmpty(tenants)) {
			return null;
		}
		List<Long> ids = tenants.stream().map(UserRoleItem::getTenantId)
				.collect(Collectors.toList());
		return this.list(new QueryWrapper<SdpsTenant>().select("id", "name")
				.in("id", ids));
	}

	@Override
	public boolean removeTenantUsers(TenantResourceVo tenantResourceVo) {
		userRoleItemMapper.delete(new QueryWrapper<UserRoleItem>().eq(
				"tenant_id", tenantResourceVo.getTenantId()).in("user_id",
				tenantResourceVo.getUserIds()));
		return true;
	}

	@Override
	public boolean updateHdfsResource(TenantResourceVo tenantResourceVo) {
		SdpsTenantResource tenantResource = BeanUtil.copyProperties(
				tenantResourceVo, SdpsTenantResource.class, "assClusterId",
				"assClusterName", "tenantId", "hdfsDir", "resourceFullName",
				"resourceName", "itemId");
		tenantResourceMapper.updateById(tenantResource);
		return true;
	}

}
