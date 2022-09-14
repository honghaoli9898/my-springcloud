package com.seaboxdata.sdps.user.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.UrlUserVo;
import com.seaboxdata.sdps.common.core.utils.CsvUtils;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.mybatis.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;
import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;
import com.seaboxdata.sdps.user.utils.UserOperatorUtils;
import com.seaboxdata.sdps.user.vo.user.CreateUserVo;
import com.seaboxdata.sdps.user.vo.user.ImportUserCsvVo;
import com.seaboxdata.sdps.user.vo.user.PageUserRequest;
import com.seaboxdata.sdps.user.vo.user.UserVo;

@Slf4j
@RestController
@RequestMapping("/UC30")
public class UserManagerController {
	@Autowired
	private IUserService userService;

	/**
	 * 查询人信息界面
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/UC3001")
	public PageResult<SysUser> findUsers(
			@Validated @RequestBody PageUserRequest request) {
		PageResult<SysUser> results = userService.findUsers(request);
		return results;
	}

	/**
	 * 创建用户
	 *
	 * @param createUserVo
	 * @return
	 */
	@PostMapping("/UC3002")
	public Result insertUser(@Validated @RequestBody CreateUserVo createUserVo) {
		if (userService.count(new QueryWrapper<SysUser>().eq("username",
				createUserVo.getUsername())) > 0) {
			return Result.failed("已存在相同用户名的用户");
		}
		if (userService.count(new QueryWrapper<SysUser>().eq("email",
				createUserVo.getEmail())) > 0) {
			return Result.failed("此邮箱已经注册");
		}
		if (CollUtil.isEmpty(createUserVo.getTenantIds())) {
			return Result.failed("租户不能为空");
		}
		userService.createUserAllInfo(createUserVo);
		return Result.succeed("操作成功");
	}

	/**
	 * 删除人的所有信息
	 *
	 * @param ids
	 * @return
	 */
	@PostMapping("/UC3003")
	public Result deleteUserByIds(@RequestBody List<Long> ids) {
		userService.deleteUserAllInfo(ids);
		return Result.succeed("操作成功");
	}

	/**
	 * 增加人的项目角色
	 *
	 * @param userVo
	 * @return
	 */
	@PostMapping("/UC3006")
	public Result insertUserItemRole(@RequestBody UserVo userVo) {
		try {
			userService.insertUserItemRole(userVo);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
			return Result.failed("操作失败");
		}
	}

	/**
	 * 修改人的项目角色
	 *
	 * @param userVo
	 * @return
	 */
	@PostMapping("/UC3007")
	public Result updateUserItemRole(@RequestBody UserVo userVo) {
		if (StrUtil.isBlank(userVo.getType())) {
			return Result.failed("修改类型不能为空");
		}
		userService.updateUserItemRole(userVo);
		return Result.succeed("操作成功");
	}

	/**
	 * 将用户从组中移除
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/UC3009")
	public Result removeUserFromGroups(@RequestBody UserRequest request) {
		userService.removeUserFromGroups(request);
		return Result.succeed("操作成功");
	}

	/**
	 * 添加用户到所属组
	 *
	 * @param userGroup
	 * @return
	 */
	@PostMapping("/UC3010")
	public Result addUserToGroup(@RequestBody List<UserGroup> userGroup) {
		userService.addUserToGroup(userGroup);
		return Result.succeed("操作成功");
	}

	/**
	 * 查询用户列表
	 *
	 * @param sysUser
	 * @return
	 */
	@PostMapping("/UC3013")
	public Result findUserList(@RequestBody UserRequest sysUser) {
		List<SysUser> results = userService.findUserList(sysUser);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 批量人员导入
	 *
	 * @param file
	 * @return
	 */
	@PostMapping("/UC3014")
	public Result importUsers(
			@RequestParam(name = "file", required = false) MultipartFile file) {
		try {
			if (Objects.isNull(file) || file.isEmpty()) {
				return Result.failed("文件不能为空");
			}
			CsvReader csvReader = CsvUtil.getReader();
			List<ImportUserCsvVo> rows = csvReader.read(
					IoUtil.getReader(file.getInputStream(), "GBK"),
					ImportUserCsvVo.class);
			Map<String, List<ImportUserCsvVo>> map = UserOperatorUtils
					.checkImportUser(rows, userService);
			if (MapUtil.isNotEmpty(map)) {
				return Result.failed(map, "上传数据存在问题,请检查修改后上传");
			}
			userService.batchImportUserInfo(rows);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
			return Result.failed("操作失败");
		}
	}

	@PostMapping("/UC3015")
	public Result updateUserInfo(@RequestBody SysUser sysUser) {
		if (Objects.isNull(sysUser) || Objects.isNull(sysUser.getId())) {
			return Result.failed("id为必传项");
		}
		if (StrUtil.isNotBlank(sysUser.getUsername())) {
			return Result.failed("登录名称不能更改");
		}
		userService.updateUserInfo(sysUser);
		return Result.succeed("操作成功");
	}

	@GetMapping("/UC3016")
	public void downloadTemplate(HttpServletResponse response) {
		try {
			CsvReader csvReader = CsvUtil.getReader();
			List<ImportUserCsvVo> rows = csvReader.read(ResourceUtil.getReader(
					"userTemplate.csv", CharsetUtil.CHARSET_GBK),
					ImportUserCsvVo.class);
			CsvUtils.writeCsv(rows, response.getOutputStream(), response,
					"userTemplate.csv");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
		}
	}

	@RequestMapping("/UC3017")
	public Result<SdpsServerInfo> selectRangerUserInfo(
			@RequestParam("username") String username) {
		try {
			SdpsServerInfo rangerInfo = userService.selectServerUserInfo(
					username, ServerTypeEnum.C.name(), "password");
			if (Objects.isNull(rangerInfo)) {
				return Result.failed("暂无此用户");
			}
			return Result.succeed(rangerInfo, "操作成功");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
		}
		return Result.failed("操作失败");
	}

	@RequestMapping("/UC3018")
	public Result<JSONObject> getUserCnt() {
		try {
			long total = userService.count();

			long yesterday = userService
					.count(new QueryWrapper<SysUser>().lt("create_time",
							DateUtil.parse(DateUtil.now(),
									CommonConstant.DATE_FORMAT)));
			Long today = total;
			Long thisWeek = total;
			long lastWeek = userService.count(new QueryWrapper<SysUser>().lt(
					"create_time",
					DateUtil.parse(DateUtil.now(), CommonConstant.DATE_FORMAT)
							.offset(DateField.DAY_OF_MONTH,
									Math.negateExact(DateUtil.parse(
											DateUtil.now(),
											CommonConstant.DATE_FORMAT)
											.dayOfWeek() - 2))));
			JSONObject result = new JSONObject();
			result.put("total", total);
			result.put("today", today);
			result.put("yesterday", yesterday);
			result.put("thisWeek", thisWeek);
			result.put("lastWeek", lastWeek);
			return Result.succeed(result, "操作成功");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
		}
		return Result.failed("操作失败");
	}

	/**
	 * 获取url用户关系
	 * 
	 * @param urlId
	 * @param user
	 * @return
	 */
	@PostMapping("/getUrlUsers")
	public Result<UrlUserVo> getUrlUsers(@RequestParam("urlId") Long urlId,
			@LoginUser SysUser user) {
		return Result.succeed(userService.getUrlUsers(urlId, user));
	}

	/**
	 * 查询同步用户结果
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/UC3020")
	public Result<List<SdpsUserSyncInfo>> getUserSyncInfo(
			@RequestParam("userId") Long userId) {
		return Result.succeed(userService.getUserSyncInfo(userId));
	}

	@GetMapping("/UC3021/{id}")
	public Result getRolesByUserId(@PathVariable("id") Long userId) {
		JSONObject result = userService.getRolesByUserId(userId);
		return Result.succeed(result, "操作成功");
	}

	@PostMapping("/UC3022")
	public Result insertTenantUsers(@RequestBody UserVo userVo) {
		try {
			if (Objects.isNull(userVo.getTenantId())
					|| CollUtil.isEmpty(userVo.getUserIds())
					|| CollUtil.isEmpty(userVo.getRoleIds())) {
				return Result.failed("传入参数有误");
			}
			userService.insertTenantUsers(userVo);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			log.error("cluster-insert-error", ex);
			return Result.failed("操作失败");
		}
	}
}
