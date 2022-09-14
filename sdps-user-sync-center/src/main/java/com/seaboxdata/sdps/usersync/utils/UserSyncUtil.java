package com.seaboxdata.sdps.usersync.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.UserSyncOperatorEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncStatusEnum;
import com.seaboxdata.sdps.usersync.mapper.SdpsUserSyncInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.usersync.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.usersync.service.IUserService;
import com.seaboxdata.sdps.usersync.service.UserSyncService;

public class UserSyncUtil {
	public static final String USER_SYNC_BEAN_NAME_SUFFIX = "UserSyncService";

	public static void processUserSyncByType(UserSyncRequest userSyncRequest) {
		UserSyncService userSyncService = SpringUtil.getBean(
				userSyncRequest.getBeanName(), UserSyncService.class);
		switch (UserSyncOperatorEnum.getEnumByCode(userSyncRequest
				.getOperation())) {
		case ADD:
			userSyncService.userSyncAdd(userSyncRequest);
			break;
		case UPDATE:
			userSyncService.userSyncUpdate(userSyncRequest);
			break;
		case DELETE:
			userSyncService.userSyncDelete(userSyncRequest);
			break;
		default:
			break;
		}
	}

	public static String getEncryptPassword(String pass) {
		SysGlobalArgsMapper globalArgsMapper = SpringUtil
				.getBean(SysGlobalArgsMapper.class);
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "publicKey"));
		return RsaUtil.encrypt(pass, sysGlobalArgs.getArgValue());
	}

	public static void userSyncEndProccessByType(UserSyncRequest userSyncRequest) {
		String operator = userSyncRequest.getOperation();
		Long userId = userSyncRequest.getUserId();
		SdpsUserSyncInfoMapper userSyncInfoMapper = SpringUtil
				.getBean(SdpsUserSyncInfoMapper.class);
		IUserService userService = SpringUtil.getBean(IUserService.class);
		long cnt = userSyncInfoMapper
				.selectList(
						new QueryWrapper<SdpsUserSyncInfo>()
								.select("sync_result").eq("user_id", userId)
								.eq("operator", operator)).stream()
				.filter(data -> !data.getSyncResult()).count();
		if (cnt == 0) {
			switch (UserSyncOperatorEnum.getEnumByCode(userSyncRequest
					.getOperation())) {
			case ADD:
				userService.updateUser(userId, Boolean.TRUE,
						UserSyncStatusEnum.SYNC_SUCCESS.getCode());
				break;
			case UPDATE:
				userService.updateUser(userId, Boolean.TRUE,
						UserSyncStatusEnum.SYNC_SUCCESS.getCode());
				break;
			case DELETE:
				userService.deleteUserAllInfo(userId);
				break;
			default:
				break;
			}
		} else {
			switch (UserSyncOperatorEnum.getEnumByCode(userSyncRequest
					.getOperation())) {
			case ADD:
				userService.updateUser(userId, Boolean.FALSE,
						UserSyncStatusEnum.SYNC_ERROR.getCode());
				break;
			case UPDATE:
				userService.updateUser(userId, Boolean.TRUE,
						UserSyncStatusEnum.SYNC_ERROR.getCode());
				break;
			case DELETE:
				userService.updateUser(userId, Boolean.FALSE,
						UserSyncStatusEnum.SYNC_ERROR.getCode());
				break;
			default:
				break;
			}
		}
	}

}
