package com.seaboxdata.sdps.job.executor.service.jobhandler.analysis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.constant.TaskConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.job.core.context.XxlJobHelper;
import com.seaboxdata.sdps.job.core.handler.annotation.XxlJob;
import com.seaboxdata.sdps.job.executor.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.job.executor.feign.SeaboxProxyFegin;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpServerKeytabMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsTaskInfoMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SysGlobalArgsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class AnalysisHdfsMetaDataDispatch {
    private static Logger logger = LoggerFactory.getLogger(AnalysisHdfsMetaDataDispatch.class);

    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

    @Autowired
    SeaboxProxyFegin seaboxProxyFegin;

    @Autowired
    SdpsServerInfoMapper sdpsServerInfoMapper;

    @Autowired
    SysGlobalArgsMapper sysGlobalArgsMapper;

    @Autowired
    SdpsTaskInfoMapper sdpsTaskInfoMapper;

    @Autowired
    SdpServerKeytabMapper sdpServerKeytabMapper;
    /**
     * ??????HDFS?????????????????????????????????????????????
     */
    @XxlJob("AnalysisHdfsMetaDataDispatch")
    public void executeSeaboxAnalysisHdfsMetaData() {

//        Integer clusterId = Integer.valueOf(XxlJobHelper.getJobParam());
        String jobParamJson = XxlJobHelper.getJobParam();
        DispatchJobRequest dispatchJobRequest = JSONObject.parseObject(jobParamJson, DispatchJobRequest.class);
        Integer clusterId = dispatchJobRequest.getClusterId();
        String jobId = dispatchJobRequest.getSdpsJobId();

        //??????????????????????????????
        Boolean isFetch = seaboxProxyFegin.execFetchAndExtractHdfsMetaData(clusterId);
        logger.info("??????HDFS??????????????????HDFS?????????:" + isFetch);
        if (isFetch) {

            //??????????????????
            SdpsCluster sdpsCluster = bigdataCommonFegin.querySdpsClusterById(clusterId);
            //?????????#??????ID
            String clusterNameAndID = sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();

            //???????????????
            Boolean isEnablekerberos = Boolean.FALSE;
            //??????????????????kerberos
            Boolean isClusterEnablekerberos = sdpsCluster.getKerberos();
            Boolean isDBConfEnablekerberos = Boolean.valueOf(sysGlobalArgsMapper.selectOne(
                    new QueryWrapper<SysGlobalArgs>()
                            .eq("arg_type","kerberos")
                            .eq("arg_key", "enable")).getArgValue());
            if(isClusterEnablekerberos && isDBConfEnablekerberos){
                isEnablekerberos = Boolean.TRUE;
            }

            //??????hdfs????????????
            ArrayList<String> hdfsConfList = new ArrayList<>();
            hdfsConfList.add("core-site");
            String hdfsConfJson = seaboxProxyFegin.getServerConfByConfName(sdpsCluster.getClusterId(), "HDFS", hdfsConfList);
            Map hdfsConfMap = JSON.parseObject(hdfsConfJson, Map.class);
            String hdfsPathPrefix = (String) hdfsConfMap.get("fs.defaultFS");
            //??????hbase????????????
            ArrayList<String> hbaseConfList = new ArrayList<>();
            hbaseConfList.add("hbase-env");
            hbaseConfList.add("hbase-site");
            String hbaseConfJson = seaboxProxyFegin.getServerConfByConfName(sdpsCluster.getClusterId(), "HBASE", hbaseConfList);
            Map hbaseConfMap = JSON.parseObject(hbaseConfJson, Map.class);
            String hbaseZkQuorum = (String) hbaseConfMap.get("hbase.zookeeper.quorum");
            String hbaseZkPort = (String) hbaseConfMap.get("hbase.zookeeper.property.clientPort");
            String hbaseZnode = (String) hbaseConfMap.get("zookeeper.znode.parent");

            //?????????????????????????????????
            List<TaskConfig> configList = bigdataCommonFegin.getTaskConfByClusterTypeAndTaskType(TaskConstants.TASK_TYPE_HDFS_METADATA_ANALYSIS, ClusterConstants.CLUSTER_TYPE_SEABOX);
            Map<String, String> confMaps = new HashMap<>();
            for (TaskConfig taskConfig : configList) {
                confMaps.put(taskConfig.getArgKey(), taskConfig.getArgValue());
            }

            StringBuffer shellSb = new StringBuffer();
            //??????????????????
            String taskSavePath = confMaps.get(TaskConstants.TASK_KEY_TASK_SAVE_PATH);
            //??????(???????????????????????????????????????????????????)
            Date date = new Date();
            SimpleDateFormat sdfDayDf = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYYMMDD);
            String dayStr = sdfDayDf.format(DateUtils.addDays(new Date(), -1));

            SimpleDateFormat sdfDay = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYY_MM_DD);
            String day = sdfDay.format(DateUtils.addDays(date, -1));
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISS);
            String dayTime = sdf.format(DateUtils.addDays(date, -1));

            //shell?????????????????????:??????+?????????
            String taskFilePath = taskSavePath
                    .concat("/")
                    .concat(clusterNameAndID)
                    .concat("/")
                    .concat(day)
                    .concat("/");

            //??????kerberos,????????????kerberos
            if(isEnablekerberos){
                shellSb.append("kdestroy").append("\n");
            }

            String filePrefix = TaskConstants.TASK_TYPE_HDFS_METADATA_ANALYSIS.concat("_").concat(dayTime);

            //??????????????????jar?????????????????????1???
            shellSb.append("touch").append(" ").append(taskFilePath).append(filePrefix).append(".log").append("\n");
            shellSb.append("tail -f").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" 2>&1 &").append("\n");
            shellSb.append("tailpid=\\$!").append("\n");

            //??????shell????????????
//        shellSb.append(TaskConstants.SHELL_SCRIPT_PREFIX).append("\n");
            //?????????spark-submit?????????
            shellSb.append(confMaps.get(TaskConstants.SPARK_TASK_KEY_SUBMIT_PATH)).append(" ");
            //?????????spark-??????????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.TASK_KEY_NAME).append(" ")
                    .append(confMaps.get(TaskConstants.TASK_KEY_NAME)).append(" ");
            //?????????spark?????????????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CLASS).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_CLASS)).append(" ");
            //?????????spark?????????????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_MASTER).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_MASTER)).append(" ");
            //?????????spark?????????????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_QUEUE).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_QUEUE)).append(" ");
            //?????????spark?????????????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_DEPLOY_MODE).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_DEPLOY_MODE)).append(" ");
            //?????????driver?????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_DRIVER_MEMORY).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_DRIVER_MEMORY)).append(" ");
            //???????????????executor?????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_NUM_EXECUTORS).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_NUM_EXECUTORS)).append(" ");
            //???????????????executor????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_EXECUTOR_MEMORY).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_EXECUTOR_MEMORY)).append(" ");
            //???????????????executor????????????
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_EXECUTOR_CORES).append(" ")
                    .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_EXECUTOR_CORES)).append(" ");
            //??????kerberos,??????spark??????????????????
            if(isEnablekerberos){
                String sparkUserKerberosPrincipal = sdpServerKeytabMapper.selectOne(
                    new QueryWrapper<SdpServerKeytab>()
                        .eq("principal_type","USER")
                        .eq("local_username","spark")
                ).getPrincipalName();
                String sparkUserKeytabFile = sdpServerKeytabMapper.selectOne(
                        new QueryWrapper<SdpServerKeytab>()
                                .eq("principal_type","USER")
                                .eq("local_username","spark")
                ).getKeytabFilePath();
                //??????spark kerberos???principal???keytab??????
                shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CONF).append(" ")
                        .append("spark.yarn.principal=").append(sparkUserKerberosPrincipal).append(" ");
                shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CONF).append(" ")
                        .append("spark.yarn.keytab=").append(sparkUserKeytabFile).append(" ");

//                //??????krb5File
//                String krb5File = sysGlobalArgsMapper.selectOne(new QueryWrapper<SysGlobalArgs>()
//                    .eq("arg_type","kerberos").eq("arg_key", "krb5")).getArgValue();
//                String hbaseUserKeytab = (String) hbaseConfMap.get("hbase_user_keytab");
//                hbaseUserKeytab = hbaseUserKeytab
//                        .substring(hbaseUserKeytab.lastIndexOf("/")+1);
//                hbaseUserKeytab = keytabPath.concat("/").concat(String.valueOf(sdpsCluster.getClusterId())).concat(".").concat(hbaseUserKeytab);
//                //hbase???keytab??????
//                String hbaseServerKeytab = (String) hbaseConfMap.get("hbase.master.keytab.file");
//                hbaseServerKeytab = hbaseServerKeytab
//                        .substring(hbaseServerKeytab.lastIndexOf("/")+1);
//                hbaseServerKeytab = keytabPath.concat("/").concat(String.valueOf(sdpsCluster.getClusterId())).concat(".").concat(hbaseServerKeytab);
//                //phoenix??????keytab??????
//                String phoenixQueryServerKeytab = (String) hbaseConfMap.get("phoenix.queryserver.keytab.file");
//                phoenixQueryServerKeytab = phoenixQueryServerKeytab
//                        .substring(phoenixQueryServerKeytab.lastIndexOf("/")+1);
//                phoenixQueryServerKeytab = keytabPath.concat("/").concat(String.valueOf(sdpsCluster.getClusterId())).concat(".").concat(phoenixQueryServerKeytab);
//                shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_FILES).append(" ")
//                        .append(hbaseUserKeytab.concat(",")
//                                .concat(hbaseServerKeytab).concat(",")
//                                .concat(phoenixQueryServerKeytab)
//                        ).append(" ");
            }

            //?????????HDFS???????????????jar????????????
            shellSb.append(confMaps.get(TaskConstants.SPARK_TASK_KEY_JAR_PATH)).append(" ");


            //HDFS?????????????????????????????????HDFS??????
            String imageFilePath = confMaps.get(TaskConstants.HDFS_ANALYSIS_HDFS_IMAGE_FILE_PATH);
            //??????????????????Header
            String isHeader = confMaps.get(TaskConstants.HDFS_ANALYSIS_EXTRACT_FILE_ISHEADER);


            StringBuffer endShell = new StringBuffer();
            SdpsServerInfo sdpsServerInfo = null;
            String imageFileExtractPath = "";



            //??????????????????,????????????API?????????HOST???IP
            SpringClientFactory springClientFactory = SpringUtil.getBean(SpringClientFactory.class);
            ILoadBalancer loadBalancerSeabox = springClientFactory.getLoadBalancer(ServiceNameConstants.SEABOX_PROXY_SERVICE);
            List<Server> serversSeabox = loadBalancerSeabox.getReachableServers();
            String seaboxProxyHostIp = serversSeabox.get(0).getHostPort();
            String clusterConfApiUrl = "http://".concat(seaboxProxyHostIp).concat("/seabox/getServerConfByConfName");
            String downloadKrb5Api = "http://".concat(seaboxProxyHostIp).concat("/seaboxKeytab/downloadKrb5");

            String itemCenterHostIp = null;
            try {
                ILoadBalancer loadBalancerItemCenter = springClientFactory.getLoadBalancer(ServiceNameConstants.ITEM_CENTER);
                List<Server> serversItemCenter = loadBalancerItemCenter.getReachableServers();
                itemCenterHostIp = serversItemCenter.get(0).getHostPort();
            }catch (Exception e){
                log.error("item-center????????????,?????????item-center??????????????????");
            }

            endShell.append(shellSb)
                    .append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_CLUSTER_NAME).append(" ")
                    .append("\'").append(clusterNameAndID).append("\'").append(" ");
            //????????????ID
            endShell.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_ID).append(" ")
                    .append("\'").append(sdpsCluster.getClusterId()).append("\'").append(" ");

            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_CLUSTER_TYPE).append(" ")
                    .append("\'").append(ClusterConstants.CLUSTER_TYPE_SEABOX).append("\'").append(" ");

            //??????????????????
            String clusterHostConf = StringUtils.replace(sdpsCluster.getClusterHostConf(), "\"", "\\\"");
            endShell.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_IP_HOST).append(" ")
                    .append("\'").append(clusterHostConf).append("\'").append(" ");

            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_JDBC).append(" ")
                    .append("\'").append(confMaps.get(TaskConstants.HDFS_ANALYSIS_TASK_KEY_MYSQL_JDBC_URL)).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_DAYTIME).append(" ")
                    .append("\'").append(dayStr).append("\'").append(" ");
            imageFileExtractPath = hdfsPathPrefix + imageFilePath + "/" + clusterNameAndID + "/" + TaskConstants.HDFS_IMAGE_EXTRACT_FILE_PREFIX + day;
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_IMAGEFILE_PATH).append(" ")
                    .append("\'").append(imageFileExtractPath).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_IMAGEFILE_TYPE).append(" ")
                    .append("\'").append(TaskConstants.HDFS_IMAGE_EXTRACT_FILE_TYPE_CSV).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_IMAGE_SEPARATOR).append(" ")
                    .append("\'").append(TaskConstants.HDFS_IMAGE_EXTRACT_FILE_SEPARATOR).append("\'").append(" ");
            //extract_file??????????????????
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_IS_HEADER).append(" ")
                    .append("\'").append(isHeader).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_ZK_QUORUM).append(" ")
                    .append("\'").append(hbaseZkQuorum).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_ZK_PORT).append(" ")
                    .append("\'").append(hbaseZkPort).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PARAM_ZNODE).append(" ")
                    .append("\'").append(hbaseZnode).append("\'").append(" ");
            //??????????????????API???ip:port
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PROJECT_RELATION_URL).append(" ")
                    .append("\'").append(itemCenterHostIp).append("\'").append(" ");
            //??????????????????API????????????UserId,UserName
            String projectUserId = confMaps.get(TaskConstants.HDFS_ANALYSIS_TASK_KEY_PROJECT_RELATION_USER_ID);
            String projectUserName = confMaps.get(TaskConstants.HDFS_ANALYSIS_TASK_KEY_PROJECT_RELATION_USER_NAME);
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PROJECT_USER_ID).append(" ")
                    .append("\'").append(projectUserId).append("\'").append(" ");
            endShell.append("-").append(TaskConstants.ANALYSIS_TASK_PROJECT_USER_NAME).append(" ")
                    .append("\'").append(projectUserName).append("\'").append(" ");

            //??????????????????API
            endShell.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_CONF_API).append(" ")
                    .append("\'").append(clusterConfApiUrl).append("\'").append(" ");

            //??????????????????API
            endShell.append("-").append(TaskConstants.TASK_PARAM_DOWNLOAD_KRB5_API).append(" ")
                    .append("\'").append(downloadKrb5Api).append("\'").append(" ");

            //????????????????????????????????????ID
            endShell.append("-").append(TaskConstants.SDPS_JOB_ID).append(" ")
                    .append("\'").append(jobId).append("\'");

            //??????????????????
            endShell.append(" ").append(">").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" ").append("2>&1").append("\n");

            //??????????????????jar?????????????????????2???
            endShell.append("statue=\\$?").append("\n");
            endShell.append("kill \\$tailpid").append("\n");
            endShell.append("exit \\$statue");

            //????????????
            sdpsServerInfo = sdpsServerInfoMapper
                    .selectOne(new QueryWrapper<SdpsServerInfo>()
                            .eq("user", TaskConstants.ANALYSIS_TASK_USER_SPAKR)
                            .eq("server_id", sdpsCluster.getClusterId())
                            .eq("type", TaskConstants.ANALYSIS_TASK_SPAKR_TASK_TYPE));
            SysGlobalArgs sysGlobalArgs = sysGlobalArgsMapper
                    .selectOne(new QueryWrapper<SysGlobalArgs>()
                            .eq("arg_type", "password")
                            .eq("arg_key", "privateKey"));
            sdpsServerInfo.setPasswd(RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
                    sysGlobalArgs.getArgValue()));

            //?????????????????????
            SdpsTaskInfo sdpsTaskInfo = SdpsTaskInfo.builder()
                    .id(Long.valueOf(jobId))
                    .clusterId(sdpsCluster.getClusterId())
                    .clusterName(sdpsCluster.getClusterName())
                    .userName("hdfs")
                    .yarnQueue(confMaps.get(TaskConstants.SPARK_TASK_KEY_QUEUE))
                    .applicationType(TaskConstants.TASK_TYPE_SPARK)
                    .shellPath(taskFilePath)
                    .shellContext(endShell.toString())
                    .ext0(imageFileExtractPath)
                    .updateTime(new Date())
                    .build();
            int updateCount = sdpsTaskInfoMapper.updateById(sdpsTaskInfo);
            if(updateCount > 0){
                log.info("???????????????,???????????????????????????:"+sdpsTaskInfo.toString());
            }

            try {
                RemoteShellExecutorUtil execMakeDir = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
                execMakeDir.exec("mkdir -p ".concat(taskSavePath).concat("/").concat(clusterNameAndID).concat("/").concat(day));

                RemoteShellExecutorUtil execGenerateShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
                execGenerateShell.exec("echo \"".concat(endShell.toString()).concat("\" > ").concat(taskFilePath).concat(filePrefix).concat(".sh"));

                RemoteShellExecutorUtil execChmod = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
                execChmod.exec("chmod 755 ".concat(taskFilePath).concat(filePrefix).concat(".sh"));

                XxlJobHelper.log("???????????????????????????HDFS???????????????"
                        .concat("[??????ID:").concat(String.valueOf(sdpsCluster.getClusterId()))
                        .concat(";????????????:").concat(sdpsCluster.getClusterName()).concat("]??????:"));

                RemoteShellExecutorUtil execShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
                execShell.exec2("sh ".concat(taskFilePath).concat(filePrefix).concat(".sh"));


            } catch (Exception e) {
                logger.error("??????HDFS???????????????????????????????????????????????????:", e);
                XxlJobHelper.handleFail("??????HDFS?????????????????????");
                e.printStackTrace();
            }
        } else {
            logger.error("??????HDFS??????????????????HDFS???????????????");
            XxlJobHelper.handleFail("??????HDFS?????????????????????:[??????HDFS??????????????????HDFS???????????????]");
        }

//        Result<String> yarnConf = bigdataCommonFegin.getServerConfByConfName(clusterId, "YARN", Arrays.asList(new String[]{"yarn-site"}));



    }
}
