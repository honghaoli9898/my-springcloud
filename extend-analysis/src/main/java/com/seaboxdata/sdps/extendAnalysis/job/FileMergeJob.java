package com.seaboxdata.sdps.extendAnalysis.job;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.Base64Util;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeDataInfo;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.extendAnalysis.common.Constants;
import com.seaboxdata.sdps.extendAnalysis.common.cmd.BaseJobCmd;
import com.seaboxdata.sdps.extendAnalysis.entity.ClusterHostConf;
import com.seaboxdata.sdps.extendAnalysis.entity.HdfsDirPathInfo;
import com.seaboxdata.sdps.extendAnalysis.mapper.*;
import com.seaboxdata.sdps.extendAnalysis.merge.*;
import com.seaboxdata.sdps.extendAnalysis.utils.HdfsUtil;
import com.seaboxdata.sdps.extendAnalysis.utils.HdfsUtils;
import io.leopard.javahost.JavaHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.*;

@Slf4j
public class FileMergeJob {

    private HdfsUtil hdfsUtil;
    public static void main(String[] args) {
        Properties properties = BaseJobCmd.init(args);
        //合并文件的源目录
        String sourcePath = properties.getProperty(Constants.HDFS_MERGE_DIR_PATH);
        String[] paths = sourcePath.split(",");
        for (String path : paths) {
            runJob(properties,path);
        }
    }

    /**
     * 合并HDFS小文件入口
     * @param properties
     * @param path
     */
    private static void runJob(Properties properties,String path){
        FileSystem fs = null;
        try {
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

            //sdps任务作业映射ID
            String sdpsJobId = properties.getProperty(Constants.SDPS_JOB_ID);
//            //sdps作业提交ID
//            String sdpsSubmitId = properties.getProperty(Constants.SDPS_MERGE_SUBMIT_ID);
            //sdps作业提交ID
            String sdpsMergeDataIdPathJson = properties.getProperty(Constants.SDPS_MERGE_DATA_ID_PATH_JSON);
            ApplicationContext beanConf = new ClassPathXmlApplicationContext("spring-container.xml");
            SdpsTaskInfoMapper sdpsTaskInfoMapper = beanConf.getBean(SdpsTaskInfoMapper.class);
            SdpsMergeDataInfoMapper sdpsMergeDataInfoMapper = beanConf.getBean(SdpsMergeDataInfoMapper.class);

            //集群ID
            String clusterId = properties.getProperty(Constants.CLUSTER_ID);

            //合并文件的文件压缩格式，如LZO、GZIP
            String confCodec = properties.getProperty(Constants.HDFS_MERGE_FILE_CODEC);
            //作业合并文件的文件格式，如ORC、AVRO
            String confFormat = properties.getProperty(Constants.HDFS_MERGE_FILE_FORMAT);
            //合并临时目录
            String mergerTempPath = Constants.MERGE_TEMP_PATH;
            String clusterConf = properties.getProperty(Constants.CLUSTER_CONF_API);
            Configuration hdfsConf = HdfsUtils.loadConfFromApi(clusterConf);

            String downloadKrb5FileApi = properties.getProperty(Constants.DOWNLOAD_KRB5_API);

            //查询集群是否开启kerberos
            Boolean isEnablekerberos = Boolean.FALSE;
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-container.xml");
            SdpsClusterMapper sdpsClusterMapper = applicationContext.getBean(SdpsClusterMapper.class);
            Boolean isClusterEnablekerberos = sdpsClusterMapper.selectById(Integer.valueOf(clusterId)).getKerberos();
            SysGlobalArgsMapper sysGlobalArgsMapper = applicationContext.getBean(SysGlobalArgsMapper.class);
            Boolean isDBConfEnablekerberos = Boolean.valueOf(sysGlobalArgsMapper.selectOne(
                    new QueryWrapper<SysGlobalArgs>().eq("arg_type","kerberos").eq("arg_key", "enable")).getArgValue());

            if (isClusterEnablekerberos && isDBConfEnablekerberos) {
                fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf);
            }else {
                fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf,"hdfs");
            }

            if (isClusterEnablekerberos && isDBConfEnablekerberos) {
                isEnablekerberos = Boolean.TRUE;
                //临时目录
                String tempDir = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
                        .eq("arg_type","kerberos").eq("arg_key", "sparkTaskTempDir")).getArgValue();
//                //hdfs上keytab路径
//                String hdfsKeytabPath = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
//             kibana           .eq("arg_type","kerberos").eq("arg_key", "hdfsKeytabPath")).getArgValue();

                //krb5File名
                String krb5File = tempDir.concat("/").concat("krb5.conf");
                //krb5File
                HttpResponse httpResponse = HttpRequest.get(downloadKrb5FileApi).execute();
                String krb5FileContext = httpResponse.body();

                //server_keytab表Mapper
                SdpServerKeytabMapper sdpServerKeytabMapper = applicationContext.getBean(SdpServerKeytabMapper.class);
                QueryWrapper<SdpServerKeytab> hdfsUserQueryWrapper = new QueryWrapper<SdpServerKeytab>()
                        .eq("principal_type","USER")
                        .eq("local_username","hdfs");
                SdpServerKeytab hdfsUserSdpServerKeytab = sdpServerKeytabMapper.selectOne(hdfsUserQueryWrapper);
                //hdfs用户主体
                String hdfsUserPrincipalName = hdfsUserSdpServerKeytab.getPrincipalName();
                //hdfs的keytab文件内容
                String hdfsUserKeytabContent = hdfsUserSdpServerKeytab.getKeytabContent();
                //hdfs的keytab文件名称
                String hdfsUserKeytabFileName = hdfsUserSdpServerKeytab.getKeytabFileName();
                String hdfsUserkeytabFile = tempDir.concat("/").concat(hdfsUserKeytabFileName);

                log.info("tempDir:"+tempDir);
                log.info("krb5File:"+krb5File);
                log.info("hdfsUserPrincipalName:"+hdfsUserPrincipalName);
                log.info("hdfsUserKeytabFileName:"+hdfsUserKeytabFileName);


                //下载keytab,krb5 到临时目录
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
                    Base64Util.convertStrToFile(hdfsUserKeytabContent,hdfsUserkeytabFile);
                } catch (Exception e) {
                    log.error("生成hdfs用户keytab文件异常:",e);
                }
//                String defaultFS = hdfsConf.get("fs.defaultFS");
//                fs.copyToLocalFile(false,
//                        new Path(defaultFS.concat(hdfsKeytabPath).concat("/").concat(krb5FileName)),
//                        new Path(tempDir.concat("/").concat(krb5FileName)),
//                        true);
//                fs.copyToLocalFile(false,
//                        new Path(defaultFS.concat(hdfsKeytabPath).concat("/").concat(hdfsUserKeytab)),
//                        new Path(tempDir.concat("/").concat(hdfsUserKeytab)),
//                        true);
                HdfsUtil hdfsUtil = new HdfsUtil(Integer.valueOf(clusterId), hdfsConf,
                        krb5File,
                        hdfsUserPrincipalName,
                        hdfsUserkeytabFile,
                        isClusterEnablekerberos,
                        isDBConfEnablekerberos
                        );
                fs = hdfsUtil.getFs();

            }

            FileManager fileManager = new FileManager();
            //获取目录路径
            List<DirPathInfo> dirList = fileManager.getDirPathInfoList(fs, path, confCodec, confFormat, mergerTempPath);

            if (!CollectionUtils.isEmpty(dirList)) {
                String tmpPath = fileManager.getDirPathInfo(fs, path, confCodec, confFormat, false, mergerTempPath).getTargetPath();
                log.info("完成获取文件合并TMP路径 source={}, tmp={}", new Object[]{path, tmpPath});
                //删除临时文件
                deleteTmpPath(tmpPath, fs);
                //spark 合并文件
                Map<String, PermissionInfo> permissionInfoMap = mergeFile(dirList, properties, fs,sdpsTaskInfoMapper,sdpsJobId);
                //删除合并的文件
                fileManager.deleteCombinedFiles(dirList, fs);
                //替换文件
                replaceFile(tmpPath, fs, permissionInfoMap,mergerTempPath);


                //更新合并文件数据信息表
                Date date = new Date();
                Map<String,String> sdpsMergeDataIdPathMap = JSONObject.parseObject(sdpsMergeDataIdPathJson, Map.class);
                for (Map.Entry<String, String> entry : sdpsMergeDataIdPathMap.entrySet()) {
                    HdfsDirPathInfo hdfsDirPathInfo = HdfsUtils.getHdfsDirAndSmallFileInfo(fs, entry.getValue(), true);
                    SdpsMergeDataInfo mergeDataInfo = SdpsMergeDataInfo.builder()
                            .id(Long.valueOf(entry.getKey()))
                            .mergeAfterTotalFileNum(hdfsDirPathInfo.getTotalFileNum())
                            .mergeAfterTotalFileSize(hdfsDirPathInfo.getBytes())
                            .mergeAfterTotalFileBlockSize(hdfsDirPathInfo.getTotalBlockBytes())
                            .mergeAfterTotalSmallFileNum(hdfsDirPathInfo.getTotalSmallFileNum())
                            .updateTime(date)
                            .build();
                    int updateCount = sdpsMergeDataInfoMapper.updateById(mergeDataInfo);
                    if(updateCount > 0){
                        log.info("合并小文件,成功更新合并文件数据信息表数据"+mergeDataInfo.toString());
                    }
                }

            }

        } catch (Exception e) {
            log.error("合并小文件异常:",e);
            System.exit(1);
        }finally {
            try {
                if(fs !=  null){
                    fs.close();
                }
            }catch (Exception e){
                log.error("HDFS FileSystem 关闭异常:",e);
                System.exit(1);
            }
        }

    }

    /**
     * 删除临时目录
     * @param targetPath
     * @param fs
     */
    private static void deleteTmpPath(String targetPath, FileSystem fs) {
        Path path = new Path(targetPath);
        try {
            if (fs.exists(path)) {
                fs.delete(new Path(targetPath), true);
            }
        } catch (IOException e) {
            log.error("删除HDFS临时目录异常:", e);
            System.exit(1);
        }

    }


    /**
     * Spark 合并文件
     * @param dirList
     * @param parameters
     * @param fs
     * @return
     * @throws Exception
     */
    private static Map<String, PermissionInfo> mergeFile(List<DirPathInfo> dirList, Properties parameters, FileSystem fs,
                                                         SdpsTaskInfoMapper sdpsTaskInfoMapper,String sdpsJobId) throws Exception {
        FileManager fileManager = new FileManager();
        Map<String, PermissionInfo> permissionInfoMap = new HashMap();

        SparkConf sparkConf = new SparkConf();
        for (Map.Entry<String, String> entry : fs.getConf()) {
            sparkConf.set(entry.getKey(), entry.getValue());
        }

        SparkSession spark = SparkSession.builder()
                .appName("SmallFileMerge-Job")
                .master("local[*]")
                .config("spark.hadoop.avro.mapred.ignore.inputs.without.extension", "false")
                .config(sparkConf)
                .getOrCreate();

        //更新任务表数据
        SparkContext sparkContext = spark.sparkContext();
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
        int updateCount = sdpsTaskInfoMapper.updateById(sdpsTaskInfo);
        if(updateCount > 0){
            log.info("成功更新任务表数据:"+sdpsTaskInfo.toString());
        }


        for (DirPathInfo dirInfo : dirList) {
            if (!CollectionUtils.isEmpty(dirInfo.getFilePaths())) {
                log.info("begin to merge file of dirInfo={}", dirInfo);
                String targetPath = dirInfo.getTargetPath();
                CodecType codec = dirInfo.getCodecType();
                FormatType format = dirInfo.getFormatType();
                String sparkCodec = codec.getSparkCodec(format);
                String dirPath = (new Path(dirInfo.getDirPath())).toUri().getPath();
                //权限信息Map
                permissionInfoMap.put(dirPath, fileManager.getRandomFilePermission(parameters, dirInfo, fs));
                //默认合并文件阈值
                long mergedThreshold = FileManager.DEFAULT_MERGED_FILE_THRESHOLD;
                Integer mergedFileNum = Math.toIntExact(dirInfo.getBytes() / mergedThreshold + 1L);
                log.info("merge file mergedThreshold={}, mergedFileNum={}", mergedThreshold, mergedFileNum);

                switch (format) {
                    case TEXT:
                        spark.read().textFile(dirInfo.getFilePathSeq()).coalesce(mergedFileNum).write().option("compression", sparkCodec).text(targetPath);
                        break;
                    case ORC:
                        spark.read().format("org.apache.spark.sql.execution.datasources.orc.OrcFileFormat").orc(dirInfo.getFilePathSeq()).coalesce(mergedFileNum).write().option("compression", sparkCodec).orc(targetPath);
                        break;
                    case AVRO:
                        spark.read().format("com.databricks.spark.avro").load(dirInfo.getFilePathSeq()).coalesce(mergedFileNum).write().format("com.databricks.spark.avro").save(targetPath);
                        break;
                    case PARQUET:
                        spark.read().parquet(dirInfo.getFilePathSeq()).coalesce(mergedFileNum).write().option("compression", sparkCodec).parquet(targetPath);
                        break;
                    case SEQ:
                        Path randomPath = fileManager.getRandomPath(dirInfo);
                        SequenceFile.Reader reader = null;
                        try {
                            reader = new SequenceFile.Reader(fs.getConf(), new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(randomPath)});
                            log.info("finish get seq file key class={}, value class={}", reader.getKeyClass(), reader.getValueClass());
                            JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
                            sc.sequenceFile(dirInfo.getFilePathNames(), reader.getKeyClass(), reader.getValueClass()).map((tuple) -> {
                                return new Tuple2<String,String>(tuple._1.toString(), tuple._2.toString());
                            }).coalesce(mergedFileNum).saveAsObjectFile(targetPath);

                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(1);
                        } finally {
                            try {
                                if (reader != null) {
                                    reader.close();
                                }
                            } catch (Exception e) {
                                log.error("合并文件异常:", e);
                                System.exit(1);
                            }
                        }
                        break;
                    default:
                        throw new Exception("file format type of " + format + " is not support o");
                }
            }
        }

        return permissionInfoMap;
    }

    /**
     * 替换文件
     * @param targetPath
     * @param fs
     * @param permissionInfoMap
     */
    private static void replaceFile(String targetPath, FileSystem fs, Map<String, PermissionInfo> permissionInfoMap ,String mergerTempPath) {
        try {
            RemoteIterator iterator = fs.listFiles(new Path(targetPath), true);
            while (iterator.hasNext()) {
                LocatedFileStatus status = (LocatedFileStatus) iterator.next();
                Path filePath = status.getPath();
                if (filePath.getName().startsWith("part-")) {
                    Path target = new Path(StringUtils.substringAfter(filePath.toUri().getPath(), mergerTempPath));
                    Path parent = target.getParent();
                    PermissionInfo permission = (PermissionInfo) permissionInfoMap.get(parent.toString());
                    log.info("begin to replace file of filePath={}, target={}, parent={}, permission={}", new Object[]{filePath.toString(), target, parent.toString(), permission});
                    fs.rename(filePath, parent);
                    if (permission != null) {
                        fs.setOwner(target, permission.getOwner(), permission.getGroup());
                        fs.setPermission(target, permission.getPermission());
                    }
                }
            }
        } catch (IOException e) {
            log.error("替换文件异常:", e);
            System.exit(1);
        }
    }

}
