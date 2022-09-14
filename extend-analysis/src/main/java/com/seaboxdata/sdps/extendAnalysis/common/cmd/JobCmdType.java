package com.seaboxdata.sdps.extendAnalysis.common.cmd;

public enum JobCmdType {

    CLUSTER("clusterName", "cluster-Name", true, false, "the cluster name"),
    CLUSTER_ID("clusterId", "cluster-id", true, false, "the cluster id"),
    CLUSTER_TYPE("clusterType", "cluster-type", true, false, "the cluster type to execute, as seabox"),
    CLUSTER_IP_HOST("clusterIpHost", "cluster-ip-host", true, false, "the cluster host mapping"),
    JDBC_URL("jdbc", "jdbcUrl", true, false, "the cluster id to execute"),
    HDFS_FSIMAGE_DAY_TIME("dayTime", "fsimage-dayTime", true, false, "The value to the job's data day time"),
    HDFS_FSIMAGE_FILE_PATH("imageFilePath", "fsimage-filePath", true, false, "The path to the job's fsimage decode file"),
    HDFS_FSIMAGE_FILE_TYPE("imageFileType", "fsimage-fileType", true, false, "The file type of fsimage file, such as csv"),
    HDFS_FSIMAGE_FILE_SEPARATOR("imageSeparator", "fsimage-separator", true, false, "The separator type of fsimage file, such as ,"),
    HDFS_FSIMAGE_FILE_HEADER("header", "fsimage-header", true, false, "The fsimage file has header, such as true: has header"),
    HDFS_FSIMAGE_HBASE_QUORUM("zkQuorum", "fsimage-zkQuorum", true, false, "The hbase of zk quorum"),
    HDFS_FSIMAGE_HBASE_PORT("zkPort", "fsimage-zkPort", true, false, "The hbase of zk port"),
    HDFS_FSIMAGE_HBASE_ZNODE("znode", "fsimage-znode", true, false, "The hbase of zk znode"),
    CLUSTER_CONF_API("clusterConfApi", "cluster-conf", true, false, "The cluster conf api"),
    DOWNLOAD_KRB5_API("downloadKrb5Api", "download-krb5-conf", true, false, "download krb5 file api"),
    PROJECT_RELATION_URL("projectRelationUrl", "project-relation-url", true, false, "The project db table relation url"),
    PROJECT_USER_ID("projectUserId", "project-user-id", true, false, "The project admin user id"),
    PROJECT_USER_NAME("projectUserName", "project-user-name", true, false, "The project admin user name"),
    HDFS_MERGE_DIR_PATH("sourcePath", "hdfs-merge-source-path", true, false, "The dir path to the job's to merge file"),
    HDFS_MERGE_FILE_CODEC("fileCodec", "hdfs-merge-fileCodec", true, false, "The file codec of the job's to merge file, such as LZO、GZIP"),
    HDFS_MERGE_FILE_FORMAT("fileFormat", "hdfs-merge-fileFormat", true, false, "The file format of the job's to merge file, such as ORC、AVRO"),
    HDFS_MERGE_REAL_USER("mergeRealUser", "hdfs-merge-user", true, false, "merge file real user"),
    HDFS_MERGE_REAL_GROUP("mergeRealGroup", "hdfs-merge-group", true, false, "merge file real group"),
    SDPS_JOB_ID("sdpsJobId", "sdps-job-id", true, false, "sdps map task job id"),
    SDPS_MERGE_SUBMIT_ID("sdpsMergeSubmitId", "sdps-merge-submit-id", true, false, "sdps map merge submit id"),
    SDPS_MERGE_DATA_ID_PATH_JSON("sdpsMergeDataIdPathJson", "sdps-merge-data-id-path-json", true, false, "sdps map merge data info id and path json");
    /**
     * 参数简短表示
     */
    private String opt;
    /**
     * 参数长类型表示
     */
    private String longOpt;
    /**
     * 指定是否接受参数
     */
    private boolean hasArg;
    /**
     * 是否必传
     */
    private boolean required;
    /**
     * 参数描述
     */
    private String des;


    JobCmdType(String opt, String longOpt, boolean hasArg, boolean required, String des) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.hasArg = hasArg;
        this.required = required;
        this.des = des;
    }

    public String getOpt() {
        return opt;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public boolean getHasArg() {
        return hasArg;
    }

    public boolean getRequired() {
        return required;
    }

    public String getDes() {
        return des;
    }
}
