package com.seaboxdata.sdps.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.model.Result;

@Component
@FeignClient(name = "uaa-server")
public interface UaaFeginService {
	@GetMapping("/check/code")
	public Result checkCode(@RequestParam("deviceId") String deviceId,
			@RequestParam("validCode") String validCode);
}
