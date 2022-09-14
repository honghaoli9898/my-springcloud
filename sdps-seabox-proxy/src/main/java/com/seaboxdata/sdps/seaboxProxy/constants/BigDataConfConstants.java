package com.seaboxdata.sdps.seaboxProxy.constants;

import java.util.List;

import cn.hutool.core.collection.CollUtil;

/**
 * 大数据常量类
 */
public class BigDataConfConstants {
    /**
     * 配置文件名称
     */
    public static final String HDFS_CONF_CORE_SITE_FILE = "core-site.xml";
    public static final String HDFS_CONF_HDFS_SITE_FILE = "hdfs-site.xml";
    public static final String YARN_CONF_YARN_SITE_FILE = "yarn-site.xml";

    /**
     * Ranger用户的角色（ROLE_USER:普通用户,ROLE_ADMIN_AUDITOR:角色管理审计,ROLE_SYS_ADMIN:管理员）
     */
    public static final String RANGER_ROLE_USER = "ROLE_USER";
    public static final String RANGER_ADMIN_AUDITOR = "ROLE_ADMIN_AUDITOR";
    public static final String RANGER_SYS_ADMIN = "ROLE_SYS_ADMIN";
    /**
     * Ranger查询用户，API中常量参数：用户名
     */
    public static final String RANGER_QUERY_USER_API_PARAMS_USERNAME = "name";
    /**
     * Ranger删除用户，API中常量参数：是否强制删除
     */
    public static final String RANGER_DELETE_USER_API_PARAMS_FORCE = "forceDelete";
    /**
     * Ranger查询用户组，API中常量参数：用户名
     */
    public static final String RANGER_QUERY_GROUP_API_PARAMS_GROUPNAME = "name";

    /**
     * url请求前缀
     */
    public static final String HTTP_URL_PREFIX = "http://";

    /**
     * url请求链接的分号分隔符(:)
     */
    public static final String HTTP_URL_SEPARATOR = ":";

    /**
     * hdfs元数据文件前缀
     */
    public static final String HDFS_META_DATA_IMAGE_FILE_PREFIX = "fsimage";
    /**
     * hdfs元数据解析文件前缀
     */
    public static final String HDFS_META_DATA_EXTRACT_FILE_PREFIX = "extract";

    /**
     * 提取hdfs元数据分隔符
     */
    public static final String EXTRACT_HDFS_SOURCE_DATA_DELIMITER = ":";

    /**
     * yarn queue配置保文解析前缀
     */
    public static final String YARN_QUEUE_CONF_PREFIX = "yarn.scheduler.capacity.";
   
    /**
     * yarn queue其他配置key
     */
    public static final String YARN_QUEUE_CONF_MAXIMUM_APPLICATIONS = YARN_QUEUE_CONF_PREFIX.concat("maximum-applications");
    
    public static final String YARN_QUEUE_CONF_MAXIMUM_AM_RESOURCE_PERCENT = YARN_QUEUE_CONF_PREFIX.concat("maximum-am-resource-percent");
    
    public static final String YARN_QUEUE_CONF_NODE_LOCALITY_DELAY = YARN_QUEUE_CONF_PREFIX.concat("node-locality-delay");
    
    public static final String YARN_QUEUE_CONF_CAPACITY = ".capacity";

    /**
     * 队列参数
     */
    public static final String YARN_QUEUE_CONF_QUEUES = ".queues";
    
    public static final String YARN_QUEUE_CONF_STATE = ".state";
    public static final String YARN_QUEUE_CONF_MAXIMUM_CAPACITY = ".maximum-capacity";
    public static final String YARN_QUEUE_CONF_PRIORITY = ".priority";
    public static final String YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_MB = ".maximum-allocation-mb";
    public static final String YARN_QUEUE_CONF_MAXIMUM_ALLOCATION_VCORES = ".maximum-allocation-vcores";
    public static final String YARN_QUEUE_CONF_USER_LIMIT_FACTOR = ".user-limit-factor";
    public static final String YARN_QUEUE_CONF_MINIMUM_USER_LIMIT_PERCENT = ".minimum-user-limit-percent";
    public static final String YARN_QUEUE_CONF_MAX_APPLICATIONS = ".maximum-applications";
    public static final String YARN_QUEUE_CONF_MAX_AM_RESOURCE_PERCENT = ".maximum-am-resource-percent";
    public static final String YARN_QUEUE_CONF_ACL_ADMINISTER_QUEUE = ".acl_administer_queue";
    public static final String YARN_QUEUE_CONF_ACL_SUMBIT_APPLICATIONS = ".acl_submit_applications";
    public static final String YARN_QUEUE_CONF_ORDERING_POLICY = ".ordering-policy";
    public static final String YARN_QUEUE_CONF_DEFAULT_CAPACITY = "capacity";
    public static final String YARN_SCHEDULER_MAXIMUM_ALLOCATION_MB ="yarn.scheduler.maximum-allocation-mb";
    public static final String YARN_SCHEDULER_MAXIMUM_ALLOCATION_VCORES ="yarn.scheduler.maximum-allocation-vcores";
    public static final String YARN_QUEUE_FULL_NAME_REGEX = YARN_QUEUE_CONF_PREFIX.concat("([\\S]+)").concat("\\").concat(YARN_QUEUE_CONF_CAPACITY);
    public static final String YARN_SCHEDULER_MINIMUM_ALLOCATION_VCORES ="yarn.scheduler.minimum-allocation-vcores";
    public static final String YARN_SCHEDULER_MINIMUM_ALLOCATION_MB ="yarn.scheduler.minimum-allocation-mb";
    
    public static final List<String> YARN_QUEUE_STATE_LIST = CollUtil.newArrayList("RUNNING","STOPPED");

    public static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";

    public static final String HDFS_RECYCLE_DIR = "/user/admin/.Trash";

    /**
     * ranger 服务策略后缀
     */
    public static final String RANGER_SERVER_TYPE_SUFFIX_HDFS="hadoop";
    public static final String RANGER_SERVER_TYPE_SUFFIX_HBASE="hbase";
    public static final String RANGER_SERVER_TYPE_SUFFIX_HIVE="hive";
    public static final String RANGER_SERVER_TYPE_SUFFIX_YARN="yarn";
    public static final String RANGER_SERVER_TYPE_SUFFIX_KAFKA="kafka";
}
