package com.seaboxdata.sdps.extendAnalysis.common;

public class Constants {

    public static final String SDPS_JOB_ID = "sdps.job.id";
    public static final String SDPS_MERGE_SUBMIT_ID = "sdps.merge.submit.id";
    public static final String SDPS_MERGE_DATA_ID_PATH_JSON = "sdps.merge.data.id.path.json";
    public static final String CLUSTER_NAME = "seabox.job.cluster.name";
    public static final String CLUSTER_ID = "seabox.job.cluster.id";
    public static final String CLUSTER_TYPE = "seabox.job.cluster.type";
    public static final String CLUSTER_IP_HOST = "seabox.cluster.ipHost";
    public static final String JDBC_URL = "seabox.job.jdbc.url";
    public static final String HDFS_FSIMAGE_DAY_TIME = "seabox.job.file.dayTime";
    public static final String HDFS_FSIMAGE_FILE_PATH = "seabox.spark.file.path";
    public static final String HDFS_FSIMAGE_FILE_TYPE = "seabox.spark.file.type";
    public static final String HDFS_FSIMAGE_FILE_SEPARATOR = "seabox.spark.file.separator";
    public static final String HDFS_FSIMAGE_FILE_HEADER = "seabox.spark.file.header";
    public static final String HDFS_FSIMAGE_HBASE_QUORUM = "seabox.hbase.zookeeper.quorum";
    public static final String HDFS_FSIMAGE_HBASE_PORT = "seabox.hbase.zookeeper.port";
    public static final String HDFS_FSIMAGE_HBASE_ZNODE = "seabox.hbase.zookeeper.znode";
    public static final String CLUSTER_CONF_API = "seabox.cluster.conf";
    public static final String DOWNLOAD_KRB5_API = "seabox.download.api";
    public static final String PROJECT_RELATION_URL = "project.relation.url";
    /**
     * phoenix jdbc 前缀
     */
    public static final String PHOENIX_URL_PREFIX = "jdbc:phoenix:";

    /**
     * 小文件大小:64MB
     */
    public static final Long SMALL_FILE_BYTES = 67108864L;

    public static final String PHOENIX_BROADCAST_KEY = "phoenixUrl";

    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    public static final String HTTP_HEADER_SDPS_USER_ID_KEY = "x-userid-header";
    public static final String HTTP_HEADER_SDPS_USER_NAME_KEY = "x-user-header";
    /**
     * 查询SDPS中项目列表API的URL
     */
    public static final String SDPS_PROJECT_LIST_API = "/MC10/selectItemAllList";
    /**
     * 查询SDPS中数据库列表API的URL
     */
    public static final String SDPS_DB_LIST_API = "/MC20/selectDatabaseList";
    /**
     * 查询SDPS中数据表列表API的URL
     */
    public static final String SDPS_TABLE_LIST_API = "/MC40/selectTableList";

    /**
     * hdfs合并文件目录
     */
    public static final String HDFS_MERGE_DIR_PATH = "hdfs.merge.dir.path";
    /**
     * 作业要合并文件的文件压缩格式，如LZO、GZIP
     */
    public static final String HDFS_MERGE_FILE_CODEC = "hdfs.merge.file.codec";
    /**
     * 作业合并文件的文件格式，如ORC、AVRO
     */
    public static final String HDFS_MERGE_FILE_FORMAT = "hdfs.merge.file.format";

    /**
     * 合并文件的临时目录
     */
    public static final String MERGE_TEMP_PATH = "/tmp/spark/merge";
    /**
     * 合并文件真实的用户
     */
    public static final String HDFS_MERGE_REAL_USER = "merge.file.real.user";
    /**
     * 合并文件真实的用户组
     */
    public static final String HDFS_MERGE_REAL_GROUP = "merge.file.real.group";

    public static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
}
