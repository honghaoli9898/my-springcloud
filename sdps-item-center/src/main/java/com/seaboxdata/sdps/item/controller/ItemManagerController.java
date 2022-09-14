package com.seaboxdata.sdps.item.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.mapper.SdpsItemMapper;
import com.seaboxdata.sdps.item.mapper.SdpsTenantResourceMapper;
import com.seaboxdata.sdps.item.mapper.SysRoleMapper;
import com.seaboxdata.sdps.item.model.UserRoleItem;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.service.IUserRoleItemService;
import com.seaboxdata.sdps.item.vo.item.CreateItemVo;
import com.seaboxdata.sdps.item.vo.item.ItemReourceRequest;
import com.seaboxdata.sdps.item.vo.item.ItemRequest;

@RestController
@RequestMapping("/MC10")
public class ItemManagerController {
	@Autowired
	private IItemService itemService;

	@Autowired
	private SdpsItemMapper sdpsItemMapper;

	@Autowired
	private IUserRoleItemService userRoleItemService;

	@Autowired
	private SysRoleMapper roleMapper;

	@Autowired
	private SdpsTenantResourceMapper tenantResourceMapper;

	/**
	 * 新建项目
	 * 
	 * @param item
	 * @return
	 */
	@PostMapping("/MC1001")
	public Result insertItem(@RequestBody SdpsItem item) {
		if (StrUtil.isBlank(item.getIden()) || StrUtil.isBlank(item.getName())
				|| CollUtil.isEmpty(item.getIds())
				|| Objects.isNull(item.getTenantId())) {
			return Result.failed("项目名称、项目标识、管理员、租户id为必填项");
		}
		if (itemService.count(new QueryWrapper<SdpsItem>().eq("iden",
				item.getIden())) > 0) {
			return Result.failed("已存在相同项目标识的项目");
		}

		Long id = itemService.insertItem(item);
		Map<String, Object> result = MapUtil.newHashMap();
		result.put("itemId", id);
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 新建项目于资源
	 * 
	 * @param resources
	 * @return
	 */
	@PostMapping("/MC1002")
	public Result insertItemResource(@LoginUser SysUser sysUser,
			@RequestBody ItemReourceRequest resources) {
		if (Objects.isNull(resources.getItemId())
				|| StrUtil.isBlank(resources.getResourceName())
				|| Objects.isNull(resources.getAssClusterId())
				|| StrUtil.isBlank(resources.getHdfsDir())
				|| CollUtil.isEmpty(resources.getInfos())
				|| Objects.isNull(resources.getWeight())) {
			return Result.failed("项目id、集群id、hdfs路径、资源信息为必填项");
		}
		if (!isHasItemAuthority(sysUser, resources.getItemId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.insertResources(resources);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC1003")
	public Result insertItemAndResource(@RequestBody CreateItemVo itemVo) {
		SdpsItem item = itemVo.getItemInfo();
		if (StrUtil.isBlank(item.getIden()) || StrUtil.isBlank(item.getName())
				|| CollUtil.isEmpty(item.getIds())) {
			return Result.failed("项目名称、项目标识、管理员为必填项");
		}
		if (itemService.count(new QueryWrapper<SdpsItem>().eq("iden",
				item.getIden())) > 0) {
			return Result.failed("已存在相同项目标识的项目");
		}
		ItemReourceRequest resources = itemVo.getResourcesInfo();
		if (Objects.isNull(resources.getAssClusterId())
				|| StrUtil.isBlank(resources.getHdfsDir())) {
			return Result.failed("集群id、hdfs路径为必填项");
		}
		itemService.insertItemAndResource(item, resources);
		return Result.succeed("操作成功");
	}

	/**
	 * 更新项目信息
	 * 
	 * @param item
	 * @return
	 */
	@PostMapping("/MC1004")
	public Result updateItemById(@LoginUser SysUser sysUser,
			@RequestBody SdpsItem item) {
		if (Objects.isNull(item.getId())) {
			return Result.failed("项目id不能为空");
		}
		if (StrUtil.isNotBlank(item.getIden())) {
			return Result.failed("项目标识不能被修改");
		}
		if (!isHasItemAuthority(sysUser, item.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.updateItem(item);
		return Result.succeed("操作成功");
	}

	/**
	 * 更新项目资源信息
	 * 
	 * @param resources
	 * @return
	 */
	@PostMapping("/MC1005")
	public Result updateItemHdfsResources(@LoginUser SysUser sysUser,
			@RequestBody SdpsTenantResource resources) {
		if (Objects.isNull(resources.getId())) {
			return Result.failed("资源id不能为空");
		}
		if (StrUtil.isNotBlank(resources.getResourceName())
				|| StrUtil.isNotBlank(resources.getHdfsDir())
				|| Objects.nonNull(resources.getAssClusterId())) {
			return Result.failed("hdfs目录、yarn队列、集群不能被修改");
		}
		Long itemId = tenantResourceMapper.selectById(resources.getId())
				.getItemId();
		if (!isHasItemAuthority(sysUser, itemId)) {
			return Result.failed("您没有该项目权限");
		}
		itemService.updateResourcesById(resources);
		return Result.succeed("操作成功");
	}

	/**
	 * 分页查询项目
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1006")
	public PageResult<ItemDto> findItems(
			@Validated @RequestBody PageRequest<ItemRequest> request) {
		if (Objects.nonNull(request.getParam())
				&& Objects.nonNull(request.getParam().getEndTime())) {
			request.getParam().setEndTime(
					DateUtil.offsetDay(request.getParam().getEndTime(), 1));
		}
		if (Objects.isNull(request.getParam())) {
			request.setParam(new ItemRequest());
		}
		PageResult<ItemDto> results = itemService.findItems(request.getPage(),
				request.getSize(), request.getParam());
		return results;
	}

	@GetMapping("/MC1025")
	public Result<List<ItemDto>> findItemsByUser(
			@RequestParam("username") String username,
			@RequestParam("userId") Long userId,
			@RequestParam("clusterId") Long clusterId) {
		ItemRequest request = new ItemRequest();
		request.setClusterId(clusterId);
		request.setUsername(username);
		request.setUserId(userId);
		List<ItemDto> results = itemService.findItemsByUser(request);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 查询项目成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1007")
	public PageResult<ItemDto> findItemMember(
			@Validated @RequestBody PageRequest<ItemRequest> request) {
		PageResult<ItemDto> results = itemService.findItemMembers(request);
		return results;
	}

	/**
	 * 查询项目成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1008")
	public PageResult<ItemDto> findGroups(
			@Validated @RequestBody PageRequest<ItemRequest> request) {
		PageResult<ItemDto> results = itemService.findGroups(request);
		return results;
	}

	/**
	 * 删除项目
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1009")
	public Result deleteItems(@LoginUser SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (CollUtil.isEmpty(request.getIds())) {
			return Result.succeed("操作成功");
		}
		for (Long id : request.getIds()) {
			if (!isHasItemAuthority(sysUser, id)) {
				return Result.failed("您没有该项目权限");
			}
		}
		itemService.deleteItems(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 根据项目id查询项目资源
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1010")
	public Result findItemResourcesById(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<SdpsTenantResource> results = itemService
				.findItemResourcesById(request);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 增加项目成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1011")
	public Result insertMemberByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())
				|| CollUtil.isEmpty(request.getRoleIds())) {
			return Result.failed("项目id、项目成员、成员角色不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.insertMemberByItemId(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 增加项目成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1012")
	public Result insertGroupByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())
				|| CollUtil.isEmpty(request.getRoleIds())) {
			return Result.failed("项目id、项目组、组角色不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.insertGroupByItemId(request);
		return Result.succeed("操作成功");
	}

	private boolean isHasItemAuthority(SysUser sysUser, Long itemId) {
		if (CommonConstant.ADMIN_USER_NAME.equals(sysUser.getUsername())) {
			return true;
		}
		boolean isHasItemAuthority = false;
		Long roleId = roleMapper.selectOne(
				new QueryWrapper<SysRole>().eq("code", "projectManager"))
				.getId();
		long count = userRoleItemService.count(new QueryWrapper<UserRoleItem>()
				.eq("item_id", itemId).eq("role_id", roleId)
				.eq("user_id", sysUser.getId()));
		if (count != 0L) {
			isHasItemAuthority = true;
		}
		return isHasItemAuthority;
	}

	/**
	 * 删除项目成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1013")
	public Result deleteGroupByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())) {
			return Result.failed("项目id、项目组不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.deleteGroupByItemId(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 删除项目成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1014")
	public Result deleteMembersByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())) {
			return Result.failed("项目id、项目人员不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.deleteMembersByItemId(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 更新项目成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1015")
	public Result updateMembersByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())
				|| CollUtil.isEmpty(request.getRoleIds())) {
			return Result.failed("项目id、项目人员、成员角色不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.updateMembersByItemId(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 更新项目成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1016")
	public Result updateGroupByItemId(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())
				|| CollUtil.isEmpty(request.getIds())
				|| CollUtil.isEmpty(request.getRoleIds())) {
			return Result.failed("项目id、项目组、组角色不能为空");
		}
		if (!isHasItemAuthority(sysUser, request.getId())) {
			return Result.failed("您没有该项目权限");
		}
		itemService.updateGroupByItemId(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 查询项目所有成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1017")
	public Result selectAllMembersByItemId(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<ItemDto> results = itemService.selectAllMembersByItemId(request);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 查询项目所有成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1018")
	public Result selectAllGroupByItemId(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<ItemDto> results = itemService.selectAllGroupByItemId(request);
		return Result.succeed(results, "操作成功");
	}

	@PostMapping("/MC1019")
	public Result selectItemByExample(@RequestBody ItemRequest request) {
		List<ItemDto> result = itemService.selectItemByExample(request);
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/selectItemAllList")
	public String selectItemAllList() {
		List<ItemDto> result = sdpsItemMapper
				.findItemsByExample(new ItemRequest());
		return JSONObject.toJSONString(result);
	}

	/**
	 * 根据用户查询用户所拥有的项目
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1020")
	public Result selectItemByUser(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("用户id不能为空");
		}
		List<ItemDto> result = itemService.selectItemByUser(request.getId(),
				false);
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 查询当前用户拥有的项目
	 * 
	 * @param user
	 * @return
	 */
	@PostMapping("/MC1021")
	public Result selectItemByCurrUser(@LoginUser(isFull = false) SysUser user) {
		if (Objects.isNull(user.getId())) {
			return Result.failed("用户id不能为空");
		}
		List<ItemDto> result = itemService
				.selectItemByUser(user.getId(), false);
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 查询项目不存在的成员
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1022")
	public Result selectNotExisitMemberByItemId(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<ItemDto> result = itemService
				.selectNotExisitMemberByItemId(request.getId());
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 查询项目不存在的成员组
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1023")
	public Result selectNotExisitGroupByItemId(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<ItemDto> result = itemService.selectNotExisitGroupByItemId(request
				.getId());
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 根据项目id查询项目拥有的集群
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC1024")
	public Result selectClusterByItemId(@RequestBody ItemRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("项目id不能为空");
		}
		List<SdpsCluster> result = itemService.selectClusterByItemId(request
				.getId());
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 更新项目资源信息
	 * 
	 * @param resources
	 * @return
	 */
	@PostMapping("/MC1025")
	public Result updateItemYarnResources(@LoginUser SysUser sysUser,
			@RequestBody ItemReourceRequest resources) {
		if (Objects.isNull(resources.getId())) {
			return Result.failed("资源id不能为空");
		}
		if (StrUtil.isNotBlank(resources.getResourceName())
				|| StrUtil.isNotBlank(resources.getHdfsDir())
				|| Objects.nonNull(resources.getAssClusterId())) {
			return Result.failed("hdfs目录、yarn队列、集群不能被修改");
		}
		Long itemId = tenantResourceMapper.selectById(resources.getId())
				.getItemId();
		if (!isHasItemAuthority(sysUser, itemId)) {
			return Result.failed("您没有该项目权限");
		}
		itemService.updateItemYarnResources(resources);
		return Result.succeed("操作成功");
	}
}
