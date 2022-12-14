package com.seaboxdata.sdps.job.executor.service.jobhandler.merge;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.constant.TaskConstants;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeDataInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeSubmitInfo;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.job.core.context.XxlJobHelper;
import com.seaboxdata.sdps.job.core.handler.annotation.XxlJob;
import com.seaboxdata.sdps.job.executor.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class MergeHdfsFileJob {
    private static Logger logger = LoggerFactory.getLogger(MergeHdfsFileJob.class);

    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

    @Autowired
    SdpsServerInfoMapper sdpsServerInfoMapper;

    @Autowired
    SysGlobalArgsMapper sysGlobalArgsMapper;

    @Autowired
    SdpsTaskInfoMapper sdpsTaskInfoMapper;

    @Autowired
    SdpsMergeSubmitInfoMapper sdpsMergeSubmitInfoMapper;

    @Autowired
    SdpsMergeDataInfoMapper sdpsMergeDataInfoMapper;

    @Autowired
    SdpServerKeytabMapper sdpServerKeytabMapper;

    @XxlJob("ExecuteMergeHdfsFile")
    public void executeMergeHdfsFile() {
        String jobParamJson = XxlJobHelper.getJobParam();
        DispatchJobRequest dispatchJobRequest = JSONObject.parseObject(jobParamJson, DispatchJobRequest.class);

        spliceMergeShell(dispatchJobRequest.getClusterId(),
                dispatchJobRequest.getSourcePath(),
                dispatchJobRequest.getCodec(),
                dispatchJobRequest.getFormat(),
                dispatchJobRequest.getUserName(),
                dispatchJobRequest.getSdpsJobId(),
                dispatchJobRequest.getSubmitId(),
                dispatchJobRequest.getMergeDataIdPathMap());

    }

    private Boolean spliceMergeShell(Integer clusterId,String sourcePath,
                                     String codec,String format,
                                     String userName,String jobId,Long submitId,Map<String,String> mergeDataIdPathMap){
        Boolean flag = false;

        //????????????
        SdpsCluster sdpsCluster = bigdataCommonFegin.querySdpsClusterById(clusterId);
        //?????????#??????ID
        String clusterNameID = sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();

        //???????????????
        Boolean isEnablekerberos = Boolean.FALSE;
        //??????????????????kerberos
        Boolean isClusterEnablekerberos = sdpsCluster.getKerberos();
        Boolean isDBConfEnablekerberos = Boolean.valueOf(sysGlobalArgsMapper.selectOne(
                new QueryWrapper<SysGlobalArgs>()
                        .eq("arg_type","kerberos")
                        .eq("arg_key", "enable")).getArgValue());
        if(Objects.nonNull(isClusterEnablekerberos) && Objects.nonNull(isDBConfEnablekerberos)){
            if(isClusterEnablekerberos && isDBConfEnablekerberos){
                isEnablekerberos = Boolean.TRUE;
            }
        }



        //?????????????????????????????????
        List<TaskConfig> configList = bigdataCommonFegin.getTaskConfByClusterTypeAndTaskType(TaskConstants.TASK_TYPE_HDFS_MERGE_FILE, ClusterConstants.CLUSTER_TYPE_SEABOX);
        Map<String, String> confMaps = new HashMap<>();
        for (TaskConfig taskConfig : configList) {
            confMaps.put(taskConfig.getArgKey(), taskConfig.getArgValue());
        }

        StringBuffer shellSb = new StringBuffer();

        //??????????????????
        String taskSavePath = confMaps.get(TaskConstants.TASK_KEY_TASK_SAVE_PATH);
        //??????
        Date date = new Date();
        SimpleDateFormat sdfDay = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYY_MM_DD);
        String day = sdfDay.format(date);
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISS);
        String dayTime = sdf.format(date);
        //shell?????????????????????:??????+?????????
        String taskFilePath = taskSavePath
                .concat("/")
                .concat(clusterNameID)
                .concat("/")
                .concat(day)
                .concat("/");

        //??????kerberos,????????????kerberos
        if(isEnablekerberos){
            shellSb.append("kdestroy").append("\n");
        }

        String filePrefix = TaskConstants.TASK_TYPE_HDFS_MERGE_FILE.concat("_").concat(dayTime);

        //??????????????????jar?????????????????????1???
        shellSb.append("touch").append(" ").append(taskFilePath).append(filePrefix).append(".log").append("\n");
        shellSb.append("tail -f").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" 2>&1 &").append("\n");
        shellSb.append("tailpid=\\$!").append("\n");

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
        }


        //?????????HDFS???????????????jar????????????
        shellSb.append(confMaps.get(TaskConstants.SPARK_TASK_KEY_JAR_PATH)).append(" ");

        //??????jar???????????????ID???
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_ID).append(" ")
                .append("\'").append(clusterId).append("\'").append(" ");
        //??????jar?????????????????????????????????
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_SOURCE_PATH).append(" ")
                .append("\'").append(sourcePath).append("\'").append(" ");
        //??????jar????????????????????????
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_CODEC).append(" ")
                .append("\'").append(codec).append("\'").append(" ");
        //??????jar????????????????????????
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_FORMAT).append(" ")
                .append("\'").append(format).append("\'").append(" ");

        //??????jar?????????????????????IP???
        String clusterHostConf = StringUtils.replace(sdpsCluster.getClusterHostConf(), "\"", "\\\"");
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_IP_HOST).append(" ")
                .append("\'").append(clusterHostConf).append("\'").append(" ");


        //??????????????????,????????????API?????????HOST???IP
        SpringClientFactory springClientFactory = SpringUtil.getBean(SpringClientFactory.class);
        ILoadBalancer loadBalancerSeabox = springClientFactory.getLoadBalancer(ServiceNameConstants.SEABOX_PROXY_SERVICE);
        List<Server> serversSeabox = loadBalancerSeabox.getReachableServers();
        String seaboxProxyHostIp = serversSeabox.get(0).getHostPort();
        String clusterConfApiUrl = "http://".concat(seaboxProxyHostIp).concat("/seabox/getServerConfByConfName");

        //??????????????????API
        String clusterConfApi = clusterConfApiUrl.concat("?clusterId=").concat(String.valueOf(sdpsCluster.getClusterId()))
                .concat("&confStrs=").concat("core-site,hdfs-site,hadoop-env").concat("&serverName=").concat("HDFS");
        if(isEnablekerberos){
            String downloadKrb5Api = "http://".concat(seaboxProxyHostIp).concat("/seaboxKeytab/downloadKrb5");
            //??????????????????API
            shellSb.append("-").append(TaskConstants.TASK_PARAM_DOWNLOAD_KRB5_API).append(" ")
                    .append("\'").append(downloadKrb5Api).append("\'").append(" ");
        }

        //??????jar?????????????????????API?????????
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_CONF_API).append(" ")
                .append("\'").append(clusterConfApi).append("\'").append(" ");

        //??????jar????????????????????????????????????
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_USER).append(" ")
                .append("\'").append(userName).append("\'").append(" ");
        //??????jar???????????????????????????????????????
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_GROUP).append(" ")
                .append("\'").append("hdfs").append("\'").append(" ");
        //????????????????????????????????????ID
        shellSb.append("-").append(TaskConstants.SDPS_JOB_ID).append(" ")
                .append("\'").append(jobId).append("\'").append(" ");
//        //?????????????????????????????????????????????ID
//        shellSb.append("-").append(TaskConstants.SDPS_MERGE_SUBMIT_ID).append(" ")
//                .append("\'").append(submitId).append("\'").append(" ");

        //?????????????????????????????????????????????ID
        String mergeDataIdPathMapJson = StringUtils.replace(JSONObject.toJSONString(mergeDataIdPathMap), "\"", "\\\"");
        shellSb.append("-").append(TaskConstants.SDPS_MERGE_DATA_ID_PATH_JSON).append(" ")
                .append("\'").append(mergeDataIdPathMapJson).append("\'");

        //????????????
        //??????????????????
        shellSb.append(" ").append(">").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" ").append("2>&1").append("\n");

        //??????????????????jar?????????????????????2???
        shellSb.append("statue=\\$?").append("\n");
        shellSb.append("kill \\$tailpid").append("\n");
        shellSb.append("exit \\$statue");

        //????????????
        SdpsServerInfo sdpsServerInfo = sdpsServerInfoMapper
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
                .userName(userName)
                .yarnQueue(confMaps.get(TaskConstants.SPARK_TASK_KEY_QUEUE))
                .applicationType(TaskConstants.TASK_TYPE_SPARK)
                .shellPath(taskFilePath)
                .shellContext(shellSb.toString())
                .updateTime(new Date())
                .build();
        int updateCount = sdpsTaskInfoMapper.updateById(sdpsTaskInfo);
        if(updateCount > 0){
            log.info("?????????????????????,???????????????????????????:"+sdpsTaskInfo.toString());
        }
        //?????????????????????????????????
        SdpsMergeSubmitInfo mergeSubmitInfo = SdpsMergeSubmitInfo.builder()
                .id(submitId)
                .taskId(Long.valueOf(jobId))
                .updateTime(new Date())
                .build();
        int updateMergeSubmitCount = sdpsMergeSubmitInfoMapper.updateById(mergeSubmitInfo);
        if(updateMergeSubmitCount > 0){
            log.info("?????????????????????,?????????????????????????????????????????????:"+mergeSubmitInfo.toString());
        }
        //?????????????????????????????????
        for (String mergeDataInfoId : mergeDataIdPathMap.keySet()) {
            SdpsMergeDataInfo sdpsMergeDataInfo = SdpsMergeDataInfo.builder()
                    .id(Long.valueOf(mergeDataInfoId))
                    .taskId(Long.valueOf(jobId))
                    .updateTime(new Date())
                    .build();
            int updateMergeDataInfoCount = sdpsMergeDataInfoMapper.updateById(sdpsMergeDataInfo);
            if(updateMergeDataInfoCount > 0 ){
                log.info("?????????????????????,?????????????????????????????????????????????:"+sdpsMergeDataInfo.toString());
            }
        }

        try {
            //????????????????????????
            RemoteShellExecutorUtil execMakeDir = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            execMakeDir.exec("mkdir -p ".concat(taskSavePath).concat("/").concat(clusterNameID).concat("/").concat(day));

            //??????????????????shell??????
            RemoteShellExecutorUtil execGenerateShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            execGenerateShell.exec("echo \"".concat(shellSb.toString()).concat("\" > ").concat(taskFilePath).concat(filePrefix).concat(".sh"));

            //??????????????????shell??????
            RemoteShellExecutorUtil execShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            int i = execShell.exec2("sh ".concat(taskFilePath).concat(filePrefix).concat(".sh"));
            if(i == 1){
                XxlJobHelper.handleFail("???????????????????????????");
            }else {
                flag = true;
            }
            XxlJobHelper.log("????????????:",flag.toString());

        }catch (Exception e){
            log.error("??????HDFS??????????????????????????????????????????:", e);
            XxlJobHelper.handleFail("??????HDFS?????????????????????");
        }


        return flag;
    }

}
