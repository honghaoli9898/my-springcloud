package com.seaboxdata.sdps.extendAnalysis.utils;

import com.alibaba.fastjson.JSON;
import com.seaboxdata.sdps.extendAnalysis.entity.HdfsDirPathInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HdfsUtils {
    public static final Long DEFAULT_MERGE_THRESHOLD = 67108864L;

    /**
     * 加载Hdfs配置对象
     * @param confJsonApi
     * @return
     */
    public static Configuration loadConfFromApi(String confJsonApi) {
        Configuration conf = new Configuration();
        try {
            HashMap confMap = getMapFromApi(confJsonApi);
            for (Object obj : confMap.entrySet()) {
                conf.set(String.valueOf(((Map.Entry) obj).getKey()), String.valueOf(((Map.Entry) obj).getValue()));
            }
//            conf.addResource(new FileInputStream(new File("/root/deploy/seabox-proxy/resources/config24" + File.separator + "core-site.xml")));
//            conf.addResource(new FileInputStream(new File("/root/deploy/seabox-proxy/resources/config24" + File.separator + "hdfs-site.xml")));
        } catch (Exception e) {
            log.error("加载集群配置异常:", e);
            System.exit(1);
        }
        return conf;
    }

    /**
     * Api获取配置Map对象
     * @param confJsonApi
     * @return
     */
    public static HashMap getMapFromApi(String confJsonApi) {
        HashMap confMap = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            URIBuilder uriBuilder = new URIBuilder(confJsonApi);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse response = null;

            //使用HttpClient发起请求，获取response
            response = httpClient.execute(httpGet);
            //解析响应
            if (response.getStatusLine().getStatusCode() == 200) {
                String confJson = EntityUtils.toString(response.getEntity(), "utf8");
                confMap = JSON.parseObject(confJson, HashMap.class);
            }
        } catch (Exception e) {
            log.error("Api获取配置Map对象异常:", e);
            System.exit(1);
        }
        return confMap;
    }

    /**
     * 获取Hdfs目录和小文件信息
     * @param fs
     * @param sourcePath
     * @param recursive
     * @return
     */
    public static HdfsDirPathInfo getHdfsDirAndSmallFileInfo(FileSystem fs, String sourcePath, boolean recursive){
        HdfsDirPathInfo dirPathInfo = new HdfsDirPathInfo(sourcePath);
        try {
            RemoteIterator<LocatedFileStatus> fileIterator = fs.listFiles(new Path(sourcePath), recursive);
            while(fileIterator.hasNext()) {
                LocatedFileStatus file = fileIterator.next();
                dirPathInfo.addFile(file);
                if(file.getLen() < DEFAULT_MERGE_THRESHOLD){
                    dirPathInfo.addSmallFile(file);
                }
            }

        } catch (IOException e) {
            log.error("获取Hdfs目录信息异常:",e);
            System.exit(1);
        }
        return dirPathInfo;
    }
}
