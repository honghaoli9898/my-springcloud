package com.seaboxdata.sdps.common.core.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需要同步keytab文件的服务
 */
public enum KeytabServerEnum {
    HDFS("hdfs.headless.keytab"),
    HIVE("hive.service.keytab"),
    HBASE("hbase.service.keytab"),
    SPARK2("spark.headless.keytab"),
//    KAFKA("kafka.service.keytab"),
//    ZOOKEEPER("zk.service.keytab"),
    PHOENIX("spnego.service.keytab");
//    YARN("yarn.service.keytab");

    private String keytabName;

    public String getKeytabName() {
        return keytabName;
    }

    static Map<String, KeytabServerEnum> search = new HashMap<String, KeytabServerEnum>() {
        {
            put(HDFS.keytabName, HDFS);
            put(HIVE.keytabName, HIVE);
            put(HBASE.keytabName, HBASE);
            put(SPARK2.keytabName, SPARK2);
//            put(KAFKA.keytabName, KAFKA);
//            put(YARN.keytabName, YARN);
            put(PHOENIX.keytabName, PHOENIX);
//            put(ZOOKEEPER.keytabName, ZOOKEEPER);
        }
    };

    KeytabServerEnum(String keytabName) {
        this.keytabName = keytabName;
    }

    public static List<String> getKeytabServerList() {
        List<String> list = new ArrayList<>();
        list.add(HBASE.keytabName);
        list.add(HDFS.keytabName);
        list.add(HIVE.keytabName);
        list.add(SPARK2.keytabName);
//        list.add(KAFKA.keytabName);
//        list.add(YARN.keytabName);
        list.add(PHOENIX.keytabName);
        return list;
    }

    public static String getServiceNameByKeytabName(String keytabName) {
        return search.get(keytabName).name();
    }
}
