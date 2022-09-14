package com.seaboxdata.sdps.seaboxProxy.config;

import io.leopard.javahost.JavaHost;

import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsClusterMapper;

/**
 * 定时任务，拉取路由信息 路由信息由路由项目单独维护
 */
@Slf4j
@Component
public class DynamicDataSourceScheduling {
	public static final Map<Integer, UserGroupInformation> kerberosMap = MapUtil
			.newConcurrentHashMap();
	@Autowired
	private SdpsClusterMapper clusterMapper;

	@Scheduled(cron = "0 0 0/12 * * ?")
	public void updateDynamicDatasourceKerberos() {
		kerberosMap.forEach((k, v) -> {
			try {
				String clusterHost = clusterMapper.selectById(k)
						.getClusterHostConf();
				JSONArray hosts = JSONArray.parseArray(clusterHost);
				if (Objects.nonNull(hosts)) {
					hosts.forEach(hostObj -> {
						JSONObject jsonObj = (JSONObject) hostObj;
						String host = jsonObj.getString("host");
						if (StrUtil.isBlank(host)) {
							host = jsonObj.getString("domainName");
						}
						String ip = jsonObj.getString("ip");
						JavaHost.updateVirtualDns(host, ip);
					});
				} else {
					log.info("未查到集群DNS映射,将使用系统DNS映射");
				}
				JavaHost.printAllVirtualDns();
				v.checkTGTAndReloginFromKeytab();
				log.info("更新集群{}:hbase kerberos成功", k);
			} catch (Exception e) {
				log.error("更新集群{}的hbase kerberos报错", k, e);
			}
		});
//		KerberosTicketUtil
//				.printTicketInfo(DynamicDataSourceScheduling.kerberosMap);
	}

}
