package com.seaboxdata.sdps.usersync.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.utils.FileUtils;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.UserSyncStatusEnum;
import com.seaboxdata.sdps.usersync.request.Request;
import com.seaboxdata.sdps.usersync.request.impl.UserSyncThreadRequest;
import com.seaboxdata.sdps.usersync.service.IKeytabService;
import com.seaboxdata.sdps.usersync.service.IUserService;
import com.seaboxdata.sdps.usersync.service.RequestAsyncProcessService;

@Slf4j
@RestController
@RequestMapping("/usersync")
public class UserSyncController {
	@Resource
	private RequestAsyncProcessService requestAsyncProcessService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IKeytabService keytabService;

	@PostMapping("/operator")
	public Result userSynOperator(@RequestBody UserSyncRequest userSyncRequest) {
		try {
			if (Objects.isNull(userSyncRequest.getUserId())) {
				if (CollUtil.isNotEmpty(userSyncRequest.getUserIds())) {
					userService.updateUsers(userSyncRequest.getUserIds(), null,
							UserSyncStatusEnum.SYNCING.getCode());
					userService
							.updateUser(userSyncRequest.getUserId(),
									Boolean.FALSE,
									UserSyncStatusEnum.SYNCING.getCode());
					for (int i = 0; i < userSyncRequest.getUserIds().size(); i++) {
						UserSyncRequest copyUserSyncRequest = new UserSyncRequest();
						BeanUtil.copyProperties(userSyncRequest,
								copyUserSyncRequest, true);
						copyUserSyncRequest.setUserIds(null);
						copyUserSyncRequest.setUserId(userSyncRequest
								.getUserIds().get(i));
						Request request = new UserSyncThreadRequest(
								copyUserSyncRequest);
						requestAsyncProcessService.process(request);
					}

				} else {
					return Result.failed(false, "异步同步提交参数存在问题");
				}
			} else {
				userService.updateUser(userSyncRequest.getUserId(), null,
						UserSyncStatusEnum.SYNCING.getCode());
				Request request = new UserSyncThreadRequest(userSyncRequest);
				requestAsyncProcessService.process(request);
			}

			return Result.succeed(true, "异步同步提交成功");
		} catch (Exception e) {
			log.error("同步用户异步提交报错", e);
		}
		return Result.failed(false, "异步同步提交成功");

	}

	/**
	 * 根据集群和用户获取keytab
	 * 
	 * @param clusterId
	 *            集群id
	 * @param keytabName
	 *            用户名
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/getKeytab")
	public Result getKeytab(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("keytabName") String keytabName) {
		try {
			String keytab = keytabService.getKeytab(clusterId, keytabName);
			return Result.succeed(keytab, "获取keytab文件成功");
		} catch (Exception e) {
			return Result.failed(false, e.getMessage());
		}
	}

	/**
	 * 根据集群获取krb5
	 * 
	 * @param clusterId
	 *            集群id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/getKrb5")
	public Result getKeytab(@RequestParam("clusterId") Integer clusterId) {
		try {
			String krb5 = keytabService.getKrb5(clusterId);
			return Result.succeed(krb5, "获取krb5文件成功");
		} catch (Exception e) {
			return Result.failed(false, e.getMessage());
		}
	}

	/**
	 * 票据查询
	 * 
	 * @param clusterId
	 *            集群id
	 * @param keytabName
	 *            keytab名
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @return
	 */
	@GetMapping("/selectKeytab")
	public PageResult selectKeytab(
			@RequestParam(value = "clusterId", required = false) Integer clusterId,
			@RequestParam(value = "keytabName", required = false) String keytabName,
			@RequestParam(value = "principalType", required = false) String principalType,
			@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		PageResult result;
		try {
			Page<SdpServerKeytab> page = keytabService.selectKeytab(clusterId,
					keytabName, principalType, pageNo, pageSize);
			result = PageResult.<SdpServerKeytab> builder().code(0)
					.data(page.getResult()).msg("操作成功").count(page.getTotal())
					.build();
		} catch (Exception e) {
			log.error("查询keytab失败", e);
			result = PageResult.<SdpServerKeytab> builder().code(-1)
					.msg("查询异常").build();
		}
		return result;
	}

	/**
	 * 下载keytab文件
	 * 
	 * @param path
	 *            keytab路径
	 * @param userAgent
	 * @param inline
	 * @return
	 */
	@GetMapping("/download")
	public void downlaodFile(
			@RequestParam("path") String path,
			@RequestHeader("user-agent") String userAgent,
			@RequestParam(required = false, defaultValue = "false") boolean inline) {
		String filename = path.substring(path.lastIndexOf("/") + 1);
		File file = new File(path);
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletRequest request = requestAttributes.getRequest();
		HttpServletResponse response = requestAttributes.getResponse();
		FileUtils.downloadFile(file, request, response, filename, false);
	}

	/**
	 * 检查keytab的有效性
	 *
	 * @param keytabs
	 *            keytab文件名
	 * @return
	 */
	@GetMapping("/checkKeytab")
	public Result checkKeytab(@RequestParam("keytabs") List<String> keytabs,
			@RequestParam("clusterIds") List<Integer> clusterIds) {
		try {
			keytabService.checkKeytab(keytabs, clusterIds);
			return Result.succeed("成功");
		} catch (Exception e) {
			log.error("检查keytab文件有效性失败", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * 更新keytab文件
	 *
	 * @param list
	 * @return
	 */
	@PostMapping("/updateKeytab")
	public Result updateKeytab(@RequestBody List<SdpServerKeytab> list) {
		try {
			KeytabUtil.updateKeytabs(list);
			return Result.succeed("成功");
		} catch (Exception e) {
			log.error("更新keytab失败", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * 从kdc节点拉取keytab文件
	 * 
	 * @param pathList
	 *            keytab路径集合
	 * @return
	 */
	@PostMapping("/pullKeytabFromKdc")
	public Result pullKeytabFromKdc(@RequestBody List<String> pathList) {
		try {
			Map<String, Object> map = keytabService.pullKeytabFromKdc(pathList);
			return Result.succeed(map, "成功");
		} catch (Exception e) {
			log.error("用户同步模块拉取keytab失败", e);
			return Result.failed("用户同步模块拉取keytab失败");
		}
	}
}
