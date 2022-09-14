package com.seaboxdata.sdps.seaboxProxy.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.image.SeaboxImageTextWriter;
import com.seaboxdata.sdps.seaboxProxy.service.MetaDataExtract;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Component
@Slf4j
public class TestHdfsUtil {

    @Autowired
    BigdataVirtualHost bigdataVirtualHost;
    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

//    @Value("${metaData.hdfs.fsimageAndExtractTempPath}")
//    String metaDataHdfsFsimageAndExtractTempPath;
//
//    @Value("${metaData.hdfs.tempPath}")
//    String metaDataHdfsTempPath;

    @Autowired
    MetaDataExtract metaDataExtract;

    @Test
    public void selectHdfsPathInfo() throws IOException {
        bigdataVirtualHost.setVirtualHost(2);
        //System.setProperty("HADOOP_USER_NAME", "hdfs");

//        FileSystem fs = new HdfsUtil(2).getFs();
//        FileStatus[] fileStatuses = fs.listStatus(new Path("/testData"));
//        for (FileStatus fileStatus : fileStatuses) {
//            if (fileStatus.isDirectory()) {
//                String dirName = fileStatus.getPath().getName();
//                System.out.println(dirName);
//            }
//        }

        FileSystem fs = new HdfsUtil(2).getFs();
        FileStatus fileStatus = fs.getFileStatus(new Path("/hdp/apps/3.1.5.0-152/yarn/service-dep.tar.gz"));
        System.out.println(fileStatus.getPermission().toString());
        BlockStoragePolicySpi storagePolicy = fs.getStoragePolicy(new Path("/hdp/apps/3.1.5.0-152/yarn/service-dep.tar.gz"));
        System.out.println(storagePolicy.getStorageTypes());
//        FileStatus[] fileStatuses = fs.listStatus(new Path("/"));
//        for (FileStatus fileStatus : fileStatuses) {
//            if (fileStatus.isDirectory()) {
//                String dirName = fileStatus.getPath().getName();
//                String owner = fileStatus.getOwner();
//                String group = fileStatus.getGroup();
//                System.out.println("==>" + fileStatus.getPath().toString());
//                ContentSummary summary = fs.getContentSummary(fileStatus.getPath());
//                long quota = summary.getQuota();
//                long spaceConsumed = summary.getSpaceConsumed();
//                long spaceQuota = summary.getSpaceQuota();
//
//                System.out.println("dirName===>" + dirName);
//                System.out.println("owner===>" + owner);
//                System.out.println("group===>" + group);
//                System.out.println("quota===>" + Long.valueOf(quota));
//                System.out.println("spaceConsumed===>" + Long.valueOf(spaceConsumed));
//                System.out.println("spaceQuota===>" + Long.valueOf(spaceQuota));
//                System.out.println("==================================================");
//
////                HdfsSaveObj hdfsSaveObj = HdfsSaveObj.builder()
////                        .dirName(dirName)
////                        .owner(owner)
////                        .group(group)
////                        .quotaNum(quota)
////                        .spaceConsumed(spaceConsumed)
////                        .spaceQuotaNum(spaceQuota)
////                        .build();
////                System.out.println("=====hdfsSaveObj========>" + hdfsSaveObj.toString());
//            }


//        ArrayList<HdfsSaveObj> dirInfo = new HdfsUtil(2).getDirOwnerGroupUsedQuotaNumSpaceQuotaNum("/testData");
//        String s = JSON.toJSONString(dirInfo);
//        System.out.println(s);

    }

    @Test
    public void fun01() {
        bigdataVirtualHost.setVirtualHost(2);
        //System.setProperty("HADOOP_USER_NAME", "hdfs");
        HdfsUtil hdfsUtil = new HdfsUtil(2);
        boolean b = hdfsUtil.mkdirSetQNAndSQNAndOwner("/testData/120",
                0, 0, null, null);
        System.out.println(b);
    }

    @Test
    public void fun02() throws IOException {
        bigdataVirtualHost.setVirtualHost(2);
        //System.setProperty("HADOOP_USER_NAME", "hdfs");
        HdfsUtil hdfsUtil = new HdfsUtil(2);
        FileSystem fs = hdfsUtil.getFs();
        fs.setOwner(new Path("/testData/131"), null, "a");
        fs.close();
    }

    @Test
    public void testHdfsfun() throws IOException {
        bigdataVirtualHost.setVirtualHost(2);
        //System.setProperty("HADOOP_USER_NAME", "hdfs");
        HdfsUtil hdfsUtil = new HdfsUtil(2);
        FileSystem fs = hdfsUtil.getFs();
        String pathStr = "/testData111";
        boolean bool = fs.exists(new Path(pathStr));
        if(bool){
            FileStatus[] listStatus = fs.listStatus(new Path(pathStr));
            System.out.println(listStatus.length);
        }else {
            System.out.println(pathStr+"不存在");
        }
        fs.close();
    }

    @Test
    public void imageData() throws Exception {
        bigdataVirtualHost.setVirtualHost(1);
        //System.setProperty("HADOOP_USER_NAME", "hdfs");
//        File file = new File("/action/sourceImage/fsimage_20211124-3");
        File file = new File("c:/data/fsimage_20220419");
        HdfsUtil hdfsUtil = new HdfsUtil(1);
        boolean b = hdfsUtil.fetchHdfsImage(file);
        hdfsUtil.closeFs();
        System.out.println(b);

    }

    @Test
    public void imageDataExtract() {

        String inputFile = "/action/sourceImage/fsimage_20211124-1";
        String outputFile = "/action/sourceImage/extract_20211124-2";
        String delimiter = ":";
        String tempPath = "/action/sourceImage/tmp";
        String inflate = "false";

        PrintStream out = null;
        RandomAccessFile accessFile = null;
        SeaboxImageTextWriter writer = null;
        try {
            out = new PrintStream(outputFile, "UTF-8");
            writer = new SeaboxImageTextWriter(out, delimiter, tempPath, Boolean.parseBoolean(inflate));
            accessFile = new RandomAccessFile(inputFile, "r");
            writer.visit(accessFile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (accessFile != null) {
                    accessFile.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testFetchExtractHdfsMetaData() {
        boolean bool = metaDataExtract.fetchAndExtractHdfsMetaData(1);
        System.out.println(bool);
    }

    @Test
    public void testAmbariConf() {

//        String confStr = new AmbariUtil(1).getAmbariServerConfByConfName("core-site");
//        System.out.println("=========="+confStr);
//        if(StringUtils.isNotBlank(confStr)){
//            Map map = JSON.parseObject(confStr, Map.class);
//            for (Object m : map.entrySet()){
//                System.out.println(((Map.Entry)m).getKey()+"===>" + ((Map.Entry)m).getValue());
//            }
//        }
    }

    @Test
    public void aaa() {
        String test = "{\n" +
                "    \"configurations\": [{\n" +
                "            \"node.properties\": {\n" +
                "                \"properties_attributes\": {},\n" +
                "                \"properties\": {\n" +
                "                    \"plugin.config-dir\": \"/opt/bigdata/presto/presto-server/catalog\",\n" +
                "                    \"node.environment\": \"production\",\n" +
                "                    \"plugin.dir\": \"/opt/bigdata/presto/presto-server/plugin\"\n" +
                "                }\n" +
                "            }\n" +
                "        }, {\n" +
                "            \"zoo.cfg\": {\n" +
                "                \"properties_attributes\": {},\n" +
                "                \"properties\": {\n" +
                "                    \"quorum.cnxn.threads.size\": \"20\",\n" +
                "                    \"autopurge.purgeInterval\": \"24\",\n" +
                "                    \"dataDir\": \"/hadoop/zookeeper\",\n" +
                "                    \"autopurge.snapRetainCount\": \"30\",\n" +
                "                    \"clientPort\": \"2181\",\n" +
                "                    \"initLimit\": \"10\",\n" +
                "                    \"tickTime\": \"3000\",\n" +
                "                    \"syncLimit\": \"5\",\n" +
                "                    \"quorum.auth.enableSasl\": \"false\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        String configurations = JSONObject.parseObject(test).get("configurations").toString();


        List<Map> maps = JSON.parseArray(configurations, Map.class);
        for (Map map : maps) {
            for (Object m : map.entrySet()) {
//                System.out.println(((Map.Entry)m).getKey()+"     " + ((Map.Entry)m).getValue());
                if (((Map.Entry) m).getKey().equals("zoo.cfg")) {

                }
            }
        }
    }

    @Test
    public void testJson() {
        String str = "{\"fs.defaultFS\":\"hdfs://MyHdfsHA\",\"fs.s3a.multipart.size\":\"67108864\",\"hadoop.http.cross-origin.allowed-methods\":\"GET,PUT,POST,OPTIONS,HEAD,DELETE\",\"ipc.server.tcpnodelay\":\"true\",\"fs.gs.working.dir\":\"/\",\"hadoop.http.cross-origin.allowed-origins\":\"*\",\"mapreduce.jobtracker.webinterface.trusted\":\"false\",\"fs.gs.path.encoding\":\"uri-path\",\"hadoop.http.cross-origin.max-age\":\"1800\",\"hadoop.proxyuser.root.groups\":\"*\",\"ipc.client.idlethreshold\":\"8000\",\"hadoop.proxyuser.hdfs.groups\":\"*\",\"fs.s3a.fast.upload\":\"true\",\"fs.trash.interval\":\"360\",\"hadoop.http.authentication.simple.anonymous.allowed\":\"true\",\"hadoop.security.authorization\":\"false\",\"ipc.client.connection.maxidletime\":\"30000\",\"fs.gs.application.name.suffix\":\" (GPN:Hortonworks; version 1.0) HDP/{{version}}\",\"hadoop.proxyuser.hcat.groups\":\"*\",\"hadoop.proxyuser.livy.groups\":\"*\",\"hadoop.proxyuser.hive.hosts\":\"%HOSTGROUP::host_group_1%,%HOSTGROUP::host_group_2%\",\"hadoop.proxyuser.root.hosts\":\"master\",\"ha.failover-controller.active-standby-elector.zk.op.retries\":\"120\",\"hadoop.http.cross-origin.allowed-headers\":\"X-Requested-With,Content-Type,Accept,Origin,WWW-Authenticate,Accept-Encoding,Transfer-Encoding\",\"hadoop.security.authentication\":\"simple\",\"fs.s3a.fast.upload.buffer\":\"disk\",\"hadoop.proxyuser.hdfs.hosts\":\"*\",\"fs.azure.user.agent.prefix\":\"User-Agent: APN/1.0 Hortonworks/1.0 HDP/{{version}}\",\"hadoop.security.instrumentation.requires.admin\":\"false\",\"hadoop.proxyuser.hue.hosts\":\"*\",\"ipc.client.connect.max.retries\":\"50\",\"io.file.buffer.size\":\"131072\",\"hadoop.proxyuser.livy.hosts\":\"*\",\"fs.s3a.user.agent.prefix\":\"User-Agent: APN/1.0 Hortonworks/1.0 HDP/{{version}}\",\"hadoop.proxyuser.hcat.hosts\":\"*\",\"hadoop.proxyuser.hue.groups\":\"*\",\"net.topology.script.file.name\":\"/etc/hadoop/conf/topology_script.py\",\"io.compression.codecs\":\"org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.SnappyCodec\",\"hadoop.proxyuser.hive.groups\":\"*\",\"ha.zookeeper.quorum\":\"%HOSTGROUP::host_group_1%:2181,%HOSTGROUP::host_group_2%:2181,%HOSTGROUP::host_group_3%:2181\",\"hadoop.http.filter.initializers\":\"org.apache.hadoop.security.AuthenticationFilterInitializer,org.apache.hadoop.security.HttpCrossOriginFilterInitializer\",\"io.serializations\":\"org.apache.hadoop.io.serializer.WritableSerialization\"}";
        Map map = JSON.parseObject(str, Map.class);
        for (Object m : map.entrySet()) {
            System.out.println(((Map.Entry) m).getKey() + "===>" + ((Map.Entry) m).getValue());
        }

    }

    @Test
    public void testMkdir() throws IOException, InterruptedException {
        Configuration hdfsConf = new Configuration();

        String userName = "bpz1";

        // 加载集群配置
        ArrayList<String> confList = new ArrayList<>();
        confList.add("core-site");
        confList.add("hdfs-site");
        String confJson = new AmbariUtil(1)
                .getAmbariServerConfByConfName("HDFS", confList);
        Map confMap = JSON.parseObject(confJson, Map.class);
        for (Object obj : confMap.entrySet()) {
            hdfsConf.set(String.valueOf(((Map.Entry) obj).getKey()),
                    String.valueOf(((Map.Entry) obj).getValue()));
        }
        FileSystem fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf,userName);
        HdfsAdmin hdfsAdmin = new HdfsAdmin(FileSystem.getDefaultUri(hdfsConf),
                hdfsConf);
        DFSAdmin dfsAdmin = new DFSAdmin(hdfsConf);


        String pathStr = "/testData/bpz1Dir";

//        boolean bool = fs.mkdirs(new Path(pathStr));
//        System.out.println(bool);

        Trash trash = new Trash(fs,fs.getConf());

        boolean b = trash.moveToTrash(new Path(pathStr));
        System.out.println(b);
    }

    public FileSystem changeUser(FileSystem fs,Configuration hdfsConf,String userName) throws IOException, InterruptedException {
        return fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf,userName);
    }
}
