package com.seaboxdata.sdps.job.executor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.FileUtil;

import com.seaboxdata.sdps.common.core.properties.KerberosProperties;

@Component
@Order(1)
public class KerberosRunner implements ApplicationRunner {
	@Autowired
	private KerberosProperties kerberosProperties;

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		if (kerberosProperties.getEnable()) {
			String kdcKeytabPath = kerberosProperties.getKdcKeytabPath();
			FileUtil.mkdir(kdcKeytabPath);
		}

	}
}