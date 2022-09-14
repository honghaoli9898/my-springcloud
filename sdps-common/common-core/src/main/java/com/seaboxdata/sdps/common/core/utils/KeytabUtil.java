package com.seaboxdata.sdps.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class KeytabUtil {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 拼接keytab文件名
     * @param clusterId 集群id
     * @param username  用户名
     * @param isServer  是否是服务
     * @return
     */
    public static String getKeytabName(Integer clusterId, String username, Boolean isServer) {
        //如果是服务，则直接返回
        if (isServer) {
            return clusterId + "." + username;
        }
        return clusterId + "-" + username + ".headless.keytab";
    }

    /**
     * 检查keytab文件是否存在
     * @param clusterIdList 集群id集合
     * @param username      用户名
     * @param beanName      restTemplate的bean名称
     */
    public static void checkKeytabExist(List<Integer> clusterIdList, String username, String beanName, Boolean isServer) {
        clusterIdList.forEach(clusterId -> checkKeytabExist(clusterId, getKeytabName(clusterId, username, isServer), beanName, isServer));
    }

    /**
     * 检查keytab文件是否存在
     * @param clusterId    集群id
     * @param beanName     restTemplate的bean名称
     */
    public static void checkKeytabExist(Integer clusterId, String keytabFileName, String beanName, Boolean isServer) {
        String keytabPathVal = SpringUtil.getProperty("security.kerberos.login.keytabPath");

        String keytabPath = keytabPathVal.concat("/") + keytabFileName;

        File file = new File(keytabPath);
        String getKeytabUrl = SpringUtil.getProperty("security.kerberos.login.getKeytabUrl");
        String url = getKeytabUrl + "clusterId=" + clusterId + "&keytabName=" + keytabFileName;
        if (isServer) {
            convertKeytabToFile(url, beanName, keytabPath);
        } else if (!file.exists()) {
            //如果文件不存在，则请求user-sync-center获取keytab
            getFile(file, url, beanName, keytabPath);
        }
    }

    /**
     * 检测krb5文件
     * @param clusterId 集群id
     * @param beanName  restTemplate的bean名称
     */
    public static void checkKrb5Exist(Integer clusterId, String beanName) {
        String krb5Path = SpringUtil.getProperty("security.kerberos.login.krb5");
        File file = new File(krb5Path);
        //如果文件不存在，则请求user-sync-center获取keytab
        if (!file.exists()) {
            String getKrb5Url = SpringUtil.getProperty("security.kerberos.login.getKrb5Url");
            String url = getKrb5Url + "clusterId=" + clusterId;
            getFile(file, url, beanName, krb5Path);
        }
    }

    /**
     * keytab文件写入文件
     * @param file       keytab文件
     * @param url        获取keytab的url
     * @param beanName   restTemplate的bean名称
     * @param keytabPath keytab路径
     */
    private static synchronized void getFile(File file, String url, String beanName, String keytabPath) {
        if (!file.exists()) {
            convertKeytabToFile(url, beanName, keytabPath);
        }
    }

    /**
     * 将base64字符串转为keytab文件
     * @param url        获取keytab base64字符串的接口
     * @param beanName   restTemplate的bean名称
     * @param keytabPath keytab路径
     */
    private static void convertKeytabToFile(String url, String beanName, String keytabPath) {
        RestTemplate restTemplate = SpringUtil.getBean(beanName, RestTemplate.class);
        JSONObject jo = RestTemplateUtil.restGet(restTemplate, url);
        String keytabStr = jo.getString("data");
        convertKeytabToFile(keytabStr, keytabPath);
    }

    /**
     * 将base64字符串转为keytab文件
     * @param keytabStr  base64字符串
     * @param keytabPath keytab路径
     */
    private static void convertKeytabToFile(String keytabStr, String keytabPath) {
        String suffix = sdf.format(new Date());
        //先备份
        FileUtils.fileCopyToTmp(keytabPath, suffix);
        try {
            Base64Util.convertStrToFile(keytabStr, keytabPath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            FileUtils.reverseTmp(keytabPath, suffix);
        }
        //删除备份文件
        FileUtils.deleteFile(keytabPath + "." + suffix);
    }

    /**
     * 用于检查hdfs、hive、hbase等服务的keytab文件进行更新
     * @param keytabList 要更新的keytab文件集合
     * @param beanName   restTemplate的bean名
     * @param isServer   是否是服务的keytab更新
     */
    public static void checkKeytabEffective(List<String> keytabList, List<Integer> clusterIds, String beanName, Boolean isServer) {
        //如果集群列表或者需要同步的keytab文件为空，则不需要同步
        if (CollectionUtils.isEmpty(clusterIds) || CollectionUtils.isEmpty(keytabList)) {
            return;
        }
        for (String keytab : keytabList) {
            //替换keytab文件
            checkKeytabExist(clusterIds, keytab, beanName, isServer);
        }
    }

    /**
     * 更新keytab文件
     * @param list keytab文件集合
     */
    public static void updateKeytabs(List<SdpServerKeytab> list) {
        for (SdpServerKeytab serverKeytab : list) {
            convertKeytabToFile(serverKeytab.getKeytabContent(), serverKeytab.getKeytabFilePath());
        }
    }
}
