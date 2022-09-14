package com.seaboxdata.sdps.seaboxProxy.util;

import com.alibaba.fastjson.JSON;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class HiveUtil {

    private HiveMetaStoreClient client;

    public HiveUtil(Integer clusterId) {
        try {
//            System.setProperty("HADOOP_PROXY_USER", "hive");
            //获取hive配置信息
            ArrayList<String> confList = new ArrayList<>();
            confList.add("hive-site");
            String confJson = new AmbariUtil(clusterId).getAmbariServerConfByConfName("HIVE", confList);
            Map confMap = JSON.parseObject(confJson, Map.class);
            String HiveUri = confMap.get("hive.metastore.uris").toString();
            HiveConf hiveConf = new HiveConf();
            hiveConf.set("hive.metastore.uris", HiveUri);
            client = new HiveMetaStoreClient(hiveConf);
        } catch (Exception e) {
            log.error("获取hive-metastore客户端失败", e);
            throw new BusinessException("获取hive-metastore客户端失败");
        }
    }

    /**
     * 根据库和表名获取表信息
     * @param dbName    库
     * @param tableName 表
     * @return
     */
    public Table getTable(String dbName, String tableName) {
        Table table = null;
        try {
            table = client.getTable(dbName, tableName);
        } catch (Exception e) {
            log.error("获取hive表信息失败", e);
            throw new BusinessException("获取hive表信息失败");
        } finally {
            client.close();
        }
        return table;
    }
}
