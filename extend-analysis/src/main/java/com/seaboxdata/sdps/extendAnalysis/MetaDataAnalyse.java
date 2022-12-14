package com.seaboxdata.sdps.extendAnalysis;

import io.leopard.javahost.JavaHost;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import scala.Tuple2;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.Base64Util;
import com.seaboxdata.sdps.common.framework.bean.analysis.DBTableInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.DirFileInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.TenantInfo;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import com.seaboxdata.sdps.extendAnalysis.common.Constants;
import com.seaboxdata.sdps.extendAnalysis.common.DataTempType;
import com.seaboxdata.sdps.extendAnalysis.common.FileStatsType;
import com.seaboxdata.sdps.extendAnalysis.common.INodeFileType;
import com.seaboxdata.sdps.extendAnalysis.common.cmd.BaseJobCmd;
import com.seaboxdata.sdps.extendAnalysis.entity.ClusterHostConf;
import com.seaboxdata.sdps.extendAnalysis.entity.FileInfo;
import com.seaboxdata.sdps.extendAnalysis.entity.FileStatsInfo;
import com.seaboxdata.sdps.extendAnalysis.entity.ImageFileInfo;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpServerKeytabMapper;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpsTaskInfoMapper;
import com.seaboxdata.sdps.extendAnalysis.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.extendAnalysis.utils.HdfsUtils;
import com.seaboxdata.sdps.extendAnalysis.utils.JDBCUtils;
import com.seaboxdata.sdps.extendAnalysis.utils.RegexUtils;
import com.seaboxdata.sdps.extendAnalysis.utils.tenant.TenantUtils;

@Slf4j
public class MetaDataAnalyse {
    /**
     * ???????????????????????????flatMap
     */
    private static FlatMapFunction<ImageFileInfo, FileInfo> flatMapFun = new FlatMapFunction<ImageFileInfo, FileInfo>() {
        private static final long serialVersionUID = 6636785248743209527L;

        @Override
        public Iterator<FileInfo> call(ImageFileInfo imageFileInfo) {
            FileInfo fileInfo = new FileInfo(imageFileInfo);
            if (fileInfo != null && !fileInfo.getPath().isEmpty()) {
                List<FileInfo> fileList = new ArrayList();
                String path = fileInfo.getPath();
                String[] splits = path.split("/");
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < splits.length - 1; i++) {
                    if (i == 1) {
                        buffer.append(splits[i]);
                    } else {
                        buffer.append("/").append(splits[i]);
                    }
                    FileInfo info = new FileInfo(imageFileInfo);
                    info.setParentPath(buffer.toString());
                    fileList.add(info);
                }
                return fileList.iterator();
            } else {
                return Collections.emptyIterator();
            }
        }
    };
    /**
     * ??????reduce????????????????????????????????????reduce???
     */
    private static Function2<DirFileInfo, DirFileInfo, DirFileInfo> reduceFun = new Function2<DirFileInfo, DirFileInfo, DirFileInfo>() {

        private static final long serialVersionUID = 7967342273611497381L;

        @Override
        public DirFileInfo call(DirFileInfo v1, DirFileInfo v2) {
            DirFileInfo info = new DirFileInfo();
            info.setPath(v1.getPath());
            info.setTotalFileNum(v1.getTotalFileNum() + v2.getTotalFileNum());
            info.setTotalFileSize(v1.getTotalFileSize() + v2.getTotalFileSize());
            info.setTotalBlockNum(v1.getTotalBlockNum() + v2.getTotalBlockNum());
            info.setTotalSmallFileNum(v1.getTotalSmallFileNum() + v2.getTotalSmallFileNum());
            info.setTotalEmptyFileNum(v1.getTotalEmptyFileNum() + v2.getTotalEmptyFileNum());
            info.setModificationTime(v1.getModificationTime());
            info.setAccessTime(v1.getAccessTime());
            return info;
        }
    };

    /**
     * ????????????????????????????????????????????????
     */
    private static Function2<FileStatsInfo, FileStatsInfo, FileStatsInfo> add = new Function2<FileStatsInfo, FileStatsInfo, FileStatsInfo>() {

        private static final long serialVersionUID = -2962196809668908860L;

        @Override
        public FileStatsInfo call(FileStatsInfo v1, FileStatsInfo v2) throws Exception {
            Long num = v1.getNum();
            num = num + v2.getNum();
            Long size = v1.getSize();
            size = size + v2.getSize();
            return new FileStatsInfo(num, size);
        }
    };

    public static void main(String[] args) {
        Properties properties = BaseJobCmd.init(args);
        runJob(properties);
    }

    private static void runJob(Properties properties) {
        boolean success = false;

        //????????????DNS
        Properties p = new Properties();
        String clusterIpHostJson = properties.getProperty(Constants.CLUSTER_IP_HOST);
        List<ClusterHostConf> hostConfList = JSONArray.parseArray(clusterIpHostJson, ClusterHostConf.class);
        for (ClusterHostConf hostConf : hostConfList) {
            p.setProperty(hostConf.getHost(), hostConf.getIp());
        }
        JavaHost.updateVirtualDns(p);
        JavaHost.printAllVirtualDns();
        //????????????DNS

        String clusterName = properties.getProperty(Constants.CLUSTER_NAME);
        String clusterId = properties.getProperty(Constants.CLUSTER_ID);
        String clusterType = properties.getProperty(Constants.CLUSTER_TYPE);
        String jdbcUrl = properties.getProperty(Constants.JDBC_URL);
        String dayTime = properties.getProperty(Constants.HDFS_FSIMAGE_DAY_TIME);
        String fsimageFilePath = properties.getProperty(Constants.HDFS_FSIMAGE_FILE_PATH);
        String fsimageFileType = properties.getProperty(Constants.HDFS_FSIMAGE_FILE_TYPE);
        String fsimageFileSeparator = properties.getProperty(Constants.HDFS_FSIMAGE_FILE_SEPARATOR);
        String fsimageFileHeader = properties.getProperty(Constants.HDFS_FSIMAGE_FILE_HEADER);
        String zkQuorum = properties.getProperty(Constants.HDFS_FSIMAGE_HBASE_QUORUM);
        String zkPort = properties.getProperty(Constants.HDFS_FSIMAGE_HBASE_PORT);
        String zkNode = properties.getProperty(Constants.HDFS_FSIMAGE_HBASE_ZNODE);
        String clusterConfApi = properties.getProperty(Constants.CLUSTER_CONF_API);
        String downloadFileApi = properties.getProperty(Constants.DOWNLOAD_KRB5_API);
        String phoenixUrl = Constants.PHOENIX_URL_PREFIX + zkQuorum + ":" + zkPort + ":" + zkNode;
        Integer pathIndex = 100;

        String projectRelationUrl = properties.getProperty(Constants.PROJECT_RELATION_URL);
        String userIdKey = properties.getProperty(Constants.HTTP_HEADER_SDPS_USER_ID_KEY);
        String userNameKey = properties.getProperty(Constants.HTTP_HEADER_SDPS_USER_NAME_KEY);
        Map<String, TenantInfo> tenantMap = TenantUtils.getTenantInfo(projectRelationUrl, userIdKey, userNameKey);

        Boolean isEnablekerberos = Boolean.FALSE;
        //????????????????????????kerberos
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-container.xml");
        SdpsClusterMapper sdpsClusterMapper = applicationContext.getBean(SdpsClusterMapper.class);
        Boolean isClusterEnablekerberos = sdpsClusterMapper.selectById(Integer.valueOf(clusterId)).getKerberos();
        SysGlobalArgsMapper sysGlobalArgsMapper = applicationContext.getBean(SysGlobalArgsMapper.class);
        Boolean isDBConfEnablekerberos = Boolean.valueOf(sysGlobalArgsMapper.selectOne(
                new QueryWrapper<SysGlobalArgs>().eq("arg_type","kerberos").eq("arg_key", "enable")).getArgValue());
        if(isClusterEnablekerberos && isDBConfEnablekerberos){
            isEnablekerberos = Boolean.TRUE;
        }

        //??????????????????(hdfs)
        HashMap hdfsConfMap = HdfsUtils.getMapFromApi(clusterConfApi.concat("?")
                .concat("clusterId=").concat(clusterId).concat("&")
                .concat("confStrs=core-site,hdfs-site").concat("&")
                .concat("serverName=HDFS")
        );
        //??????????????????(hbase)
        Configuration hbaseConf = HdfsUtils.loadConfFromApi(clusterConfApi.concat("?")
                .concat("clusterId=").concat(clusterId).concat("&")
                .concat("confStrs=hbase-env,hbase-site").concat("&")
                .concat("serverName=HBASE")
        );
        Configuration hadoopConf = new Configuration();
        for (Object obj : hdfsConfMap.entrySet()) {
            hadoopConf.set(String.valueOf(((Map.Entry) obj).getKey()), String.valueOf(((Map.Entry) obj).getValue()));
        }

        FileSystem fs = null;
        try {
            fs = FileSystem.get(FileSystem.getDefaultUri(hadoopConf), hadoopConf);
            fs.close();
        } catch (IOException e) {
            log.error("??????HDFS FileSystem??????:",e);
            System.exit(1);
        }finally {
            try {
                if(fs != null){
                    fs.close();
                }
            }catch (Exception e){
                log.error("HDFS FileSystem ???????????????:",e);
                System.exit(1);
            }
        }

        SparkConf sparkConf = new SparkConf();
        for (Map.Entry<String, String> entry : fs.getConf()) {
            sparkConf.set(entry.getKey(), entry.getValue());
        }
        SparkSession sparkSession = SparkSession.builder()
                .appName("AnalyseFsImageFile-Job")
                .master("local[*]")
                .config(sparkConf)
                .getOrCreate();
        //?????????????????????
        String sdpsJobId = properties.getProperty(Constants.SDPS_JOB_ID);
        SparkContext sparkContext = sparkSession.sparkContext();


//        if(isEnablekerberos){
//            //krb5File
//            String krb5File = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
//                    .eq("arg_type","kerberos").eq("arg_key", "krb5")).getArgValue();
//            sparkContext.addFile(krb5File);
//            //keytabPath
//            String keytabPath = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
//                    .eq("arg_type","kerberos").eq("arg_key", "keytabPath")).getArgValue();
//            //hbase???keytab??????
//            String hbaseUserKeytab = hbaseConf.get("hbase_user_keytab");
//            hbaseUserKeytab = hbaseUserKeytab
//                    .substring(hbaseUserKeytab.lastIndexOf("/")+1);
//            sparkContext.addFile(keytabPath.concat("/").concat(clusterId).concat(".").concat(hbaseUserKeytab));
//            //hbase??????keytab??????
//            String hbaseServerKeytab = hbaseConf.get("hbase.master.keytab.file");
//            hbaseServerKeytab = hbaseServerKeytab
//                    .substring(hbaseServerKeytab.lastIndexOf("/")+1);
//            sparkContext.addFile(keytabPath.concat("/").concat(clusterId).concat(".").concat(hbaseServerKeytab));
//            //phoenix??????keytab??????
//            String phoenixQueryServerKeytab = hbaseConf.get("phoenix.queryserver.keytab.file");
//            phoenixQueryServerKeytab = phoenixQueryServerKeytab
//                    .substring(phoenixQueryServerKeytab.lastIndexOf("/")+1);
//            sparkContext.addFile(keytabPath.concat("/").concat(clusterId).concat(".").concat(phoenixQueryServerKeytab));
//        }


        String applicationId = sparkContext.applicationId();
        String applicationName = sparkContext.appName();
        String yarnWebappAddress = sparkContext.conf().get(Constants.YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
        String yarnTrackingUrl = "http://".concat(yarnWebappAddress).concat("/cluster/app/").concat(applicationId);
        SdpsTaskInfo sdpsTaskInfo = SdpsTaskInfo.builder()
                .id(Long.valueOf(sdpsJobId))
                .yarnAppId(applicationId)
                .yarnAppName(applicationName)
                .yarnTrackingUrl(yarnTrackingUrl)
                .updateTime(new Date())
                .build();
        //sdps??????????????????ID
        ApplicationContext beanConf = new ClassPathXmlApplicationContext("spring-container.xml");
        SdpsTaskInfoMapper sdpsTaskInfoMapper = beanConf.getBean(SdpsTaskInfoMapper.class);
        int updateCount = sdpsTaskInfoMapper.updateById(sdpsTaskInfo);
        if(updateCount > 0){
            log.info("???????????????????????????:"+sdpsTaskInfo.toString());
        }


        //??????????????????
        Map<String, Object> mapValues = new HashMap();
        mapValues.put("clusterName", clusterName);
        mapValues.put("clusterType", clusterType);
        mapValues.put("dayTime", dayTime);
        mapValues.put(Constants.PHOENIX_BROADCAST_KEY, phoenixUrl);
        mapValues.put("tenantMap", tenantMap);


        //??????kerberos??????PhoenixUrl
        if(isEnablekerberos){

            //hdfs??????Map
            mapValues.put("hdfsConfMap",hdfsConfMap);
            mapValues.put("zkQuorum", zkQuorum);
            mapValues.put("zkPort", zkPort);
            mapValues.put("zkNode", zkNode);

            //????????????
            String tempDir = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
                    .eq("arg_type","kerberos").eq("arg_key", "sparkTaskTempDir")).getArgValue();
            mapValues.put("tempDir",tempDir);
            //hdfs???keytab??????
            String hdfsKeytabPath = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
                    .eq("arg_type","kerberos").eq("arg_key", "hdfsKeytabPath")).getArgValue();
            mapValues.put("hdfsKeytabPath",hdfsKeytabPath);

            //krb5File
            HttpResponse httpResponse = HttpRequest.get(downloadFileApi).execute();
            String krb5Str = httpResponse.body();
            mapValues.put("krb5FileContext",krb5Str);
            //server_keytab???Mapper
            SdpServerKeytabMapper sdpServerKeytabMapper = applicationContext.getBean(SdpServerKeytabMapper.class);
            QueryWrapper<SdpServerKeytab> hbaseUserQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","USER")
                    .eq("local_username","hbase");
            SdpServerKeytab hbaseUserSdpServerKeytab = sdpServerKeytabMapper.selectOne(hbaseUserQueryWrapper);

            //hbase?????????
            mapValues.put("hbasePrincipalName",hbaseConf.get("hbase_principal_name"));
            log.info("????????????hbasePrincipalName:"+hbaseConf.get("hbase_principal_name"));
            //hbase???keytab??????
            mapValues.put("hbaseUserkeytabContext",hbaseUserSdpServerKeytab.getKeytabContent());
            mapValues.put("hbaseUserkeytabFileName",hbaseUserSdpServerKeytab.getKeytabFileName());
            log.info("????????????hbaseUserkeytabFileName:"+hbaseUserSdpServerKeytab.getKeytabFileName());
            //hbase????????????
            mapValues.put("hbaseServerPrincipal", hbaseConf.get("hbase.master.kerberos.principal"));
            log.info("????????????hbaseServerPrincipal:"+hbaseConf.get("hbase.master.kerberos.principal"));
            //hbase??????keytab??????
            QueryWrapper<SdpServerKeytab> hbaseServerQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","SERVICE")
                    .eq("local_username","hbase")
                    .last("limit 1");
            SdpServerKeytab hbaseServerSdpServerKeytab = sdpServerKeytabMapper.selectOne(hbaseServerQueryWrapper);
            mapValues.put("hbaseServerKeytabContent", hbaseServerSdpServerKeytab.getKeytabContent());
            mapValues.put("hbaseServerKeytabFileName", hbaseServerSdpServerKeytab.getKeytabFileName());
            log.info("????????????hbaseServerKeytabFileName:"+hbaseServerSdpServerKeytab.getKeytabFileName());
            //phoenix????????????
            mapValues.put("phoenixQueryserverPrincipal", hbaseConf.get("phoenix.queryserver.kerberos.principal"));
            log.info("????????????phoenixQueryserverPrincipal:"+hbaseConf.get("phoenix.queryserver.kerberos.principal"));
            //phoenix??????keytab??????
            QueryWrapper<SdpServerKeytab> phoenixServerQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","SERVICE")
                    .like("keytab_file_path","%spnego%")
                    .last("limit 1");
            SdpServerKeytab phoenixServerSdpServerKeytab = sdpServerKeytabMapper.selectOne(phoenixServerQueryWrapper);
            mapValues.put("phoenixQueryserverKeytabContent", phoenixServerSdpServerKeytab.getKeytabContent());
            mapValues.put("phoenixQueryserverKeytabFileName", phoenixServerSdpServerKeytab.getKeytabFileName());
            log.info("????????????phoenixQueryserverKeytabFileName:"+phoenixServerSdpServerKeytab.getKeytabFileName());


        }
        mapValues.put("isEnablekerberos", isEnablekerberos);
        //??????????????????
        Broadcast<Map> bcMap = JavaSparkContext.fromSparkContext(sparkSession.sparkContext()).broadcast(mapValues);

        //??????????????????
        int adviceNums = repartitionNums(hadoopConf, fsimageFilePath);
        //???????????????????????????
        Dataset<ImageFileInfo> fileInfoDs = sparkSession.read()
                .format(fsimageFileType)
                .option("sep", fsimageFileSeparator)
                .option("header", fsimageFileHeader)
                //????????????
                .schema(ImageFileInfo.getSchema())
                .load(fsimageFilePath)
                //???Java bean???????????????
                .as(Encoders.bean(ImageFileInfo.class));
        //??????????????????
        int nums = fileInfoDs.javaRDD().getNumPartitions();
        //??????????????????
        int repartitionNums = Math.max(adviceNums, nums);
        log.info("spark file rdd of advice={}, num={}, repartition={}", new Object[]{adviceNums, nums, repartitionNums});

        //??????????????????  //4253 = 2035F + 2218D
        JavaRDD<ImageFileInfo> allFileInfoJavaRDD = fileInfoDs.javaRDD().repartition(repartitionNums).cache();

        //????????????//2035
        JavaRDD<ImageFileInfo> fileInfoRDD = allFileInfoJavaRDD.filter((info) -> {
            return INodeFileType.FILE.getIndex().equals(info.getFileType());
        }).cache();

        //??????????????????????????????flatMap??????//12109
        JavaRDD<FileInfo> fileFlatMapRdd = fileInfoRDD.flatMap(flatMapFun).cache();

        //????????????//2218
        JavaRDD<DirFileInfo> allDirRdd = allFileInfoJavaRDD.filter((info) -> {
            return INodeFileType.DIRECTORY.getIndex().equals(info.getFileType());
        }).map(MetaDataAnalyse::extractEmptyDirInfo);

        //??????flatMap?????????????????????
        int numPartitions = fileFlatMapRdd.partitions().size();

        //??????????????????????????????????????????????????????????????????????????????key???value??????
        //???????????????key???value??????????????????????????? Iterable
        //????????????????????????????????????(???????????????????????????)  //1144
        JavaRDD<DirFileInfo> dirFileRdd = fileFlatMapRdd.mapToPair((info) -> {
            //??????
            int prefix = (new Random()).nextInt(numPartitions);
            //??????????????????(?????????????????????????????????????????????)
            return new Tuple2<>(prefix + "_" + info.getParentPath(), extractParentDirInfo(info));
        }).reduceByKey(reduceFun).mapToPair((tuple) -> {
            DirFileInfo dirFile = (DirFileInfo) ((Tuple2) tuple)._2;
            return new Tuple2<String, DirFileInfo>(dirFile.getPath(), dirFile);
        }).reduceByKey(reduceFun).map((tuple) -> {
            return (DirFileInfo) ((Tuple2) tuple)._2;
        }).cache();

        //????????????
        List<Tuple2<String, FileStatsInfo>> tempResult = fileInfoRDD.mapToPair((info) -> {
            return new Tuple2<String, FileStatsInfo>(getTempKey(info), new FileStatsInfo(1L, Long.valueOf(info.getFileSize())));
        }).reduceByKey(add).collect();
        //????????????
        List<Tuple2<String, FileStatsInfo>> sizeResult = fileInfoRDD.mapToPair((info) -> {
            return new Tuple2<String, FileStatsInfo>(getSizeKey(info), new FileStatsInfo(1L, Long.valueOf(info.getFileSize())));
        }).reduceByKey(add).collect();

        List<List<Object>> statsArgs = new ArrayList();
        statsArgs.addAll(getStatsArgs(tempResult, clusterName, clusterId, dayTime, "temp"));
        statsArgs.addAll(getStatsArgs(sizeResult, clusterName, clusterId, dayTime, "size"));
        //??????Mysql
        JDBCUtils jdbcUtils = new JDBCUtils(jdbcUrl);
        //????????????????????????
        jdbcUtils.execute("DELETE from sdps_hdfs_file_stats where cluster=? and day_time=?", Arrays.asList(clusterName, dayTime));
        //????????????
        jdbcUtils.executeBatch("INSERT INTO sdps_hdfs_file_stats (cluster,cluster_id, day_time, type, type_key, type_value_num, type_value_size, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, now(), now())", statsArgs,new Properties());

        //?????????,??????RDD  //2218
        JavaRDD<String> allDirPathRdd = allDirRdd.map((info) -> {
            return info.getPath();
        }).cache();
        //???????????????,??????RDD  //1144
        JavaRDD<String> dirFilePathRdd = dirFileRdd.map((info) -> {
            return info.getPath();
        }).cache();
        //??????????????? = ????????? - ???????????????  //1074
        List<String> emptyDirList = allDirPathRdd.subtract(dirFilePathRdd).collect();

        //??????????????? + ??????????????? //2218
        JavaRDD<DirFileInfo> dirRdd = allDirRdd.filter((info) -> {
            return emptyDirList.contains(info.getPath());
        }).union(dirFileRdd).map((info) -> {
            return decorateInfo(info, bcMap);
        }).repartition(numPartitions).cache();

        jdbcUtils.execute("DELETE from sdps_hdfs_db_table where cluster=?", Arrays.asList(clusterName));

        //??????Mysql
        long mysqlStart = Clock.systemUTC().millis();
        log.info("Mysql??????????????????,????????????={}", mysqlStart);
        dirRdd.filter((entity) -> {
            return RegexUtils.isMatchDBTable(entity.getPath());
        }).foreachPartition((iterator) -> {
            insertMySQL(iterator, jdbcUrl, clusterId);
        });
        long mysqlEnd = Clock.systemUTC().millis();
        log.info("Mysql??????????????????,????????????={},?????????={}???", mysqlEnd, (mysqlStart - mysqlEnd) / 1000L);

        //??????HBase,??????
        long startHBase = Clock.systemUTC().millis();
        log.info("Hbase??????????????????,????????????={}", mysqlStart);
        dirRdd.filter((info) -> {
            //????????????????????????pathIndex?????????
            return validPath(info, pathIndex);
        }).repartition(numPartitions).foreachPartition((iterator) -> {
            insertPhoenix(iterator, bcMap);
        });

        long endHBase = Clock.systemUTC().millis();
        log.info("Hbase??????????????????,????????????={}, ?????????={}???", endHBase, (endHBase - startHBase) / 1000L);

    }



    /**
     * ???????????????
     *
     * @param conf
     * @param filePath
     * @return
     */
    private static int repartitionNums(Configuration conf, String filePath) {
        int nums = 0;
        try {
            FileSystem fs = FileSystem.newInstance(conf);
            Path path = new Path(filePath);
            long length = 0L;
            if (fs.isDirectory(path)) {
                //?????????
                RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(path, true);
                while (iterator.hasNext()) {
                    length += ((LocatedFileStatus) iterator.next()).getLen();
                }
            } else {
                //?????????
                length = fs.getContentSummary(path).getLength();
            }
            //Math.toIntExact(Long)?????????????????????????????????????????????????????????ArithmeticException???????????????????????????????????????
            //67108864??????=64MB
            //length ???64MB,???nums=2
            nums = Math.toIntExact(length / 67108864L + 1L);
        } catch (Exception e) {
            log.error("?????????????????????:", e);
            System.exit(1);
        }
        return nums;
    }

    /**
     * ?????????????????????
     *
     * @param fileInfo
     * @return
     */
    private static DirFileInfo extractParentDirInfo(FileInfo fileInfo) {
        String path = fileInfo.getParentPath();
        Long fileSize = Long.valueOf(fileInfo.getFileSize());
        DirFileInfo dirInfo = new DirFileInfo();
        dirInfo.setPath(path);
        //??????????????????
        dirInfo.setTotalFileNum(1L);
        //??????????????????
        dirInfo.setTotalFileSize(fileSize);
        //??????????????????
        dirInfo.setTotalBlockNum(Long.valueOf(fileInfo.getBlockCount()));
        //????????????????????????????????????64MB??????????????????
        Long smallFileNum = fileSize < Constants.SMALL_FILE_BYTES ? 1L : 0L;
        dirInfo.setTotalSmallFileNum(smallFileNum);
        //???????????????????????????????????????0????????????????????????
        Long emptyFileNum = fileSize == 0L ? 1L : 0L;
        dirInfo.setTotalEmptyFileNum(emptyFileNum);
        //??????????????????
        dirInfo.setModificationTime(fileInfo.getModificationTime());
        //??????????????????
        dirInfo.setAccessTime(fileInfo.getAccessTime());
        return dirInfo;
    }

    private static String getTempKey(ImageFileInfo fileInfo) {
        String activeTime = StringUtils.isNotBlank(fileInfo.getAccessTime()) ? fileInfo.getAccessTime() : fileInfo.getModificationTime();
        int diffDays = (int) Math.abs((Long.valueOf(activeTime) - Clock.systemUTC().millis()) / 86400000L);
        return FileStatsType.getTempType(diffDays).name();
    }

    private static String getSizeKey(ImageFileInfo fileInfo) {
        Long fileSize = StringUtils.isNotBlank(fileInfo.getFileSize()) ? Long.valueOf(fileInfo.getFileSize()) : 0L;
        return FileStatsType.getSizeType(fileSize).name();
    }

    private static List<List<Object>> getStatsArgs(List<Tuple2<String, FileStatsInfo>> tupleList, String cluster, String clusterId, String dayTime, String type) {
        List<List<Object>> statsArgs = new ArrayList();
        Iterator<Tuple2<String, FileStatsInfo>> iterator = tupleList.iterator();

        while (iterator.hasNext()) {
            Tuple2<String, FileStatsInfo> tuple = (Tuple2) iterator.next();
            List<Object> args = new ArrayList();
            args.add(cluster);
            args.add(clusterId);
            args.add(dayTime);
            args.add(type);
            args.add(tuple._1);
            args.add(((FileStatsInfo) tuple._2).getNum());
            args.add(((FileStatsInfo) tuple._2).getSize());
            statsArgs.add(args);
        }
        return statsArgs;
    }

    /**
     * ??????????????????????????????
     *
     * @param dirInfo
     * @param bcMap
     * @return
     */
    private static DirFileInfo decorateInfo(DirFileInfo dirInfo, Broadcast<Map> bcMap) {
        Map valueMap = (Map) bcMap.getValue();
        String dayTime = (String) valueMap.get("dayTime");
        String clusterName = (String) valueMap.get("clusterName");
        Map<String, TenantInfo> tenantMap = (Map) valueMap.get("tenantMap");
        //????????????
        dirInfo.setDayTime(dayTime);
        //????????????
        dirInfo.setCluster(clusterName);
        //???????????????
        DecimalFormat df = new DecimalFormat("0.00");
        double avgSize = 0.0D;
        if (dirInfo.getTotalFileNum() > 0L) {
            avgSize = Double.valueOf(df.format(dirInfo.getTotalFileSize() / dirInfo.getTotalFileNum()));
        }
        //???????????????
        dirInfo.setAvgFileSize(avgSize);

        String path = dirInfo.getPath();
        //??????????????????
        Integer index = !StringUtils.isBlank(path) && !"/".equals(path) ? dirInfo.getPath().split("/").length : 1;
        dirInfo.setPathIndex(index);

        //????????????
        dirInfo.setTemperature(DataTempType.WARM.getIndex());

        String tenantKey = RegexUtils.matchTenantKey(path);
        if (tenantMap.containsKey(tenantKey)) {
            TenantInfo tenantInfo = (TenantInfo) tenantMap.get(tenantKey);
            dirInfo.setType(tenantInfo.getType().getIndex());
            dirInfo.setTypeValue(tenantInfo.getTypeValue());
            dirInfo.setTenant(tenantInfo.getName());
            return dirInfo;
        } else {
            //????????????????????????????????????
            DBTableInfo dbTable = RegexUtils.matchDBTable(path, clusterName);
            if (dbTable != null) {
                dirInfo.setType(dbTable.getDirType().getIndex());
                dirInfo.setTypeValue(dbTable.getDirTypeValue());
                return dirInfo;
            } else {
                dirInfo.setType(DirFileType.UNKOWN.getIndex());
                return dirInfo;
            }
        }

    }

    private static void insertMySQL(Iterator<DirFileInfo> iterator, String jdbc, String clusterId) {
        JDBCUtils jdbcUtils = new JDBCUtils(jdbc);
        ArrayList dbTableArgs = new ArrayList();
        while (iterator.hasNext()) {
            DirFileInfo info = (DirFileInfo) iterator.next();
            DBTableInfo dbTable = RegexUtils.matchDBTable(info.getPath(), info.getCluster());
            if (dbTable != null) {
                dbTableArgs.add(getDbTableArgs(dbTable, clusterId));
                if (dbTableArgs.size() == 10000) {
                    //????????????10000
                    log.info("mysql batch insert of size = 10000");
                    jdbcUtils.executeBatch("INSERT INTO sdps_hdfs_db_table (cluster, cluster_id, db_name, table_name, type, category, path, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?, now(), now())", dbTableArgs,new Properties());
                    dbTableArgs = new ArrayList();
                }
            }
        }
        log.info("Mysql?????????????????????[tb_db_table] size={}", dbTableArgs.size());
        //??????????????????
        if (!CollectionUtils.isEmpty(dbTableArgs)) {
            jdbcUtils.executeBatch("INSERT INTO sdps_hdfs_db_table (cluster, cluster_id, db_name, table_name, type, category, path, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?, now(), now())", dbTableArgs,new Properties());
        }
    }

    /**
     * @param dbTable
     * @return
     */
    private static List<Object> getDbTableArgs(DBTableInfo dbTable, String clusterId) {
        Preconditions.checkNotNull(dbTable);
        List<Object> args = new ArrayList();
        args.add(dbTable.getCluster());
        args.add(clusterId);
        args.add(dbTable.getDbName());
        args.add(dbTable.getTableName());
        args.add(dbTable.getType());
        args.add(dbTable.getCategory());
        args.add(dbTable.getPath());
        return args;
    }

    /**
     * ????????????
     *
     * @param info
     * @return
     */
    private static boolean validPath(DirFileInfo info, int pathIndex) {
        if (info.getPathIndex() > pathIndex) {
            return false;
        } else {
            return true;
        }
    }

    private static void insertPhoenix(Iterator<DirFileInfo> iterator, Broadcast<Map> bcMap) {
        long startTime = Clock.systemUTC().millis();
//        System.setProperty("HADOOP_USER_NAME", "hbase");
//        //????????????DNS
//        Properties properties = new Properties();
//        properties.setProperty("master", "10.1.3.24");
//        properties.setProperty("slave1", "10.1.3.25");
//        properties.setProperty("slave2", "10.1.3.26");
//        JavaHost.updateVirtualDns(properties);
//        JavaHost.printAllVirtualDns();
//        //????????????DNS

        //??????hbase????????????
        final Configuration hbaseConf = HBaseConfiguration.create();

        Boolean isEnablekerberos = (Boolean) ((Map) bcMap.getValue()).get("isEnablekerberos");
        //kerberos????????????
        String zkQuorum = "";
        String zkPort = "";
        String zkNode = "";
        String krb5File = "";
        String hbasePrincipalName = "";
        String hbaseUserkeytabFile = "";
        String hbaseServerPrincipal = "";
        String hbaseServerKeytabFile = "";
        String phoenixQueryserverPrincipal = "";
        String phoenixQueryserverKeytabFile = "";
        if(isEnablekerberos){
            //????????????
            String tempDir = (String) ((Map) bcMap.getValue()).get("tempDir");

            //???????????????Principal???keytab???????????????+keytab??????
            zkQuorum = (String) ((Map) bcMap.getValue()).get("zkQuorum");
            zkNode = (String) ((Map) bcMap.getValue()).get("zkNode");
            zkPort = (String) ((Map) bcMap.getValue()).get("zkPort");
            String krb5FileContext = (String) ((Map) bcMap.getValue()).get("krb5FileContext");
            krb5File = tempDir.concat("/").concat("krb5.conf");
            hbasePrincipalName = (String) ((Map) bcMap.getValue()).get("hbasePrincipalName");
            String hbaseUserkeytabContext = (String) ((Map) bcMap.getValue()).get("hbaseUserkeytabContext");
            String hbaseUserkeytabFileName = (String) ((Map) bcMap.getValue()).get("hbaseUserkeytabFileName");
            hbaseUserkeytabFile = tempDir.concat("/").concat(hbaseUserkeytabFileName);
            hbaseServerPrincipal = (String) ((Map) bcMap.getValue()).get("hbaseServerPrincipal");
            String hbaseServerKeytabContent = (String) ((Map) bcMap.getValue()).get("hbaseServerKeytabContent");
            String hbaseServerKeytabFileName = (String) ((Map) bcMap.getValue()).get("hbaseServerKeytabFileName");
            hbaseServerKeytabFile = tempDir.concat("/").concat(hbaseServerKeytabFileName);
            phoenixQueryserverPrincipal = (String) ((Map) bcMap.getValue()).get("phoenixQueryserverPrincipal");
            String phoenixQueryserverKeytabContent = (String) ((Map) bcMap.getValue()).get("phoenixQueryserverKeytabContent");
            String phoenixQueryserverKeytabFileName = (String) ((Map) bcMap.getValue()).get("phoenixQueryserverKeytabFileName");
            phoenixQueryserverKeytabFile = tempDir.concat("/").concat(phoenixQueryserverKeytabFileName);

            log.info("????????????:");
            log.info("krb5File:"+krb5File);
            log.info("hbasePrincipalName:"+hbasePrincipalName);
            log.info("hbaseUserkeytabFile:"+hbaseUserkeytabFile);
            log.info("hbaseServerPrincipal:"+hbaseServerPrincipal);
            log.info("hbaseServerKeytabFile:"+hbaseServerKeytabFile);
            log.info("phoenixQueryserverPrincipal:"+phoenixQueryserverPrincipal);
            log.info("phoenixServerKeytabFile:"+phoenixQueryserverKeytabFile);
            log.info("????????????:=================");

            //??????keytab???????????????
            HashMap hdfsConfMap = (HashMap) ((Map) bcMap.getValue()).get("hdfsConfMap");
            Configuration hdfsConf = new Configuration();
            for (Object obj : hdfsConfMap.entrySet()) {
                hdfsConf.set(String.valueOf(((Map.Entry) obj).getKey()), String.valueOf(((Map.Entry) obj).getValue()));
            }
            //??????????????????
            File tempFile = new File(tempDir);
            if(!tempFile.exists()){
                tempFile.mkdir();
            }

            try {
                FileUtils.write(new File(krb5File),krb5FileContext,"UTF-8");
            } catch (Exception e) {
                log.error("??????krb5????????????:",e);
            }
            try {
                Base64Util.convertStrToFile(hbaseUserkeytabContext,hbaseUserkeytabFile);
            } catch (Exception e) {
                log.error("??????hbase??????keytab????????????:",e);
            }
            try {
                Base64Util.convertStrToFile(hbaseServerKeytabContent,hbaseServerKeytabFile);
            } catch (Exception e) {
                log.error("??????hbase??????keytab????????????:",e);
            }
            try {
                Base64Util.convertStrToFile(phoenixQueryserverKeytabContent,phoenixQueryserverKeytabFile);
            } catch (Exception e) {
                log.error("??????Phoenix??????keytab????????????:",e);
            }

            hbaseConf.setBoolean(CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,true);
            hbaseConf.setBoolean("phoenix.schema.isNamespaceMappingEnabled", true);
            hbaseConf.set("hadoop.security.authentication", "Kerberos");
            hbaseConf.set("hbase.security.authentication", "Kerberos");
            hbaseConf.set("hbase.zookeeper.quorum", zkQuorum);
            hbaseConf.set("hbase.zookeeper.property.clientport", zkPort);
            hbaseConf.set("hbase.master.kerberos.principal",hbaseServerPrincipal);
            hbaseConf.set("hbase.regionserver.kerberos.principal",hbaseServerPrincipal);
        }

        String phoenixUrl = (String) ((Map) bcMap.getValue()).get(Constants.PHOENIX_BROADCAST_KEY);
        String phoneixTableName = "HDFS_NM.TB_DIR_INFO";
        String insertSQL = "UPSERT INTO " + phoneixTableName + " (CLUSTER, PATH, DAY_TIME, TOTAL_FILE_NUM, TOTAL_FILE_SIZE, AVG_FILE_SIZE, TOTAL_BLOCK_NUM, TOTAL_SMALL_FILE_NUM, TOTAL_EMPTY_FILE_NUM, PATH_INDEX, TYPE, TYPE_VALUE, TENANT, MODIFICATION_TIME, ACCESS_TIME, TEMPERATURE) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        log.info("??????phoenix????????????,??????sql;phoenixUrl={}, tableName={}, sql={}, ????????????={}", new Object[]{phoenixUrl, phoneixTableName, insertSQL, startTime});
        JDBCUtils jdbcUtils = new JDBCUtils(phoenixUrl);
        List<List<Object>> paramsList = new ArrayList();
        int count = 0;

        while (iterator.hasNext()) {
            DirFileInfo info = (DirFileInfo) iterator.next();
            if (info != null) {
                ++count;
                List<Object> params = getInsertArgs(info);
                paramsList.add(params);
                if (paramsList.size() == 10000) {
                    long beginTime = Clock.systemUTC().millis();
                    log.info("????????????phoenix??????hbase??????,?????????={},????????????={}", 10000, beginTime);
                    //??????????????????Kerberos
                    if(isEnablekerberos){
                        jdbcUtils.executeBatchKerberos(
                                insertSQL,paramsList,zkQuorum,zkNode,
                                krb5File,hbasePrincipalName,hbaseUserkeytabFile,
                                hbaseServerPrincipal,hbaseServerKeytabFile,
                                phoenixQueryserverPrincipal,phoenixQueryserverKeytabFile,hbaseConf
                        );
                    }else {
                        jdbcUtils.executeBatch(insertSQL, paramsList,new Properties());
                    }
                    long endTime = Clock.systemUTC().millis();
                    log.info("??????phoenix??????1000???????????????hbase??????,?????????={},????????????={},??????={} ???", new Object[]{10000, endTime, (endTime - beginTime) / 1000L});
                    //??????ArrayList
                    paramsList = new ArrayList();
                }
            }
        }
        //??????????????????Kerberos
        if(isEnablekerberos){
            jdbcUtils.executeBatchKerberos(
                    insertSQL,paramsList,zkQuorum,zkNode,
                    krb5File,hbasePrincipalName,hbaseUserkeytabFile,
                    hbaseServerPrincipal,hbaseServerKeytabFile,
                    phoenixQueryserverPrincipal,phoenixQueryserverKeytabFile,hbaseConf
            );
        }else {
            jdbcUtils.executeBatch(insertSQL, paramsList,new Properties());
        }
        long finishTime = Clock.systemUTC().millis();
        log.info("????????????phoenix??????hbase?????? tableName={}, count={}, ????????????={}, ??????={}???", new Object[]{phoneixTableName, count, finishTime, (finishTime - startTime) / 1000L});

    }

    /**
     * ?????????????????????
     *
     * @param info
     * @return
     * @throws Exception
     */
    private static List<Object> getInsertArgs(DirFileInfo info) {
        List<Object> params = new ArrayList();
        params.add(info.getCluster());
        params.add(info.getPath());
        params.add(info.getDayTime());
        params.add(info.getTotalFileNum());
        params.add(info.getTotalFileSize());
        params.add(info.getAvgFileSize());
        params.add(info.getTotalBlockNum());
        params.add(info.getTotalSmallFileNum());
        params.add(info.getTotalEmptyFileNum());
        params.add(info.getPathIndex());
        params.add(info.getType());
        params.add(info.getTypeValue());
        params.add(info.getTenant());
        params.add(info.getModificationTime());
        params.add(info.getAccessTime());
        params.add(info.getTemperature());
        return params;
    }

    /**
     * ?????????????????????
     *
     * @param fileInfo
     * @return
     */
    private static DirFileInfo extractEmptyDirInfo(ImageFileInfo fileInfo) {
        DirFileInfo dirInfo = new DirFileInfo();
        dirInfo.setPath(fileInfo.getPath());
        dirInfo.setTotalFileNum(0L);
        dirInfo.setTotalFileSize(0L);
        dirInfo.setTotalBlockNum(0L);
        dirInfo.setTotalSmallFileNum(0L);
        dirInfo.setTotalEmptyFileNum(0L);
        dirInfo.setModificationTime(fileInfo.getModificationTime());
        dirInfo.setAccessTime(fileInfo.getAccessTime());
        return dirInfo;
    }
}
