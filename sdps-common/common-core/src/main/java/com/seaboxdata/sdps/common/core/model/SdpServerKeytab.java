package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sdps_server_keytab")
public class SdpServerKeytab implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 集群id
     */
    private Integer clusterId;
    /**
     * 集群名
     */
    private String clusterName;
    /**
     * 节点
     */
    private String host;
    /**
     * kerberos主体
     */
    private String principalName;
    /**
     * 主体类型：SERVICE/USER
     */
    private String principalType;

    private String localUsername;
    /**
     * keytab路径
     */
    private String keytabFilePath;
    /**
     * keytab文件名
     */
    private String keytabFileName;
    /**
     * keytab文件所属用户
     */
    private String keytabFileOwner;
    /**
     * keytab权限
     */
    private String keytabFileMode;
    /**
     * keytab文件所属用户组
     */
    private String keytabFileGroup;
    /**
     * keytab文件是否已生成
     */
    private Boolean keytabFileInstalled;
    /**
     * keytab所属组件
     */
    private String component;
    /**
     * keytab文件的base64字符串
     */
    private String keytabContent;

    private String description;

    private Date createTime;

    private Date updateTime;

    private static Map<String, String> map = Maps.newHashMap();

    static {
        map.put("ambari_infra_spnego", "ambari_client");
        map.put("ambari_infra_smokeuser", "ambari_client");
        map.put("ambari-server", "ambari-server");
        map.put("ams_monitor", "ams_monitor");
        map.put("datanode_dn", "datanode");
        map.put("hbase", "hbase");
        map.put("hbase_regionserver_hbase", "hbase_regionserver");
        map.put("hdfs", "hdfs");
        map.put("infra-solr", "infra-solr");
        map.put("journalnode_jn", "journalnode");
        map.put("kafka3_broker", "kafka3_broker");
        map.put("logfeeder", "logfeeder");
        map.put("nfsgateway", "nfsgateway");
        map.put("namenode_nn", "namenode");
        map.put("rangeradmin", "rangeradmin");
        map.put("rangerlookup", "ranger_client");
        map.put("resource_manager_rm", "resource_manager");
        map.put("spark2user", "spark2_client");
        map.put("spark_service_keytab", "spark");
        map.put("atlas_kafka", "spark_atlas");
        map.put("yarn_ats_hbase_master", "yarn_client");
        map.put("yarn_ats_hbase_regionserver", "yarn_client");
        map.put("yarn_ats", "yarn_ats");
        map.put("yarn_timeline_reader", "yarn_timeline_reader");
        map.put("zookeeper_zk", "zookeeper");
        map.put("hdfs_hdfs_client_hdfs", "hdfs_client");
        map.put("hive_server_hive", "hive");
        map.put("llap_task_hive", "yarn_client");
        map.put("logsearch_logsearch_server_infra-solr", "infra-solr");
        map.put("history_server_jhs", "mapreduce_history");
        map.put("kafka_broker", "kafka_broker");
        map.put("livyuser", "spark_livy");
        map.put("logsearch", "logsearch");
        map.put("nodemanager_nm", "nodemanager");
        map.put("rangerusersync", "rangerusersync");
        map.put("app_timeline_server_yarn", "yarn_timeline");
        map.put("ams_collector", "ams_collector");
        map.put("ams_hbase_master_hbase", "ams_client");
        map.put("ams_hbase_regionserver_hbase", "ams_client");
        map.put("ams_zookeeper", "ams_client");
        map.put("oozie_server", "oozie_server");
        map.put("rangertagsync", "rangertagsync");
    }

    /**
     * 根据description获取所属组件
     * @param description
     * @return
     */
    public static String convertComponent(String description) {
        return map.get(description);
    }
}
