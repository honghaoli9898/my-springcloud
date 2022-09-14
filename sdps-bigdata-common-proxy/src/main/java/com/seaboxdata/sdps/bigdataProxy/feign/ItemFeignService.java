package com.seaboxdata.sdps.bigdataProxy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.seaboxdata.sdps.bigdataProxy.bean.ItemDto;
import com.seaboxdata.sdps.bigdataProxy.bean.PageItemRequest;
import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;
import com.seaboxdata.sdps.common.core.model.PageResult;

@Component
@FeignClient(name = "item-server", configuration = MultipartSupportConfig.class)
public interface ItemFeignService {
	/**
	 * 分页查询项目
	 * @param request
	 * @return
	 */
	@PostMapping("/MC10/MC1006")
	public PageResult<ItemDto> findItems(
			@Validated @RequestBody PageItemRequest request);
}
