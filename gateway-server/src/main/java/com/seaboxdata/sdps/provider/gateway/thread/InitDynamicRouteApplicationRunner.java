package com.seaboxdata.sdps.provider.gateway.thread;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.seaboxdata.sdps.provider.gateway.dynamic.DynamicRouteScheduling;
import com.seaboxdata.sdps.provider.gateway.dynamic.DynamicRouteService;
@Slf4j
@Component
public class InitDynamicRouteApplicationRunner implements ApplicationRunner {
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private DynamicRouteService dynamicRouteService;
	
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	DynamicRouteScheduling.refreshDynamicRouteInfo(restTemplate, dynamicRouteService);
    	log.info("初始化动态路由完成");
    }
}
