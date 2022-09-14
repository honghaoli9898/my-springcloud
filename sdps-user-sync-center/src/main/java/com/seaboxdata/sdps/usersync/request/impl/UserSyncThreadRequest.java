package com.seaboxdata.sdps.usersync.request.impl;

import lombok.extern.slf4j.Slf4j;

import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.usersync.request.Request;
import com.seaboxdata.sdps.usersync.utils.UserSyncUtil;

@Slf4j
public class UserSyncThreadRequest implements Request {

	private UserSyncRequest userSyncRequest;

	public UserSyncThreadRequest(UserSyncRequest userSyncRequest) {
		this.userSyncRequest = userSyncRequest;
	}

	@Override
	public void process() {
		try {
			for (String code : userSyncRequest.getSyncTypes()) {
				userSyncRequest.setBeanName(code.concat(
						UserSyncUtil.USER_SYNC_BEAN_NAME_SUFFIX));
				UserSyncUtil.processUserSyncByType(userSyncRequest);
			}
			UserSyncUtil.userSyncEndProccessByType(userSyncRequest);
		} catch (Exception e) {
			log.error("用户ID:{},同步失败", userSyncRequest.getUserId(), e);
		}
	}

	@Override
	public Long getUserId() {
		return userSyncRequest.getUserId();
	}

}
