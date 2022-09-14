package com.seaboxdata.sdps.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SdpsTenant;
import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.api.ITenantService;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.user.mybatis.dto.tenant.TenantDto;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsItemMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SdpsTenantResourceMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.SysRoleMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.user.vo.tenant.TenantResourceVo;

@RestController
@RequestMapping("/tenant")
public class TenantManagerController {

	@Autowired
	private BigdataCommonFegin bigdataCommonFegin;

	@Autowired
	private ITenantService tenantService;

	@Autowired
	private SysRoleMapper roleMapper;

	@Autowired
	private SdpsItemMapper itemMapper;

	@Autowired
	private UserRoleItemMapper userRoleItemMapper;

	@Autowired
	private SdpsTenantResourceMapper tenantResourceMapper;

	/**
	 * 两层循环实现建树
	 *
	 * @param sysMenus
	 * @return
	 */

	public static List<SdpsTenant> treeBuilder(List<SdpsTenant> sdpsTenants,
			Long tenantId) {
		List<SdpsTenant> tenants = new ArrayList<>();
		for (SdpsTenant sdpsTenant : sdpsTenants) {
			if (Objects.equals(tenantId, sdpsTenant.getId())) {
				tenants.add(sdpsTenant);
			}
			for (SdpsTenant tenant : sdpsTenants) {
				if (tenant.getParentId().equals(sdpsTenant.getId())) {
					if (sdpsTenant.getSubTenants() == null) {
						sdpsTenant.setSubTenants(new ArrayList<>());
					}
					sdpsTenant.getSubTenants().add(tenant);
				}
			}
		}
		return tenants;
	}

	@GetMapping("/get")
	public Result findTenantsInfo(
			@RequestParam(value = "tenantId") Long currTenantId) {
		List<TenantDto> dtos = tenantService.selectTenants(currTenantId);
		return Result.succeed(dtos, "操作成功");
	}

	@GetMapping("/get/{id}")
	public Result findTenantById(@PathVariable("id") Long tenantId) {
		TenantDto tenantDto = tenantService.selectTenantById(tenantId);
		return Result.succeed(tenantDto, "操作成功");
	}

	@GetMapping("/list")
	public Result findTenants(
			@RequestParam(value = "tenantId") Long currTenantId) {
		List<SdpsTenant> sdpsTenants = tenantService
				.list(new QueryWrapper<SdpsTenant>()
						.select("id", "parent_id", "`name`", "`iden`")
						.apply("id = {0} or find_in_set({0} ,ancestors)",
								currTenantId).orderByAsc("create_time"));
		return Result.succeed(treeBuilder(sdpsTenants, currTenantId), "操作成功");
	}

	@PostMapping("/create")
	public Result insertTenant(@LoginUser(isFull = true) SysUser sysUser,
			@RequestBody SdpsTenant sdpsTenant) {
		Result result = checkTenant(sysUser, sdpsTenant);
		if (result.isFailed()) {
			return result;
		}
		SdpsTenant tenant = tenantService.insertTenant(sdpsTenant);
		return Result.succeed(tenant, "操作成功");
	}

	@PostMapping("/members")
	public PageResult<SysUser> selectMembersByTenantId(
			@RequestBody PageRequest<SdpsTenant> request) {
		return tenantService.selectMembersByTenantId(request);
	}

	private Result checkTenant(SysUser sysUser, SdpsTenant sdpsTenant) {
		long count = tenantService.count(new QueryWrapper<SdpsTenant>().eq(
				"name", sdpsTenant.getName()));
		if (count > 0) {
			return Result.failed("已经存在同名租户");
		}
		count = tenantService.count(new QueryWrapper<SdpsTenant>().eq("iden",
				sdpsTenant.getIden()));
		if (count > 0) {
			return Result.failed("已经存在同标识租户");
		}
		SysRole sysRole = roleMapper.selectOne(new QueryWrapper<SysRole>().eq(
				"code", "tenantManager"));
		sdpsTenant.setLeaderRoleId(sysRole.getId());
		// List<SysRole> roles = sysUser.getRoles();
		// if (roles.contains("admin")) {
		// return Result.succeed("有权限");
		// }
		// List<SdpsTenant> tenants = tenantService
		// .list(new QueryWrapper<SdpsTenant>().eq("id",
		// sdpsTenant.getParentId()).last(
		// "or find_in_set('".concat(
		// sdpsTenant.getParentId().toString()).concat(
		// "' ,ancestors)")));
		// List<Long> tenantIds = tenants.stream().map(SdpsTenant::getId)
		// .collect(Collectors.toList());
		// count = userRoleItemMapper.selectCount(new
		// QueryWrapper<UserRoleItem>()
		// .in("tenant_id", tenantIds).eq("user_id", sysUser.getId())
		// .eq("role_id", sysRole.getId()).last("limit 1"));
		// if (count == 0) {
		// return Result.failed("没有该租户的创建权限");
		// }

		return Result.succeed("检测正常");
	}

	@PostMapping("/resource/create")
	public Result createResource(@RequestBody TenantResourceVo tenantResource) {
		Result result = checkResource(tenantResource);
		if (result.isFailed()) {
			return result;
		}
		tenantService.createResource(tenantResource);
		return Result.succeed("操作成功");
	}

	private Result checkResource(TenantResourceVo tenantResource) {
		if (StrUtil.isBlank(tenantResource.getResourceName())
				|| Objects.isNull(tenantResource.getTenantId())
				|| Objects.isNull(tenantResource.getAssClusterId())
				|| Objects.isNull(tenantResource.getWeight())
				|| CollUtil.isEmpty(tenantResource.getInfos())) {
			return Result.failed("缺少必填参数");
		}

		long cnt = tenantResourceMapper
				.selectCount(new QueryWrapper<SdpsTenantResource>().eq(
						"resource_name", tenantResource.getResourceName()));
		if (cnt > 0) {
			return Result.failed("资源标识已存在");
		}
		SdpsTenant tenant = tenantService.getById(tenantResource.getTenantId());
		SdpsTenantResource resource = tenantResourceMapper
				.selectOne(new QueryWrapper<SdpsTenantResource>().eq(
						"tenant_id", tenant.getParentId()));
		if (tenantResource.getMaxCore() > resource.getMaxCore()
				|| tenantResource.getMaxMemory() > resource.getMaxMemory()) {
			return Result.failed("最大内存或最大核数有误");
		}
		return Result.succeed("检查通过");
	}

	@GetMapping("/resource/{id}/HDFS")
	public Result getHdfsByTenantId(@PathVariable("id") Long tenantId) {
		TenantDto tenantDto = tenantService.getHdfsByTenantId(tenantId);
		return Result.succeed(tenantDto, "操作成功");
	}

	@GetMapping("/resource/{id}/YARN")
	public Result getYarnByTenantId(@PathVariable("id") Long tenantId) {
		TenantDto tenantDto = tenantService.getYarnByTenantId(tenantId);
		JSONObject result = new JSONObject();
		if (Objects.nonNull(tenantDto)) {
			Result queues = bigdataCommonFegin
					.getYarnQueueConfigurate(tenantDto.getAssClusterId()
							.intValue());
			if (queues.isFailed()) {
				return queues;
			}
			result.put("queueInfo", queues.getData());
		}
		result.put("resourceInfo", tenantDto);
		return Result.succeed(result, "操作成功");
	}

	@PostMapping("/update")
	public Result updateTenant(@RequestBody SdpsTenant sdpsTenant) {
		if (Objects.isNull(sdpsTenant.getId())
				|| StrUtil.isBlank(sdpsTenant.getName())) {
			return Result.failed("传入参数有误");
		}
		if (StrUtil.isNotBlank(sdpsTenant.getName())) {
			long cnt = tenantService.count(new QueryWrapper<SdpsTenant>().ne(
					"id", sdpsTenant.getId()).eq("name", sdpsTenant.getName()));
			if (cnt > 0) {
				return Result.failed("修改的名称已存在");
			}
		}

		sdpsTenant = BeanUtil.copyProperties(sdpsTenant, SdpsTenant.class,
				"creater", "parentId", "iden", "ancestors", "level",
				"createTime");
		tenantService.updateById(sdpsTenant);
		return Result.succeed("操作成功");
	}

	@GetMapping("/resource/get/{id}")
	public Result getHasResourceById(@PathVariable("id") Long tenantId) {
		SdpsTenant sdpsTenant = tenantService.getById(tenantId);
		if (Objects.isNull(sdpsTenant)) {
			return Result.failed("此租户不存在");
		}
		sdpsTenant = tenantService.getOne(new QueryWrapper<SdpsTenant>().eq(
				"id", sdpsTenant.getParentId()));
		List<SdpsTenantResource> resource = tenantResourceMapper
				.selectList(new QueryWrapper<SdpsTenantResource>().select(
						"distinct ass_cluster_id,ass_cluster_name").eq(
						"tenant_id", sdpsTenant.getId()));
		return Result.succeed(resource, "操作成功");
	}

	@DeleteMapping("/delete")
	public Result deleteTenant(@RequestBody TenantResourceVo resourceVo) {
		if (Objects.isNull(resourceVo.getTenantId())) {
			return Result.succeed("租户id不能为空");
		}
		SdpsTenant tenant = tenantService.getById(resourceVo.getTenantId());
		if (Objects.equals(0L, tenant.getParentId())) {
			return Result.failed("顶级租户不能被删除");
		}
		long cnt = itemMapper.selectCount(new QueryWrapper<SdpsItem>().eq(
				"tenant_id", resourceVo.getTenantId()));

		cnt = cnt
				+ tenantService.count(new QueryWrapper<SdpsTenant>().eq(
						"parent_id", tenant.getId()));
		if (cnt > 0) {
			return Result.failed("该租户下存在".concat(String.valueOf(cnt)).concat(
					"个有效的子项目和子租户"));
		}

		tenantService.deleteTenantById(resourceVo.getTenantId(), resourceVo);
		return Result.succeed("操作成功");
	}

	@PostMapping("/resource/updateYarn")
	public Result updateYarnResource(
			@RequestBody TenantResourceVo tenantResourceVo) {
		if (Objects.isNull(tenantResourceVo.getId())) {
			return Result.failed("资源ID不能为空");
		}
		tenantService.updateYarnResource(tenantResourceVo);
		return Result.succeed("操作成功");
	}

	@PostMapping("/resource/updateHdfs")
	public Result updateHdfsResource(
			@RequestBody TenantResourceVo tenantResourceVo) {
		if (Objects.isNull(tenantResourceVo.getId())) {
			return Result.failed("资源ID不能为空");
		}
		tenantService.updateHdfsResource(tenantResourceVo);
		return Result.succeed("操作成功");
	}

	@GetMapping("/all")
	public Result findAllTenants() {
		List<SdpsTenant> result = tenantService
				.list(new QueryWrapper<SdpsTenant>().select("id", "name"));
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/users/{id}/nohas")
	public Result findNoHasTenantsByUserId(@PathVariable("id") Long userId) {
		List<SdpsTenant> result = tenantService
				.findNoHasTenantsByUserId(userId);
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/users/{id}")
	public Result findHasTenantsByUserId(@PathVariable("id") Long userId) {
		List<SdpsTenant> result = tenantService.findHasTenantsByUserId(userId);
		return Result.succeed(result, "操作成功");
	}

	@DeleteMapping("/users/remove")
	public Result removeTenantUsers(
			@RequestBody TenantResourceVo tenantResourceVo) {
		tenantService.removeTenantUsers(tenantResourceVo);
		return Result.succeed("操作成功");
	}
}
