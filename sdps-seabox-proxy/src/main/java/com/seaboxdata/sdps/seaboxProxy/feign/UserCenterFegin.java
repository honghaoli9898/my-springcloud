package com.seaboxdata.sdps.seaboxProxy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;

@FeignClient(name = ServiceNameConstants.USER_SERVICE)
public interface UserCenterFegin {

	@RequestMapping(value = "/UC30/UC3017")
	public Result selectRangerUserInfo(@RequestParam("username")String username);
}
