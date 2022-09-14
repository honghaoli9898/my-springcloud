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
     * 对目录中的文件进行flatMap
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
     * 重写reduce方法（对目录中的文件进行reduce）
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
     * 对文件数量和文件大小进行叠加计算
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

        //设置虚拟DNS
        Properties p = new Properties();
        String clusterIpHostJson = properties.getProperty(Constants.CLUSTER_IP_HOST);
        List<ClusterHostConf> hostConfList = JSONArray.parseArray(clusterIpHostJson, ClusterHostConf.class);
        for (ClusterHostConf hostConf : hostConfList) {
            p.setProperty(hostConf.getHost(), hostConf.getIp());
        }
        JavaHost.updateVirtualDns(p);
        JavaHost.printAllVirtualDns();
        //设置虚拟DNS

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
        //查询集群是否开启kerberos
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-container.xml");
        SdpsClusterMapper sdpsClusterMapper = applicationContext.getBean(SdpsClusterMapper.class);
        Boolean isClusterEnablekerberos = sdpsClusterMapper.selectById(Integer.valueOf(clusterId)).getKerberos();
        SysGlobalArgsMapper sysGlobalArgsMapper = applicationContext.getBean(SysGlobalArgsMapper.class);
        Boolean isDBConfEnablekerberos = Boolean.valueOf(sysGlobalArgsMapper.selectOne(
                new QueryWrapper<SysGlobalArgs>().eq("arg_type","kerberos").eq("arg_key", "enable")).getArgValue());
        if(isClusterEnablekerberos && isDBConfEnablekerberos){
            isEnablekerberos = Boolean.TRUE;
        }

        //加载集群配置(hdfs)
        HashMap hdfsConfMap = HdfsUtils.getMapFromApi(clusterConfApi.concat("?")
                .concat("clusterId=").concat(clusterId).concat("&")
                .concat("confStrs=core-site,hdfs-site").concat("&")
                .concat("serverName=HDFS")
        );
        //加载集群配置(hbase)
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
            log.error("获取HDFS FileSystem异常:",e);
            System.exit(1);
        }finally {
            try {
                if(fs != null){
                    fs.close();
                }
            }catch (Exception e){
                log.error("HDFS FileSystem 关闭流异常:",e);
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
        //更新任务表数据
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
//            //hbase的keytab文件
//            String hbaseUserKeytab = hbaseConf.get("hbase_user_keytab");
//            hbaseUserKeytab = hbaseUserKeytab
//                    .substring(hbaseUserKeytab.lastIndexOf("/")+1);
//            sparkContext.addFile(keytabPath.concat("/").concat(clusterId).concat(".").concat(hbaseUserKeytab));
//            //hbase服务keytab文件
//            String hbaseServerKeytab = hbaseConf.get("hbase.master.keytab.file");
//            hbaseServerKeytab = hbaseServerKeytab
//                    .substring(hbaseServerKeytab.lastIndexOf("/")+1);
//            sparkContext.addFile(keytabPath.concat("/").concat(clusterId).concat(".").concat(hbaseServerKeytab));
//            //phoenix服务keytab文件
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
        //sdps任务作业映射ID
        ApplicationContext beanConf = new ClassPathXmlApplicationContext("spring-container.xml");
        SdpsTaskInfoMapper sdpsTaskInfoMapper = beanConf.getBean(SdpsTaskInfoMapper.class);
        int updateCount = sdpsTaskInfoMapper.updateById(sdpsTaskInfo);
        if(updateCount > 0){
            log.info("成功更新任务表数据:"+sdpsTaskInfo.toString());
        }


        //设置广播变量
        Map<String, Object> mapValues = new HashMap();
        mapValues.put("clusterName", clusterName);
        mapValues.put("clusterType", clusterType);
        mapValues.put("dayTime", dayTime);
        mapValues.put(Constants.PHOENIX_BROADCAST_KEY, phoenixUrl);
        mapValues.put("tenantMap", tenantMap);


        //开启kerberos处理PhoenixUrl
        if(isEnablekerberos){

            //hdfs配置Map
            mapValues.put("hdfsConfMap",hdfsConfMap);
            mapValues.put("zkQuorum", zkQuorum);
            mapValues.put("zkPort", zkPort);
            mapValues.put("zkNode", zkNode);

            //临时目录
            String tempDir = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
                    .eq("arg_type","kerberos").eq("arg_key", "sparkTaskTempDir")).getArgValue();
            mapValues.put("tempDir",tempDir);
            //hdfs上keytab路径
            String hdfsKeytabPath = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
                    .eq("arg_type","kerberos").eq("arg_key", "hdfsKeytabPath")).getArgValue();
            mapValues.put("hdfsKeytabPath",hdfsKeytabPath);

            //krb5File
            HttpResponse httpResponse = HttpRequest.get(downloadFileApi).execute();
            String krb5Str = httpResponse.body();
            mapValues.put("krb5FileContext",krb5Str);
            //server_keytab表Mapper
            SdpServerKeytabMapper sdpServerKeytabMapper = applicationContext.getBean(SdpServerKeytabMapper.class);
            QueryWrapper<SdpServerKeytab> hbaseUserQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","USER")
                    .eq("local_username","hbase");
            SdpServerKeytab hbaseUserSdpServerKeytab = sdpServerKeytabMapper.selectOne(hbaseUserQueryWrapper);

            //hbase主体名
            mapValues.put("hbasePrincipalName",hbaseConf.get("hbase_principal_name"));
            log.info("打印变量hbasePrincipalName:"+hbaseConf.get("hbase_principal_name"));
            //hbase的keytab文件
            mapValues.put("hbaseUserkeytabContext",hbaseUserSdpServerKeytab.getKeytabContent());
            mapValues.put("hbaseUserkeytabFileName",hbaseUserSdpServerKeytab.getKeytabFileName());
            log.info("打印变量hbaseUserkeytabFileName:"+hbaseUserSdpServerKeytab.getKeytabFileName());
            //hbase服务主体
            mapValues.put("hbaseServerPrincipal", hbaseConf.get("hbase.master.kerberos.principal"));
            log.info("打印变量hbaseServerPrincipal:"+hbaseConf.get("hbase.master.kerberos.principal"));
            //hbase服务keytab文件
            QueryWrapper<SdpServerKeytab> hbaseServerQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","SERVICE")
                    .eq("local_username","hbase")
                    .last("limit 1");
            SdpServerKeytab hbaseServerSdpServerKeytab = sdpServerKeytabMapper.selectOne(hbaseServerQueryWrapper);
            mapValues.put("hbaseServerKeytabContent", hbaseServerSdpServerKeytab.getKeytabContent());
            mapValues.put("hbaseServerKeytabFileName", hbaseServerSdpServerKeytab.getKeytabFileName());
            log.info("打印变量hbaseServerKeytabFileName:"+hbaseServerSdpServerKeytab.getKeytabFileName());
            //phoenix服务主体
            mapValues.put("phoenixQueryserverPrincipal", hbaseConf.get("phoenix.queryserver.kerberos.principal"));
            log.info("打印变量phoenixQueryserverPrincipal:"+hbaseConf.get("phoenix.queryserver.kerberos.principal"));
            //phoenix服务keytab文件
            QueryWrapper<SdpServerKeytab> phoenixServerQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                    .eq("principal_type","SERVICE")
                    .like("keytab_file_path","%spnego%")
                    .last("limit 1");
            SdpServerKeytab phoenixServerSdpServerKeytab = sdpServerKeytabMapper.selectOne(phoenixServerQueryWrapper);
            mapValues.put("phoenixQueryserverKeytabContent", phoenixServerSdpServerKeytab.getKeytabContent());
            mapValues.put("phoenixQueryserverKeytabFileName", phoenixServerSdpServerKeytab.getKeytabFileName());
            log.info("打印变量phoenixQueryserverKeytabFileName:"+phoenixServerSdpServerKeytab.getKeytabFileName());


        }
        mapValues.put("isEnablekerberos", isEnablekerberos);
        //设置广播变量
        Broadcast<Map> bcMap = JavaSparkContext.fromSparkContext(sparkSession.sparkContext()).broadcast(mapValues);

        //建议分区数量
        int adviceNums = repartitionNums(hadoopConf, fsimageFilePath);
        //读取需要分析的文件
        Dataset<ImageFileInfo> fileInfoDs = sparkSession.read()
                .format(fsimageFileType)
                .option("sep", fsimageFileSeparator)
                .option("header", fsimageFileHeader)
                //设置模式
                .schema(ImageFileInfo.getSchema())
                .load(fsimageFilePath)
                //为Java bean创建编码器
                .as(Encoders.bean(ImageFileInfo.class));
        //实际分区数量
        int nums = fileInfoDs.javaRDD().getNumPartitions();
        //最终分区数量
        int repartitionNums = Math.max(adviceNums, nums);
        log.info("spark file rdd of advice={}, num={}, repartition={}", new Object[]{adviceNums, nums, repartitionNums});

        //数据集重分区  //4253 = 2035F + 2218D
        JavaRDD<ImageFileInfo> allFileInfoJavaRDD = fileInfoDs.javaRDD().repartition(repartitionNums).cache();

        //处理文件//2035
        JavaRDD<ImageFileInfo> fileInfoRDD = allFileInfoJavaRDD.filter((info) -> {
            return INodeFileType.FILE.getIndex().equals(info.getFileType());
        }).cache();

        //对文件类型的目录进行flatMap操作//12109
        JavaRDD<FileInfo> fileFlatMapRdd = fileInfoRDD.flatMap(flatMapFun).cache();

        //全部目录//2218
        JavaRDD<DirFileInfo> allDirRdd = allFileInfoJavaRDD.filter((info) -> {
            return INodeFileType.DIRECTORY.getIndex().equals(info.getFileType());
        }).map(MetaDataAnalyse::extractEmptyDirInfo);

        //获取flatMap后的文件的分区
        int numPartitions = fileFlatMapRdd.partitions().size();

        //对每一条输入进行指定的操作，然后为每一条输入返回一个key－value对象
        //最后将所有key－value对象合并为一个对象 Iterable
        //对每一级目录信息进行分析(所有文件类型的目录)  //1144
        JavaRDD<DirFileInfo> dirFileRdd = fileFlatMapRdd.mapToPair((info) -> {
            //前缀
            int prefix = (new Random()).nextInt(numPartitions);
            //解析目录信息(判断文件是否是小文件、空文件等)
            return new Tuple2<>(prefix + "_" + info.getParentPath(), extractParentDirInfo(info));
        }).reduceByKey(reduceFun).mapToPair((tuple) -> {
            DirFileInfo dirFile = (DirFileInfo) ((Tuple2) tuple)._2;
            return new Tuple2<String, DirFileInfo>(dirFile.getPath(), dirFile);
        }).reduceByKey(reduceFun).map((tuple) -> {
            return (DirFileInfo) ((Tuple2) tuple)._2;
        }).cache();

        //数据热度
        List<Tuple2<String, FileStatsInfo>> tempResult = fileInfoRDD.mapToPair((info) -> {
            return new Tuple2<String, FileStatsInfo>(getTempKey(info), new FileStatsInfo(1L, Long.valueOf(info.getFileSize())));
        }).reduceByKey(add).collect();
        //数据大小
        List<Tuple2<String, FileStatsInfo>> sizeResult = fileInfoRDD.mapToPair((info) -> {
            return new Tuple2<String, FileStatsInfo>(getSizeKey(info), new FileStatsInfo(1L, Long.valueOf(info.getFileSize())));
        }).reduceByKey(add).collect();

        List<List<Object>> statsArgs = new ArrayList();
        statsArgs.addAll(getStatsArgs(tempResult, clusterName, clusterId, dayTime, "temp"));
        statsArgs.addAll(getStatsArgs(sizeResult, clusterName, clusterId, dayTime, "size"));
        //插入Mysql
        JDBCUtils jdbcUtils = new JDBCUtils(jdbcUrl);
        //先删除重复的数据
        jdbcUtils.execute("DELETE from sdps_hdfs_file_stats where cluster=? and day_time=?", Arrays.asList(clusterName, dayTime));
        //执行插入
        jdbcUtils.executeBatch("INSERT INTO sdps_hdfs_file_stats (cluster,cluster_id, day_time, type, type_key, type_value_num, type_value_size, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, now(), now())", statsArgs,new Properties());

        //总目录,路径RDD  //2218
        JavaRDD<String> allDirPathRdd = allDirRdd.map((info) -> {
            return info.getPath();
        }).cache();
        //有文件目录,路径RDD  //1144
        JavaRDD<String> dirFilePathRdd = dirFileRdd.map((info) -> {
            return info.getPath();
        }).cache();
        //空文件目录 = 总目录 - 有文件目录  //1074
        List<String> emptyDirList = allDirPathRdd.subtract(dirFilePathRdd).collect();

        //空文件目录 + 有文件目录 //2218
        JavaRDD<DirFileInfo> dirRdd = allDirRdd.filter((info) -> {
            return emptyDirList.contains(info.getPath());
        }).union(dirFileRdd).map((info) -> {
            return decorateInfo(info, bcMap);
        }).repartition(numPartitions).cache();

        jdbcUtils.execute("DELETE from sdps_hdfs_db_table where cluster=?", Arrays.asList(clusterName));

        //插入Mysql
        long mysqlStart = Clock.systemUTC().millis();
        log.info("Mysql开始插入数据,开始时间={}", mysqlStart);
        dirRdd.filter((entity) -> {
            return RegexUtils.isMatchDBTable(entity.getPath());
        }).foreachPartition((iterator) -> {
            insertMySQL(iterator, jdbcUrl, clusterId);
        });
        long mysqlEnd = Clock.systemUTC().millis();
        log.info("Mysql完成插入数据,结束时间={},总用时={}秒", mysqlEnd, (mysqlStart - mysqlEnd) / 1000L);

        //插入HBase,开始
        long startHBase = Clock.systemUTC().millis();
        log.info("Hbase开始插入数据,开始时间={}", mysqlStart);
        dirRdd.filter((info) -> {
            //过滤路径深度小于pathIndex的路径
            return validPath(info, pathIndex);
        }).repartition(numPartitions).foreachPartition((iterator) -> {
            insertPhoenix(iterator, bcMap);
        });

        long endHBase = Clock.systemUTC().millis();
        log.info("Hbase完成插入数据,结束时间={}, 总用时={}秒", endHBase, (endHBase - startHBase) / 1000L);

    }



    /**
     * 重分区数量
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
                //是目录
                RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(path, true);
                while (iterator.hasNext()) {
                    length += ((LocatedFileStatus) iterator.next()).getLen();
                }
            } else {
                //非目录
                length = fs.getContentSummary(path).getLength();
            }
            //Math.toIntExact(Long)方法在整数溢出时不返回任何值，而是引发ArithmeticException与信息性消息“整数溢出”。
            //67108864字节=64MB
            //length 为64MB,这nums=2
            nums = Math.toIntExact(length / 67108864L + 1L);
        } catch (Exception e) {
            log.error("重分区计算失败:", e);
            System.exit(1);
        }
        return nums;
    }

    /**
     * 提取父目录信息
     *
     * @param fileInfo
     * @return
     */
    private static DirFileInfo extractParentDirInfo(FileInfo fileInfo) {
        String path = fileInfo.getParentPath();
        Long fileSize = Long.valueOf(fileInfo.getFileSize());
        DirFileInfo dirInfo = new DirFileInfo();
        dirInfo.setPath(path);
        //设置文件个数
        dirInfo.setTotalFileNum(1L);
        //设置文件大小
        dirInfo.setTotalFileSize(fileSize);
        //设置块的数量
        dirInfo.setTotalBlockNum(Long.valueOf(fileInfo.getBlockCount()));
        //设置小文件（文件大小小于64MB则为小文件）
        Long smallFileNum = fileSize < Constants.SMALL_FILE_BYTES ? 1L : 0L;
        dirInfo.setTotalSmallFileNum(smallFileNum);
        //设置空文件数量（文件大小为0就记一个空文件）
        Long emptyFileNum = fileSize == 0L ? 1L : 0L;
        dirInfo.setTotalEmptyFileNum(emptyFileNum);
        //设置修改时间
        dirInfo.setModificationTime(fileInfo.getModificationTime());
        //设置访问时间
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
     * 对目录信息分析并赋值
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
        //设置日期
        dirInfo.setDayTime(dayTime);
        //设置集群
        dirInfo.setCluster(clusterName);
        //计算平均数
        DecimalFormat df = new DecimalFormat("0.00");
        double avgSize = 0.0D;
        if (dirInfo.getTotalFileNum() > 0L) {
            avgSize = Double.valueOf(df.format(dirInfo.getTotalFileSize() / dirInfo.getTotalFileNum()));
        }
        //设置平均数
        dirInfo.setAvgFileSize(avgSize);

        String path = dirInfo.getPath();
        //设置目录深度
        Integer index = !StringUtils.isBlank(path) && !"/".equals(path) ? dirInfo.getPath().split("/").length : 1;
        dirInfo.setPathIndex(index);

        //设置热度
        dirInfo.setTemperature(DataTempType.WARM.getIndex());

        String tenantKey = RegexUtils.matchTenantKey(path);
        if (tenantMap.containsKey(tenantKey)) {
            TenantInfo tenantInfo = (TenantInfo) tenantMap.get(tenantKey);
            dirInfo.setType(tenantInfo.getType().getIndex());
            dirInfo.setTypeValue(tenantInfo.getTypeValue());
            dirInfo.setTenant(tenantInfo.getName());
            return dirInfo;
        } else {
            //根据路径匹配数据库或者表
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
                    //批量插入10000
                    log.info("mysql batch insert of size = 10000");
                    jdbcUtils.executeBatch("INSERT INTO sdps_hdfs_db_table (cluster, cluster_id, db_name, table_name, type, category, path, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?, now(), now())", dbTableArgs,new Properties());
                    dbTableArgs = new ArrayList();
                }
            }
        }
        log.info("Mysql开始批量插入表[tb_db_table] size={}", dbTableArgs.size());
        //不为空则插入
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
     * 判断路径
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
//        //设置虚拟DNS
//        Properties properties = new Properties();
//        properties.setProperty("master", "10.1.3.24");
//        properties.setProperty("slave1", "10.1.3.25");
//        properties.setProperty("slave2", "10.1.3.26");
//        JavaHost.updateVirtualDns(properties);
//        JavaHost.printAllVirtualDns();
//        //设置虚拟DNS

        //准备hbase配置参数
        final Configuration hbaseConf = HBaseConfiguration.create();

        Boolean isEnablekerberos = (Boolean) ((Map) bcMap.getValue()).get("isEnablekerberos");
        //kerberos相关变量
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
            //临时目录
            String tempDir = (String) ((Map) bcMap.getValue()).get("tempDir");

            //其他变量、Principal、keytab名称、临时+keytab名称
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

            log.info("打印参数:");
            log.info("krb5File:"+krb5File);
            log.info("hbasePrincipalName:"+hbasePrincipalName);
            log.info("hbaseUserkeytabFile:"+hbaseUserkeytabFile);
            log.info("hbaseServerPrincipal:"+hbaseServerPrincipal);
            log.info("hbaseServerKeytabFile:"+hbaseServerKeytabFile);
            log.info("phoenixQueryserverPrincipal:"+phoenixQueryserverPrincipal);
            log.info("phoenixServerKeytabFile:"+phoenixQueryserverKeytabFile);
            log.info("打印参数:=================");

            //下载keytab到临时目录
            HashMap hdfsConfMap = (HashMap) ((Map) bcMap.getValue()).get("hdfsConfMap");
            Configuration hdfsConf = new Configuration();
            for (Object obj : hdfsConfMap.entrySet()) {
                hdfsConf.set(String.valueOf(((Map.Entry) obj).getKey()), String.valueOf(((Map.Entry) obj).getValue()));
            }
            //准备临时目录
            File tempFile = new File(tempDir);
            if(!tempFile.exists()){
                tempFile.mkdir();
            }

            try {
                FileUtils.write(new File(krb5File),krb5FileContext,"UTF-8");
            } catch (Exception e) {
                log.error("生成krb5文件异常:",e);
            }
            try {
                Base64Util.convertStrToFile(hbaseUserkeytabContext,hbaseUserkeytabFile);
            } catch (Exception e) {
                log.error("生成hbase用户keytab文件异常:",e);
            }
            try {
                Base64Util.convertStrToFile(hbaseServerKeytabContent,hbaseServerKeytabFile);
            } catch (Exception e) {
                log.error("生成hbase服务keytab文件异常:",e);
            }
            try {
                Base64Util.convertStrToFile(phoenixQueryserverKeytabContent,phoenixQueryserverKeytabFile);
            } catch (Exception e) {
                log.error("生成Phoenix服务keytab文件异常:",e);
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
        log.info("使用phoenix插入数据,执行sql;phoenixUrl={}, tableName={}, sql={}, 开始时间={}", new Object[]{phoenixUrl, phoneixTableName, insertSQL, startTime});
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
                    log.info("开始使用phoenix插入hbase数据,数据量={},开始时间={}", 10000, beginTime);
                    //判断是否开启Kerberos
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
                    log.info("使用phoenix完成1000条数据插入hbase数据,数据量={},结束时间={},耗时={} 秒", new Object[]{10000, endTime, (endTime - beginTime) / 1000L});
                    //清空ArrayList
                    paramsList = new ArrayList();
                }
            }
        }
        //判断是否开启Kerberos
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
        log.info("完成使用phoenix插入hbase数据 tableName={}, count={}, 结束时间={}, 耗时={}秒", new Object[]{phoneixTableName, count, finishTime, (finishTime - startTime) / 1000L});

    }

    /**
     * 获取插入的参数
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
     * 设置空目录信息
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
