package com.seaboxdata.sdps.seaboxProxy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;

@FeignClient(name = ServiceNameConstants.ITEM_CENTER)
public interface ItemCenterFegin {

	/**
	 *
	 * @param username
	 * @param userId
	 * @param clusterId
	 * @return
	 */
	@RequestMapping(value = "/MC10/MC1025")
	public Result findItemsByUser(@RequestParam("username") String username,
			@RequestParam("userId") Long userId,
			@RequestParam("clusterId") Long clusterId);
}
