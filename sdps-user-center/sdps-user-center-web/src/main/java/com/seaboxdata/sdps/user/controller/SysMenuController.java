package com.seaboxdata.sdps.user.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;

import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.context.TenantContextHolder;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.ISysMenuService;

@RestController
@Slf4j
@RequestMapping("/menus")
public class SysMenuController {
	@Autowired
	private ISysMenuService menuService;

	/**
	 * 两层循环实现建树
	 *
	 * @param sysMenus
	 * @return
	 */
	public static List<SysMenu> treeBuilder(List<SysMenu> sysMenus) {
		List<SysMenu> menus = new ArrayList<>();
		for (SysMenu sysMenu : sysMenus) {
			if (Objects.equals(-1L, sysMenu.getParentId())) {
				menus.add(sysMenu);
			}
			for (SysMenu menu : sysMenus) {
				if (menu.getParentId().equals(sysMenu.getId())) {
					if (sysMenu.getSubMenus() == null) {
						sysMenu.setSubMenus(new ArrayList<>());
					}
					sysMenu.getSubMenus().add(menu);
				}
			}
		}
		return menus;
	}

	/**
	 * 删除菜单
	 *
	 * @param id
	 */
	@DeleteMapping("/{id}")
	public Result delete(@PathVariable Long id) {
		try {
			menuService.removeById(id);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("memu-delete-error", ex);
			return Result.failed("操作失败");
		}
	}

	@GetMapping("/{roleId}/menus")
	public List<Map<String, Object>> findMenusByRoleId(@PathVariable Long roleId) {
		Set<Long> roleIds = new HashSet<>();
		roleIds.add(roleId);
		// 获取该角色对应的菜单
		List<SysMenu> roleMenus = menuService.findByRoles(roleIds);
		// 全部的菜单列表
		List<SysMenu> allMenus = menuService.findAll();
		List<Map<String, Object>> authTrees = new ArrayList<>();

		Map<Long, SysMenu> roleMenusMap = roleMenus.stream().collect(
				Collectors.toMap(SysMenu::getId, SysMenu -> SysMenu));

		for (SysMenu sysMenu : allMenus) {
			Map<String, Object> authTree = new HashMap<>();
			authTree.put("id", sysMenu.getId());
			authTree.put("name", sysMenu.getName());
			authTree.put("title", sysMenu.getTitle());
			authTree.put("pId", sysMenu.getParentId());
			authTree.put("open", true);
			authTree.put("checked", false);
			if (roleMenusMap.get(sysMenu.getId()) != null) {
				authTree.put("checked", true);
			}
			authTrees.add(authTree);
		}
		return authTrees;
	}

	@SuppressWarnings("unchecked")
	@Cacheable(value = "menu", key = "#roleCodes")
	@GetMapping("/{roleCodes}")
	public List<SysMenu> findMenuByRoles(@PathVariable String roleCodes) {
		List<SysMenu> result = null;
		if (StringUtils.isNotEmpty(roleCodes)) {
			Set<String> roleSet = (Set<String>) Convert.toCollection(
					HashSet.class, String.class, roleCodes);
			result = menuService.findByRoleCodes(roleSet,
					CommonConstant.PERMISSION);
		}
		return result;
	}

	/**
	 * 给角色分配菜单
	 */
	@PostMapping("/granted")
	public Result setMenuToRole(@RequestBody SysMenu sysMenu) {
		menuService.setMenuToRole(sysMenu.getRoleId(), sysMenu.getMenuIds());
		return Result.succeed("操作成功");
	}

	@GetMapping("/findAlls")
	public PageResult<SysMenu> findAlls() {
		List<SysMenu> list = menuService.findAll();
		return PageResult.<SysMenu> builder().data(list).code(0)
				.count((long) list.size()).build();
	}

	@GetMapping("/findOnes")
	public PageResult<SysMenu> findOnes() {
		List<SysMenu> list = menuService.findOnes();
		return PageResult.<SysMenu> builder().data(list).code(0)
				.count((long) list.size()).build();
	}

	/**
	 * 添加菜单 或者 更新
	 *
	 * @param menu
	 * @return
	 */
	@PostMapping("saveOrUpdate")
	public Result saveOrUpdate(@RequestBody SysMenu menu) {
		try {
			String tenant = TenantContextHolder.getTenant();
			menu.setTenantId(tenant);
			menuService.saveOrUpdate(menu);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("memu-saveOrUpdate-error", ex);
			return Result.failed("操作失败");
		}
	}

	/**
	 * 当前登录用户的菜单
	 *
	 * @return
	 */
	@GetMapping("/current")
	public Result<List<SysMenu>> findMyMenu(@LoginUser SysUser user) {
		List<SysRole> roles = user.getRoles();
		if (CollectionUtil.isEmpty(roles)) {
			return Result.succeed(Collections.emptyList(), "操作成功");
		}
		List<SysMenu> menus = menuService.findByRoleCodes(
				roles.parallelStream().map(SysRole::getCode)
						.collect(Collectors.toSet()), CommonConstant.MENU);
		return Result.succeed(treeBuilder(menus), "操作成功");
	}
}
