package com.seaboxdata.sdps.item.aspect;

import io.leopard.javahost.JavaHost;

import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.security.UserGroupInformation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.item.config.DynamicDataSourceScheduling;
import com.seaboxdata.sdps.item.constant.DataSourceConstant;
import com.seaboxdata.sdps.item.enums.DbSourcrTypeEnum;
import com.seaboxdata.sdps.item.enums.DbTypeEnum;
import com.seaboxdata.sdps.item.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceTypeMapper;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;

/**
 * 审计日志切面
 *
 */
@Slf4j
@Aspect
@Component
public class DatasourceSecurityAspect {
	@Pointcut("execution(* com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator.createDataSource(..))")
	public void requestServer() {
	}

	@Autowired
	private KerberosProperties kerberosProperties;

	@Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.dynamic.datasource.master.url}")
	private String url;

	@Value("${spring.datasource.dynamic.datasource.master.username}")
	private String username;

	@Value("${spring.datasource.dynamic.datasource.master.password}")
	private String password;

	@Value("${security.kerberos.login.krb5}")
	private String krb5;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Around("requestServer()")
	public Object doAround(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		Map<String, Object> requestParams = getRequestParamsByProceedJoinPoint(proceedingJoinPoint);
		DataSourceProperty dataSourceProperty = null;
		for (Entry<String, Object> entry : requestParams.entrySet()) {
			if (entry.getValue() instanceof DataSourceProperty) {
				dataSourceProperty = (DataSourceProperty) entry.getValue();
			}
		}
		SdpsDatasourceTypeMapper datasourceTypeMapper = null;
		SdpsDatasourceMapper datasourceMapper = null;
		SdpsClusterMapper clusterMapper = null;
		final List<Object> dataSource = CollUtil.newArrayList();
		if (kerberosProperties.getEnable()
				&& dataSourceProperty.getUrl().contains("hive")) {
			Integer clusterId = null;
			String properties = null;
			String clusterHost = null;
			try {
				datasourceTypeMapper = SpringUtil
						.getBean(SdpsDatasourceTypeMapper.class);
				datasourceMapper = SpringUtil
						.getBean(SdpsDatasourceMapper.class);
				clusterMapper = SpringUtil.getBean(SdpsClusterMapper.class);
			} catch (Exception e) {
			}
			if (Objects.nonNull(datasourceTypeMapper)
					&& Objects.nonNull(datasourceMapper)
					&& Objects.nonNull(clusterMapper)) {
				Long typeId = datasourceTypeMapper
						.selectOne(
								new QueryWrapper<SdpsDatasourceType>()
										.select("id")
										.eq("name", DbTypeEnum.H.getCode())
										.eq("category",
												DbSourcrTypeEnum.cluster.name()))
						.getId();
				SdpsDatasource sdpsDatasource = datasourceMapper
						.selectOne(new QueryWrapper<SdpsDatasource>().eq(
								"name", dataSourceProperty.getPoolName()).eq(
								"type_id", typeId));
				clusterHost = clusterMapper.selectById(
						sdpsDatasource.getClusterId()).getClusterHostConf();
				clusterId = sdpsDatasource.getClusterId().intValue();
				properties = sdpsDatasource.getProperties();
			} else {
				Connection conn = null;
				Statement stmt = null;
				try {
					if (!StrUtil.isEmpty(driverClassName)) {
						Class.forName(driverClassName);
						log.info("成功加载数据库驱动程序");
					}
					conn = DriverManager.getConnection(url, username, password);
					log.info("成功获取数据库连接");
					String sql = "select cluster_host_conf,sd.cluster_id,properties from sdps_datasource as sd LEFT JOIN sdps_cluster as sc on sc.cluster_id = sd.cluster_id where name = ? and type_id = (select id from sdps_datasource_type where name='HIVE' and category = 'cluster') ";
					sql = sql.replace("?",
							"'" + dataSourceProperty.getPoolName() + "'");
					stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					while (rs.next()) {
						clusterId = rs.getInt("cluster_id");
						properties = rs.getString("properties");
						clusterHost = rs.getString("cluster_host_conf");
					}
				} catch (Exception e) {
					log.debug("获取数据源信息失败", e);
				} finally {
					JdbcUtils.closeConnection(conn);
					JdbcUtils.closeStatement(stmt);
				}
			}
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
			JSONObject jsonObject = JSONObject.parseObject(properties);
			Boolean isSecurity = jsonObject
					.getBooleanValue(DataSourceConstant.IS_SECURITY);
			if (isSecurity) {
				Configuration conf = new Configuration();
				conf.setBoolean(
						CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
						true);
				System.setProperty("java.security.krb5.conf", krb5);
				conf.set("hadoop.security.authentication", "kerberos");
				UserGroupInformation.setConfiguration(conf);
				UserGroupInformation userGroupInformation = null;
				try {
					String username = dataSourceProperty.getUsername();
					String host = ReUtil.get("hive/([\\s\\S]+)"
							.concat(kerberosProperties.getUserSuffix()),
							username, 1);
					userGroupInformation = UserGroupInformation
							.loginUserFromKeytabAndReturnUGI(
									dataSourceProperty.getUsername(),
									kerberosProperties.getItemKeytabPath()
											.concat("/").concat(host)
											.concat(".")
											.concat(clusterId.toString())
											.concat(".hive.service")
											.concat(".keytab"));
					dataSourceProperty.setUsername(null);
					dataSourceProperty.setPassword(null);
					DynamicDataSourceScheduling.kerberosMap.put(clusterId,
							userGroupInformation);
				} catch (Exception e) {
					log.error("身份认证异常:{} ", e);
					throw new BusinessException(
							"身份认证异常:".concat(e.getMessage()));
				}

				userGroupInformation.doAs(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						try {
							dataSource.add(proceedingJoinPoint.proceed());
						} catch (Throwable e) {
							log.error("添加数据源报错", e);
						}
						return null;
					}
				});
			} else {
				dataSource.add(proceedingJoinPoint.proceed());
			}
		} else {
			dataSource.add(proceedingJoinPoint.proceed());
		}

		return dataSource.get(0);
	}

	private Map<String, Object> getRequestParamsByProceedJoinPoint(
			JoinPoint joinPoint) {
		String[] paramNames = ((MethodSignature) joinPoint.getSignature())
				.getParameterNames();
		Object[] paramValues = joinPoint.getArgs();
		return buildRequestParam(paramNames, paramValues);
	}

	private Map<String, Object> buildRequestParam(String[] paramNames,
			Object[] paramValues) {
		Map<String, Object> requestParams = MapUtil.newHashMap();
		for (int i = 0; i < paramNames.length; i++) {
			Object value = paramValues[i];
			if (value instanceof MultipartFile) {
				MultipartFile file = (MultipartFile) value;
				value = file.getOriginalFilename();
			}
			requestParams.put(paramNames[i], value);
		}

		return requestParams;
	}

}
