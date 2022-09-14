package com.seaboxdata.sdps.common.framework.bean.yarn;

/**
 * @author: Denny
 * @date: 2021/10/27 16:05
 * @desc: yarn队列参数常量类
 */
public class YarnConstant {

    /**
     * ******************************************************************************************
     * capacity策略 队列 公共参数
     * ******************************************************************************************
     */
    public static final String QUEUE_SUFFIX_COMMON_MAXIMUM_AM_RESOURCE_PERCENT = "maximum-am-resource-percent";
    public static final Double QUEUE_SUFFIX_COMMON_MAXIMUM_AM_RESOURCE_PERCENT_VALUE = 0.5;
    public static final String QUEUE_SUFFIX_COMMON_MAXIMUM_APPLICATIONS = "maximum-applications";
    public static final Integer QUEUE_SUFFIX_COMMON_MAXIMUM_APPLICATIONS_VALUE = 10000;
    public static final String QUEUE_SUFFIX_COMMON_NODE_LOCALITY_DELAY = "node-locality-delay";
    public static final Integer QUEUE_SUFFIX_COMMON_NODE_LOCALITY_DELAY_VALUE = 40;
    public static final String QUEUE_SUFFIX_COMMON_RESOURCE_CALCULATOR = "resource-calculator";
    public static final String QUEUE_SUFFIX_COMMON_RESOURCE_CALCULATOR_VALUE = "org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator";
    public static final String QUEUE_SUFFIX_COMMON_QUEUE_MAPPINGS_OVERRIDE_ENABLE = "queue-mappings-override.enable";
    public static final Boolean QUEUE_SUFFIX_COMMON_QUEUE_MAPPINGS_OVERRIDE_ENABLE_VALUE = false;


    /**
     * ******************************************************************************************
     * capacity策略 队列 参数名
     * ******************************************************************************************
     */
    public static final String QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT = "yarn.scheduler.capacity.";

    public static final String QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE = ".acl_administer_queue";
    public static final String QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS = ".acl_submit_applications";
    public static final String QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT = ".minimum-user-limit-percent";
    public static final String QUEUE_SUFFIX_MAXIMUM_CAPACITY = ".maximum-capacity";
    public static final String QUEUE_SUFFIX_USER_LIMIT_FACTOR = ".user-limit-factor";
    public static final String QUEUE_SUFFIX_STATE = ".state";
    public static final String QUEUE_SUFFIX_CAPACITY = ".capacity";
    public static final String QUEUE_SUFFIX_ORDERING_POLICY = ".ordering-policy";
    public static final String QUEUE_SUFFIX_PRIORITY = ".priority";
    public static final String QUEUE_SUFFIX_ACCESSIBLE_NODE_LABELS = ".accessible-node-labels";
    public static final String QUEUE_SUFFIX_QUEUES = ".queues";

    public static final String COMMON_VALUE_ACL = "*";
    public static final Integer COMMON_VALUE_ROOT_CAPACITY = 100;
    public static final Integer COMMON_VALUE_PRIORITY = 0;
    public static final Integer COMMON_VALUE_USER_LIMIT_FACTOR = 1;
    public static final Integer COMMON_VALUE_MINIMUM_USER_LIMIT_PERCENT = 100;
    public static final String COMMON_VALUE_ORDERING_POLICY = "fifo";


    /**
     * 集群默认参数
     */
    public static final String TAG = "tag";
    public static final String TAG_VERSION = "version";
    public static final String SCHEDULER_TYPE = "type";
    public static final String PROPERTIES = "properties";
    public static final String SCHEDULER_TYPE_CAPACITY = "capacity-scheduler";
    public static final String SERVICE_CONFIG_VERSION_NOTE = "service_config_version_note";
    public static final String SERVICE_CONFIG_VERSION_NOTE_VALUE = "";

    /**
     * 集群默认队列
     */
    public static final String QUEUE_ROOT = "root";
    public static final String QUEUE_ROOT_DEFAULT = "root.default";
}
