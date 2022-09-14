package com.seaboxdata.sdps.common.core.constant;

public class ClusterConstants {
    /**
     * 集群类型
     */
    public static final String CLUSTER_TYPE_SEABOX = "seabox";
    public static final String CLUSTER_TYPE_TBDS = "tbds";
    public static final String CLUSTER_TYPE_FUSIONINSIGHT = "fusionInsight";

    public static final String REMOTE_JOIN = "远程接入";
    public static final String PLATFORM_NEW = "平台创建";

    /** 集群加密盐 */
    public static final String CLUSTER_SALT = "_CLUSTER_SALT";
    /**
     * 集群类型ID映射关系
     */
    public static final String CLUSTER_TYPE_ID_SEABOX = "1";
    public static final String CLUSTER_TYPE_ID_TBDS = "2";
    public static final String CLUSTER_TYPE_ID_FUSIONINSIGHT = "3";
    public static final String OWN_FILE_PATH = "hdfs:///user/";
    public static final String PROJECT_FILE_PREFIX = "hdfs:///project/";

    public static final String MASTER = "master";
    public static final String SLAVE1 = "slave1";
    public static final String SLAVE2 = "slave2";
}
