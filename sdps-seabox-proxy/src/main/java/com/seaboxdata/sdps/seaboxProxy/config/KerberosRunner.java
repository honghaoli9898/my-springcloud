package com.seaboxdata.sdps.seaboxProxy.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.seaboxProxy.mapper.SysGlobalArgsMapper;

@Component
@Order(1)
public class KerberosRunner implements ApplicationRunner {
	@Autowired
	private KerberosProperties kerberosProperties;
	@Autowired
	private SysGlobalArgsMapper sysGlobalArgsMapper;

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		if (kerberosProperties.getEnable()) {
			
			String keytabPath = kerberosProperties.getKeytabPath();
			Boolean enable = kerberosProperties.getEnable();
			String krb5 = kerberosProperties.getKrb5();
			String userSuffix = kerberosProperties.getUserSuffix();
			String adminKeytab = kerberosProperties.getAdminKeytab();
			String adminPrincipal = kerberosProperties.getAdminPrincipal();
			String userSyncKeytabPath = kerberosProperties
					.getUserSyncKeytabPath();
			String kdcKeytabPath = kerberosProperties.getKdcKeytabPath();
			String seaboxKeytabPath = kerberosProperties.getSeaboxKeytabPath();
			String itemKeytabPath = kerberosProperties.getItemKeytabPath();
			FileUtil.mkdir(seaboxKeytabPath);
			FileUtil.mkdir(kdcKeytabPath);
			List<SysGlobalArgs> kerberosArgs = sysGlobalArgsMapper
					.selectList(new QueryWrapper<SysGlobalArgs>().eq(
							"arg_type", "kerberos"));

			for (SysGlobalArgs sysGlobalArgs : kerberosArgs) {
				boolean isUpdate = false;
				switch (sysGlobalArgs.getArgKey()) {
				case "krb5":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							krb5)) {
						sysGlobalArgs.setArgValue(krb5);
						isUpdate = true;
					}
					break;
				case "enable":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							enable.toString())) {
						sysGlobalArgs.setArgValue(enable.toString());
						isUpdate = true;
					}
					break;
				case "keytabPath":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							keytabPath)) {
						sysGlobalArgs.setArgValue(keytabPath);
						isUpdate = true;
					}
					break;
				case "userSuffix":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							userSuffix)) {
						sysGlobalArgs.setArgValue(userSuffix);
						isUpdate = true;
					}
					break;
				case "adminKeytab":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							adminKeytab)) {
						sysGlobalArgs.setArgValue(adminKeytab);
						isUpdate = true;
					}
					break;
				case "adminPrincipal":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							adminPrincipal)) {
						sysGlobalArgs.setArgValue(adminPrincipal);
						isUpdate = true;
					}
					break;

				case "userSyncKeytabPath":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							userSyncKeytabPath)) {
						sysGlobalArgs.setArgValue(adminPrincipal);
						isUpdate = true;
					}
					break;
				case "kdcKeytabPath":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							kdcKeytabPath)) {
						sysGlobalArgs.setArgValue(adminPrincipal);
						isUpdate = true;
					}
					break;
				case "seaboxKeytabPath":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							seaboxKeytabPath)) {
						sysGlobalArgs.setArgValue(adminPrincipal);
						isUpdate = true;
					}
					break;
				case "itemKeytabPath":
					if (StrUtil.equalsIgnoreCase(sysGlobalArgs.getArgValue(),
							itemKeytabPath)) {
						sysGlobalArgs.setArgValue(adminPrincipal);
						isUpdate = true;
					}
					break;
				default:
					break;
				}
				if (isUpdate) {
					sysGlobalArgsMapper.updateById(sysGlobalArgs);
				}

			}
		}

	}
}