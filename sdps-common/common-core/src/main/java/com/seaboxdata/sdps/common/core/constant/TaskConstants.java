package com.seaboxdata.sdps.common.core.constant;

public class TaskConstants {

    public static final String TASK_TYPE_SPARK = "SPARK";
    /**
     * HDFS分析元数据任务名称
     */
    public static final String TASK_TYPE_HDFS_METADATA_ANALYSIS = "HdfsMetaDataAnalysis";
    /**
     * spark任务参数
     */
    public static final String SPARK_TASK_KEY_SUBMIT_PATH = "submit-path";
    public static final String SPARK_SUBMIT_PARAM_PREFIX = "--";
    public static final String TASK_KEY_NAME = "name";
    public static final String SPARK_TASK_KEY_CLASS = "class";
    public static final String SPARK_TASK_KEY_MASTER = "master";
    public static final String SPARK_TASK_KEY_QUEUE = "queue";
    public static final String SPARK_TASK_KEY_DEPLOY_MODE = "deploy-mode";
    public static final String SPARK_TASK_KEY_DRIVER_MEMORY = "driver-memory";
    public static final String SPARK_TASK_KEY_NUM_EXECUTORS = "num-executors";
    public static final String SPARK_TASK_KEY_EXECUTOR_MEMORY = "executor-memory";
    public static final String SPARK_TASK_KEY_EXECUTOR_CORES = "executor-cores";
    public static final String SPARK_TASK_KEY_JAR_PATH = "jar-path";
    public static final String SPARK_TASK_KEY_CONF = "conf";
    public static final String SPARK_TASK_KEY_FILES = "files";
    public static final String HDFS_ANALYSIS_TASK_KEY_MYSQL_JDBC_URL = "mysql-jdbc-url";
    public static final String HDFS_ANALYSIS_HDFS_IMAGE_FILE_PATH = "hdfs-image-file-path";
    public static final String HDFS_ANALYSIS_EXTRACT_FILE_ISHEADER = "extract-file-isHeader";
    public static final String HDFS_ANALYSIS_TASK_KEY_PROJECT_RELATION_USER_ID = "x-userid-header";
    public static final String HDFS_ANALYSIS_TASK_KEY_PROJECT_RELATION_USER_NAME = "x-user-header";
    public static final String TASK_KEY_TASK_SAVE_PATH = "task-save-path";
    public static final String TASK_KEY_XXL_JOB_MAP_ID ="xxl-job-map-id";
    public static final String SHELL_SCRIPT_PREFIX = "#!/bin/bash";

    /**
     * jar包参数
     */
    public static final String ANALYSIS_TASK_PARAM_CLUSTER_NAME = "clusterName";
    public static final String TASK_PARAM_CLUSTER_ID = "clusterId";
    public static final String ANALYSIS_TASK_PARAM_CLUSTER_TYPE = "clusterType";
    public static final String TASK_PARAM_CLUSTER_IP_HOST = "clusterIpHost";
    public static final String ANALYSIS_TASK_PARAM_JDBC = "jdbc";
    public static final String ANALYSIS_TASK_PARAM_DAYTIME = "dayTime";
    public static final String ANALYSIS_TASK_PARAM_IMAGEFILE_PATH = "imageFilePath";
    public static final String ANALYSIS_TASK_PARAM_IMAGEFILE_TYPE = "imageFileType";
    public static final String ANALYSIS_TASK_PARAM_IMAGE_SEPARATOR = "imageSeparator";
    public static final String ANALYSIS_TASK_PARAM_IS_HEADER = "header";
    public static final String ANALYSIS_TASK_PARAM_ZK_QUORUM = "zkQuorum";
    public static final String ANALYSIS_TASK_PARAM_ZK_PORT = "zkPort";
    public static final String ANALYSIS_TASK_PARAM_ZNODE = "znode";
    public static final String ANALYSIS_TASK_PROJECT_RELATION_URL = "projectRelationUrl";
    public static final String ANALYSIS_TASK_PROJECT_USER_ID = "projectUserId";
    public static final String ANALYSIS_TASK_PROJECT_USER_NAME = "projectUserName";
    public static final String TASK_PARAM_CLUSTER_CONF_API = "clusterConfApi";
    public static final String TASK_PARAM_DOWNLOAD_KRB5_API = "downloadKrb5Api";
    /**
     *
     */
    public static final String HDFS_IMAGE_EXTRACT_FILE_PREFIX = "extract_";
    public static final String HDFS_IMAGE_EXTRACT_FILE_TYPE_CSV = "csv";
    public static final String HDFS_IMAGE_EXTRACT_FILE_SEPARATOR = ":";

    /**
     *
     */
    public static final String ANALYSIS_TASK_USER_SPAKR = "spark";
    public static final String ANALYSIS_TASK_SPAKR_TASK_TYPE = "SparkSSH";


    /**
     * 任务类型:HDFS合并文件
     */
    public static final String TASK_TYPE_HDFS_MERGE_FILE = "HdfsMergeFile";
    /**
     * 合并文件任务参数:合并文件的目录
     */
    public static final String HDFS_MERGE_FILE_TASK_KEY_SOURCE_PATH = "sourcePath";
    /**
     * 合并文件任务参数:文件压缩格式
     */
    public static final String HDFS_MERGE_FILE_TASK_KEY_CODEC = "fileCodec";
    /**
     * 合并文件任务参数:文件类型
     */
    public static final String HDFS_MERGE_FILE_TASK_KEY_FORMAT = "fileFormat";
    /**
     * 合并文件任务参数:合并文件真实用户
     */
    public static final String HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_USER = "mergeRealUser";
    /**
     * 合并文件任务参数:合并文件真实用户组
     */
    public static final String HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_GROUP = "mergeRealGroup";
    
    /**
     * 需要创建sdps任务的标识key
     */
    public static final String IS_SDPS_JOB = "isSdpsJob";

    public static final String IS_CREATE_TASK_INFO = "createTaskInfo";
    
    public static final String IS_UPDATE_LOG_ID = "isUpdateLogId";
    
    /**
     * xxl_job的job id标识key
     */
    public static final String XXL_JOB_LOG_ID = "xxlJobLogId";
    
    public static final String SDPS_JOB_ID = "sdpsJobId";
    
    public static final String CLUSTER_ID_KEY = "clusterId";
    
    public static final String SOURCE_PATH_KEY = "sourcePath";

    public static final String SUBMIT_ID = "submitId";

    /**
     * 合并文件作业类型:HDFS
     */
    public static final String MERGE_FILE_TASK_TYPE_HDFS = "HDFS";
    /**
     * 合并文件作业类型:HIVE
     */
    public static final String MERGE_FILE_TASK_TYPE_HIVE = "HIVE";
    /**
     * 合并文件作业类型:HIVE_
     */
    public static final String MERGE_FILE_TASK_TYPE_HIVE_EXTERNAL = "HIVE_EXTERNAL";
    /**
     * 合并文件数据信息表主键ID
     */
    public static final String SDPS_MERGE_SUBMIT_ID = "sdpsMergeSubmitId";
    /**
     * 合并文件数据信息表主键ID
     */
    public static final String SDPS_MERGE_DATA_ID_PATH_JSON = "sdpsMergeDataIdPathJson";
}
