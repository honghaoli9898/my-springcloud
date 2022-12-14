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
					return Result.failed(false, "????????????????????????????????????");
				}
			} else {
				userService.updateUser(userSyncRequest.getUserId(), null,
						UserSyncStatusEnum.SYNCING.getCode());
				Request request = new UserSyncThreadRequest(userSyncRequest);
				requestAsyncProcessService.process(request);
			}

			return Result.succeed(true, "????????????????????????");
		} catch (Exception e) {
			log.error("??????????????????????????????", e);
		}
		return Result.failed(false, "????????????????????????");

	}

	/**
	 * ???????????????????????????keytab
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param keytabName
	 *            ?????????
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/getKeytab")
	public Result getKeytab(@RequestParam("clusterId") Integer clusterId,
			@RequestParam("keytabName") String keytabName) {
		try {
			String keytab = keytabService.getKeytab(clusterId, keytabName);
			return Result.succeed(keytab, "??????keytab????????????");
		} catch (Exception e) {
			return Result.failed(false, e.getMessage());
		}
	}

	/**
	 * ??????????????????krb5
	 * 
	 * @param clusterId
	 *            ??????id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/getKrb5")
	public Result getKeytab(@RequestParam("clusterId") Integer clusterId) {
		try {
			String krb5 = keytabService.getKrb5(clusterId);
			return Result.succeed(krb5, "??????krb5????????????");
		} catch (Exception e) {
			return Result.failed(false, e.getMessage());
		}
	}

	/**
	 * ????????????
	 * 
	 * @param clusterId
	 *            ??????id
	 * @param keytabName
	 *            keytab???
	 * @param pageNo
	 *            ??????
	 * @param pageSize
	 *            ????????????
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
					.data(page.getResult()).msg("????????????").count(page.getTotal())
					.build();
		} catch (Exception e) {
			log.error("??????keytab??????", e);
			result = PageResult.<SdpServerKeytab> builder().code(-1)
					.msg("????????????").build();
		}
		return result;
	}

	/**
	 * ??????keytab??????
	 * 
	 * @param path
	 *            keytab??????
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
	 * ??????keytab????????????
	 *
	 * @param keytabs
	 *            keytab?????????
	 * @return
	 */
	@GetMapping("/checkKeytab")
	public Result checkKeytab(@RequestParam("keytabs") List<String> keytabs,
			@RequestParam("clusterIds") List<Integer> clusterIds) {
		try {
			keytabService.checkKeytab(keytabs, clusterIds);
			return Result.succeed("??????");
		} catch (Exception e) {
			log.error("??????keytab?????????????????????", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ??????keytab??????
	 *
	 * @param list
	 * @return
	 */
	@PostMapping("/updateKeytab")
	public Result updateKeytab(@RequestBody List<SdpServerKeytab> list) {
		try {
			KeytabUtil.updateKeytabs(list);
			return Result.succeed("??????");
		} catch (Exception e) {
			log.error("??????keytab??????", e);
			return Result.failed(e.getMessage());
		}
	}

	/**
	 * ???kdc????????????keytab??????
	 * 
	 * @param pathList
	 *            keytab????????????
	 * @return
	 */
	@PostMapping("/pullKeytabFromKdc")
	public Result pullKeytabFromKdc(@RequestBody List<String> pathList) {
		try {
			Map<String, Object> map = keytabService.pullKeytabFromKdc(pathList);
			return Result.succeed(map, "??????");
		} catch (Exception e) {
			log.error("????????????????????????keytab??????", e);
			return Result.failed("????????????????????????keytab??????");
		}
	}
}
