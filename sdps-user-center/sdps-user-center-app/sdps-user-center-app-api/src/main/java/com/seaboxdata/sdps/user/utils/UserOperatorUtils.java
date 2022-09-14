package com.seaboxdata.sdps.user.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.vo.user.ImportUserCsvVo;

public class UserOperatorUtils {

	public static Map<String, List<ImportUserCsvVo>> checkImportUser(
			List<ImportUserCsvVo> rows, IUserService userService) {
		Map<String, List<ImportUserCsvVo>> resultMap = MapUtil.newHashMap();
		List<ImportUserCsvVo> errorUsers = rows
				.stream()
				.filter(row -> {
					if (StrUtil.isNotBlank(row.getUsername())
							&& StrUtil.isNotBlank(row.getNickname())
							&& StrUtil.isNotBlank(row.getEmail())) {

						return false;
					}
					return true;
				}).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(errorUsers)) {
			resultMap.put("用户名或者用户密码或者登录名不完整", errorUsers);
		}
		errorUsers = rows
				.stream()
				.filter(row -> {
					if (StrUtil.isNotBlank(row.getUsername())
							&& StrUtil.isNotBlank(row.getNickname())
							&& StrUtil.isNotBlank(row.getEmail())) {
						if (userService.count(new QueryWrapper<SysUser>().eq(
								"username", row.getUsername())) > 0) {
							return true;
						}
						if (userService.count(new QueryWrapper<SysUser>().eq(
								"email", row.getEmail())) > 0) {
							return true;
						}
						return false;
					}
					return false;
				}).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(errorUsers)) {
			resultMap.put("登录名已存在", errorUsers);
		}

		return resultMap;
	}
}
