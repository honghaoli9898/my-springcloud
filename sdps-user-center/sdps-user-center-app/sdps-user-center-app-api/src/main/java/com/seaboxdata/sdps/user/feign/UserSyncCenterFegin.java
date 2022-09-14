package com.seaboxdata.sdps.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;

@FeignClient(value = "user-sync-center")
public interface UserSyncCenterFegin {

	@PostMapping("/usersync/operator")
	public Result userSynOperator(@RequestBody UserSyncRequest userSyncRequest);
}
