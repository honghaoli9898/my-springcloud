package com.seaboxdata.sdps.provider.gateway.constant;

import cn.hutool.core.map.MapUtil;

import java.util.Collections;
import java.util.Map;

public interface LoginServerConstants {
	String server_type = "Server-Type";
	
	String ws_type = "ws-type";
	
	String cluster_id = "clusterId";

	String ambari_url_path = "api-cluster";

	String sdo_url_path = "api-hue";
	
	String grafana_url_path = "api-grafana";

	String logsearch_url_path = "api-logsearch";

	String s_hbase_url_path = "api-shbase";

	String sfl2_url_path = "api-sfl2";

	String slog2_url_path = "api-slog2";
	
	String sredis_url_path = "api-redis";
	
	String ssm_url_path = "api-ssm";

	// OSS url
	String SEA_OSS_URL_PATH = "api-oss";

	String SDT2_URL_PATH = "api-sdt2";
	
	String SKAFKA_URL_PATH = "api-skafka";
	
	String SCS_URL_PATH = "api-scs";
	
	String SMS_URL_PATH = "api-sms";

	String login_cert_redis_key = "server_login_cert:";

	String redis_join = ":";

	String ambari_login_username = "admin";

	String hue_login_username = "sdo";

	String log_search_login_username = "admin";

	String shbase_login_username = "admin";

	String sfl2_login_username = "admin";

	String slog2_login_username = "admin";
	
	String scs_login_username = "admin";
	
	String sms_login_username = "admin";
	
	String sredis_login_username = "admin";

	// OSS 登录用户
	String SEA_OSS_LOGIN_USERNAME = "admin";

	// sdt2 登录用户
	String SDT2_LOGIN_USERNAME = "admin";

	Long timeout = 5000L;

	Long sleep = 500L;

	Long redis_timeout = 30L;
	
	String sdo_authorization_header = "S-Authorization";
	
	String sdp_authorization_header = "Authorization";
	
	String cert_url_path = "/login/cert";

	Map<String, String> loginServerMap = Collections.synchronizedMap(MapUtil
			.newConcurrentHashMap());
}
