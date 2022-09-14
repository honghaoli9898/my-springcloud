package com.seaboxdata.sdps.item.config;

import io.leopard.javahost.JavaHost;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.item.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.item.service.impl.DynamicOperatorDataSourceService;

/**
 * 定时任务，拉取路由信息 路由信息由路由项目单独维护
 */
@Slf4j
@Component
public class DynamicDataSourceScheduling {
	public static final Map<Integer, UserGroupInformation> kerberosMap = MapUtil
			.newConcurrentHashMap();
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DynamicOperatorDataSourceService dynamicOperatorDataSourceService;

	@Autowired
	private KerberosProperties kerberosProperties;

	@Autowired
	private SdpsClusterMapper clusterMapper;

	// 每60秒中执行一次
	@Scheduled(cron = "*/60 * * * * ?")
	public void updateDynamicDatasource() {
		try {
			List<Map<String, Object>> rows = jdbcTemplate
					.queryForList("select sd.name,sd.properties,sdt.name as type from sdps_datasource as sd inner join sdps_datasource_type as sdt on sdt.id = sd.type_id where sd.is_valid = 1");
			if (CollUtil.isNotEmpty(rows)) {
				rows.forEach(row -> {
					try {
						String name = (String) row.get("name");
						String properties = (String) row.get("properties");
						String type = (String) row.get("type");
						Set<String> dataSources = dynamicOperatorDataSourceService
								.getCurrentDataSources();
						if (!dataSources.contains(name)) {
							dynamicOperatorDataSourceService.add(name, type,
									properties);
						}
					} catch (Exception e) {
						log.error("定时更新数据源报错", e);
					}
				});
			}
		} catch (Exception e2) {
			log.error("定时更新数据源报错", e2);
		}

	}

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
				log.info("更新集群{}:hive kerberos成功", k);
			} catch (Exception e) {
				log.error("更新集群{}的hive连接报错", k, e);
			}
		});
//		KerberosTicketUtil
//				.printTicketInfo(DynamicDataSourceScheduling.kerberosMap);
	}

}
