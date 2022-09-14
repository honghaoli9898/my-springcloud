package com.seaboxdata.sdps.extendAnalysis.test;

import com.seaboxdata.sdps.common.framework.bean.analysis.DirFileInfo;
import com.seaboxdata.sdps.extendAnalysis.utils.HdfsUtils;
import io.leopard.javahost.JavaHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.SparkFiles;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Function1;
import scala.runtime.BoxedUnit;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.*;

@Slf4j
public class TestSpark {
    public static void main(String[] args) throws IOException {

        JavaHost.updateVirtualDns("master", "10.1.3.24");
        JavaHost.updateVirtualDns("slave1", "10.1.3.25");
        JavaHost.updateVirtualDns("slave2", "10.1.3.26");
        //打印虚拟host
        JavaHost.printAllVirtualDns();

        String clusterConfApi = "http://10.1.3.24:9101/seabox/getServerConfByConfName";
        //加载集群配置(hdfs)
        Configuration hadoopConf = HdfsUtils.loadConfFromApi(clusterConfApi.concat("?")
                .concat("clusterId=").concat("1").concat("&")
                .concat("confStrs=core-site,hdfs-site").concat("&")
                .concat("serverName=HDFS")
        );

        SparkConf sparkConf = new SparkConf();
        for (Map.Entry<String, String> entry : hadoopConf) {
            System.out.println(entry.getKey().concat("====").concat(entry.getValue()));
            sparkConf.set(entry.getKey(), entry.getValue());
        }

        SparkSession sparkSession = SparkSession.builder()
                .appName("testSpark")
                .master("local[*]")
                .config(sparkConf)
                .getOrCreate();

        Map confHdfsHashMap = HdfsUtils.getMapFromApi(clusterConfApi.concat("?")
                .concat("clusterId=").concat("1").concat("&")
                .concat("confStrs=core-site,hdfs-site").concat("&")
                .concat("serverName=HDFS")
        );


        //设置广播变量
        Map<String, Object> mapValues = new HashMap();
        mapValues.put("fsConfMap",confHdfsHashMap);
        //设置广播变量
        Broadcast<Map> bcMap = JavaSparkContext.fromSparkContext(sparkSession.sparkContext()).broadcast(mapValues);

        String defaultFS = hadoopConf.get("fs.defaultFS");//hdfs://MyHdfsHA
        System.out.println(defaultFS);
        String path1 = defaultFS.concat("/testData/a.txt");
//        Dataset<Row> rowDataset = sparkSession.read().text(s1);
//        rowDataset.printSchema();
        JavaRDD<String> rdd = sparkSession.sparkContext().textFile(path1, 1).toJavaRDD();



        rdd.foreachPartition((iterator)->{
            printStr(iterator,bcMap);
        });

    }

    public static void printStr(Iterator<String> iterator,Broadcast<Map> bcMap) throws IOException {
        HashMap fsConfMap = (HashMap) ((Map) bcMap.getValue()).get("fsConfMap");
        Configuration conf = new Configuration();
        for (Object obj : fsConfMap.entrySet()) {
            conf.set(String.valueOf(((Map.Entry) obj).getKey()), String.valueOf(((Map.Entry) obj).getValue()));
        }
        FileSystem fs = FileSystem.get(conf);
        String defaultFS = fs.getConf().get("fs.defaultFS");
        System.out.println("========"+defaultFS);

        Path hdfsDowloadpath = new Path(defaultFS.concat("/data/SDP7.1/sdps/sdp7.1/keytab/1.hbase.headless.keytab"));
        Path localPath = new Path("/home/spark/test");
        File file = new File("/home/spark/test");
        if(!file.exists()){
            file.mkdir();
        }

        fs.copyToLocalFile(false,hdfsDowloadpath,localPath,true);

        while (iterator.hasNext()){
            String str = iterator.next();
            System.out.println(str);
        }
    }
}
