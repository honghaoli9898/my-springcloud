package com.seaboxdata.sdps.usersync.service;

import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;

public interface UserSyncService {
	public boolean userSyncAdd(UserSyncRequest userSyncRequest);

	public boolean userSyncDelete(UserSyncRequest userSyncRequest);

	public boolean userSyncUpdate(UserSyncRequest userSyncRequest);
}
