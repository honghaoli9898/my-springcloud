package com.seaboxdata.sdps.bigdataProxy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;

@Component
@FeignClient(value = "tbds-proxy-server", configuration = MultipartSupportConfig.class)
public interface TbdsFeignService {

    @GetMapping(value = "/tbds/selectHdfs")
    public String selectHdfs();

}
