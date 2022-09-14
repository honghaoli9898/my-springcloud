package com.seaboxdata.sdps.seaboxProxy.config;

import java.security.PrivilegedAction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.support.http.stat.WebAppStat;
import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidStatConfig;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;

/**
 * 切换数据源
 */
@Component
@Data
@Slf4j
public class DynamicDataSourceConfig {

	/**
	 * 初始化默认连接池大小
	 */
	@Value("${spring.datasource.dynamic.druid.initial-size}")
	private int initialSize;
	/**
	 * 最小连接数量
	 */
	@Value("${spring.datasource.dynamic.druid.min-idle}")
	private int minIdle;
	/**
	 * 最大活跃连接数量
	 */
	@Value("${spring.datasource.dynamic.druid.max-active}")
	private int maxActive;
	/**
	 * 最大等待时间
	 */
	@Value("${spring.datasource.dynamic.druid.max-wait}")
	private int maxWait;

	/**
	 * 操作运行时间间隔
	 */
	@Value("${spring.datasource.dynamic.druid.time-between-eviction-runs-millis}")
	private long timeBetweenEvictionRunsMillis;
	/**
	 * 最小回收空闲时间
	 */
	@Value("${spring.datasource.dynamic.druid.min-evictable-idle-time-millis}")
	private long minEvictableIdleTimeMillis;

	/**
	 * 用来检测连接是否有效的sql
	 */
	@Value("${spring.datasource.dynamic.druid.validation-query}")
	private String validationQuery;

	/**
	 * 是否检测空闲连接
	 */
	@Value("${spring.datasource.dynamic.druid.test-while-idle}")
	private boolean testWhileIdle;
	/**
	 * 申请连接时执行validationQuery检测连接是否有效
	 */
	@Value("${spring.datasource.dynamic.druid.test-on-borrow}")
	private boolean testOnBorrow;
	/**
	 * 归还连接时执行validationQuery检测连接是否有效
	 */
	@Value("${spring.datasource.dynamic.druid.test-on-return}")
	private boolean testOnReturn;
	/**
	 * 过滤插件
	 */
	@Value("${spring.datasource.dynamic.druid.filters}")
	private String filters;

	/**
	 * 合并多个DruidDataSource的监控数据
	 */
	@Value("${spring.datasource.dynamic.druid.use-global-data-source-stat}")
	private boolean useGlobalDataSourceStat;

	/**
	 * 其他链接信息
	 */
	@Value("${spring.datasource.dynamic.druid.connect-properties}")
	private String connectProperties;

	/* druid插件web-stat-filter */
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.web-stat-filter.enabled}")
	private boolean webStatFilterEnabled;
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.web-stat-filter.url-pattern}")
	private String webStatFilterUrlPattern;
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.web-stat-filter.exclusions}")
	private String webStatFilterExclusions;
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.web-stat-filter.session-stat-enable}")
	private boolean webStatFilterSessionStatEnable;
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.web-stat-filter.session-stat-max-count}")
	private int webStatFilterSessionStatMaxCount;

	/* 配置 Druid 监控信息显示页面 */
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.stat-view-servlet.enabled}")
	private boolean statViewServletEnabled;
	/**
     *
     */
	@Value("${spring.datasource.dynamic.druid.stat-view-servlet.url-pattern}")
	private String statViewServletUrlPattern;
	/**
	 * 允许清空统计数据
	 */
	@Value("${spring.datasource.dynamic.druid.stat-view-servlet.reset-enable}")
	private boolean statViewServletResetEnable;
	/**
	 * 用户名
	 */
	@Value("${spring.datasource.dynamic.druid.stat-view-servlet.login-username}")
	private String statViewServletLoginUsername;
	/**
	 * 密码
	 */
	@Value("${spring.datasource.dynamic.druid.stat-view-servlet.login-password}")
	private String statViewServletLoginPassword;

	@Autowired
	private DruidDataSourceCreator druidDataSourceCreator;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private BigdataVirtualHost bigdataVirtualHost;

	@Autowired
	private SdpsClusterMapper clusterMapper;

	@Autowired
	private KerberosProperties kerberosProperties;

	/**
	 * 获取druid连接池配置
	 * 
	 * @param type
	 *            数据源类型
	 * @return
	 */
	public DruidConfig getDruidConfig(String type) {
		DruidConfig druidConfig = new DruidConfig();
		druidConfig.setInitialSize(initialSize);
		druidConfig.setMaxActive(maxActive);
		druidConfig.setMinIdle(minIdle);
		druidConfig.setMaxWait(maxWait);
		druidConfig.setValidationQuery(validationQuery);
		druidConfig
				.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		druidConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		druidConfig.setTestWhileIdle(testWhileIdle);
		druidConfig.setTestOnBorrow(testOnBorrow);
		druidConfig.setTestOnReturn(testOnReturn);
		druidConfig.setUseGlobalDataSourceStat(useGlobalDataSourceStat);

		DruidStatConfig druidStatConfig = new DruidStatConfig();
		String[] split = connectProperties.split(";");
		Map<String, Object> map = Maps.newHashMap();
		for (String prop : split) {
			String[] keyValue = prop.split("=");
			map.put(keyValue[0], keyValue[1]);
		}
		druidStatConfig.setMergeSql(Boolean.valueOf(map.get(
				"druid.stat.mergeSql").toString()));
		druidStatConfig.setSlowSqlMillis(Long.valueOf(map.get(
				"druid.stat.slowSqlMillis").toString()));
		druidConfig.setStat(druidStatConfig);

		WebStatFilter webStatFilter = new WebStatFilter();
		WebAppStat webAppStat = new WebAppStat();
		webAppStat.setMaxStatSessionCount(webStatFilterSessionStatMaxCount);
		webStatFilter.setWebAppStat(webAppStat);
		webStatFilter.setSessionStatEnable(webStatFilterSessionStatEnable);

		Properties properties = new Properties();
		druidConfig.setConnectionProperties(properties);
		druidConfig.setFilters(filters);

		if (CommonConstraint.phoenix.equals(type)) {
			getPhoenixDruidConfig(druidConfig);
		}
		return druidConfig;
	}

	/**
	 * 获取phoenix连接池配置信息
	 * 
	 * @param druidConfig
	 */
	public void getPhoenixDruidConfig(DruidConfig druidConfig) {
		Properties properties = druidConfig.getConnectionProperties();
		properties.setProperty("phoenix.schema.isNamespaceMappingEnabled",
				"true");
	}

	/**
	 * 移除连接池
	 * @param dataSourceName 连接池名
	 * @throws SQLException
	 */
	public void removeDataSource(String dataSourceName) {
		DynamicRoutingDataSource dds = (DynamicRoutingDataSource) dataSource;
		dds.removeDataSource(dataSourceName);
	}

	/**
	 * 根据集群和数据源名获取数据源
	 * 
	 * @param clusterId
	 *            集群id
	 * @param dataSourceName
	 *            数据源名
	 */
	public void changePhoenixDataSource(Integer clusterId, String dataSourceName) {
		SdpsCluster sdpsCluster = clusterMapper.selectById(clusterId);
		// 设置虚拟DNS
		bigdataVirtualHost.setVirtualHost(clusterId);
		DynamicRoutingDataSource dds = (DynamicRoutingDataSource) dataSource;
		Set<String> strings = dds.getCurrentDataSources().keySet();
		// System.setProperty("HADOOP_USER_NAME", "hbase");
		if (strings.contains(dataSourceName)) {
			return;
		}
		//获取hbase组件所在的host
		AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		JSONObject componentAndHost = ambariUtil.getComponentAndHost("HBASE");
		String phoenixHost = AmbariUtil.getHostByComponentByService("PHOENIX_QUERY_SERVER", componentAndHost);
		String hbaseMasterHost = AmbariUtil.getHostByComponentByService("HBASE_MASTER", componentAndHost);
		String hbaseServerHost = AmbariUtil.getHostByComponentByService("HBASE_REGIONSERVER", componentAndHost);
		// 获取hbase配置信息
		ArrayList<String> confList = new ArrayList<>();
		confList.add("hbase-site");
		confList.add("hbase-env");
		String confJson = new AmbariUtil(clusterId)
				.getAmbariServerConfByConfName("HBASE", confList);
		Map confMap = JSON.parseObject(confJson, Map.class);
		String zkQuorum = confMap.get("hbase.zookeeper.quorum").toString();
		String zkPort = confMap.get("hbase.zookeeper.property.clientPort")
				.toString();
		String zkDir = confMap.get("zookeeper.znode.parent").toString();
		String user = confMap.get("hbase.superuser").toString();
		String url = "jdbc:phoenix:" + zkQuorum + ":" + zkPort + ":" + zkDir;
		// 设置属性
		DruidConfig phoenix = getDruidConfig("phoenix");

		DataSourceProperty dataSourceProperty = new DataSourceProperty();
		dataSourceProperty.setDruid(phoenix);
		dataSourceProperty.setType(DruidDataSource.class);
		dataSourceProperty.setLazy(false);
		dataSourceProperty
				.setDriverClassName("org.apache.phoenix.jdbc.PhoenixDriver");
		dataSourceProperty.setUsername(user);
		dataSourceProperty.setUrl(url);
		Properties properties = new Properties();
		properties.setProperty("phoenix.schema.isNamespaceMappingEnabled",
				"true");
		properties.setProperty("phoenix.schema.mapSystemTablesToNamespace",
				"true");
		phoenix.setConnectionProperties(properties);
		if (kerberosProperties.getEnable() && sdpsCluster.getKerberos()) {
			String hbaseUserKeytab = confMap.get("hbase_user_keytab")
					.toString();
			hbaseUserKeytab = hbaseUserKeytab
					.substring(hbaseUserKeytab.lastIndexOf("/")+1);
			String hbaseUser = confMap.get("hbase_principal_name").toString();
			String phoenixKeytab = confMap.get(
					"phoenix.queryserver.keytab.file").toString();
			phoenixKeytab = phoenixKeytab.substring(phoenixKeytab
					.lastIndexOf("/")+1);
			String masterKeytab = confMap.get("hbase.master.keytab.file")
					.toString();
			masterKeytab = masterKeytab
					.substring(masterKeytab.lastIndexOf("/")+1);
			String regionserverKeytab = confMap.get(
					"hbase.regionserver.keytab.file").toString();
			regionserverKeytab = regionserverKeytab
					.substring(regionserverKeytab.lastIndexOf("/")+1);
			String masterKerberosPrincipal = confMap.get(
					"hbase.master.kerberos.principal").toString();
			String regionserverKerberosPrincipal = confMap.get(
					"hbase.regionserver.kerberos.principal").toString();
			String phoenixKerberosPrincipal = confMap.get(
					"phoenix.queryserver.kerberos.principal").toString();
			properties.setProperty("hbase.zookeeper.quorum", zkQuorum);
			properties.setProperty("hbase.master.kerberos.principal",
					masterKerberosPrincipal);
			properties.setProperty("hbase.master.keytab.file", hbaseMasterHost.concat(".")
					.concat(clusterId.toString()).concat(".").concat(masterKeytab));
			properties.setProperty("hbase.regionserver.kerberos.principal",
					regionserverKerberosPrincipal);
			properties.setProperty("hbase.regionserver.keytab.file", hbaseServerHost.concat(".")
					.concat(clusterId.toString()).concat(".").concat(regionserverKeytab));
			properties.setProperty("phoenix.queryserver.kerberos.principal",
					phoenixKerberosPrincipal);
			properties.setProperty("phoenix.queryserver.keytab.file", phoenixHost.concat(".")
					.concat(clusterId.toString()).concat(".").concat(phoenixKeytab));
			properties.setProperty("hbase.security.authentication", "kerberos");
			properties
					.setProperty("hadoop.security.authentication", "kerberos");
			properties.setProperty("zookeeper.znode.parent", zkDir);
			System.setProperty("java.security.krb5.conf",
					kerberosProperties.getKrb5());
			System.setProperty("javax.security.auth.useSubjectCredsOnl",
					"false");
			Configuration conf = new Configuration();
			conf.setBoolean(
					CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
					true);
			conf.set("hadoop.security.authentication", "kerberos");
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation userGroupInformation = null;
			try {
				userGroupInformation = UserGroupInformation
						.loginUserFromKeytabAndReturnUGI(
								hbaseUser,
								kerberosProperties.getSeaboxKeytabPath().concat("/")
										.concat(clusterId.toString())
										.concat(".").concat(hbaseUserKeytab));
			} catch (Exception e) {
				log.error("身份认证异常:{} ", e);
				throw new BusinessException("身份认证异常:".concat(e.getMessage()));
			}
			userGroupInformation.doAs(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					try {
						DataSource dataSource = druidDataSourceCreator
								.createDataSource(dataSourceProperty);
						dds.addDataSource(dataSourceName, dataSource);
					} catch (Exception e) {
						log.error("添加hbase kerberos报错", e);
					}
					return null;
				}
			});
			DynamicDataSourceScheduling.kerberosMap.put(clusterId,
					userGroupInformation);
		} else {
			String superUser = confMap.get("hbase.superuser").toString();
			UserGroupInformation ugi = UserGroupInformation
					.createRemoteUser(superUser);
			ugi.doAs(new PrivilegedAction<Object>() {

				@Override
				public Object run() {
					try {
						DataSource dataSource = druidDataSourceCreator
								.createDataSource(dataSourceProperty);
						dds.addDataSource(dataSourceName, dataSource);
					} catch (Exception e) {
						log.error("添加hbase kerberos报错", e);
					}
					return null;
				}
			});

		}

	}
}
