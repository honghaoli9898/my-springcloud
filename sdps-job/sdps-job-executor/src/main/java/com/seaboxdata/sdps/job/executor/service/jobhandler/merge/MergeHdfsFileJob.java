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

        //获取集群
        SdpsCluster sdpsCluster = bigdataCommonFegin.querySdpsClusterById(clusterId);
        //集群名#集群ID
        String clusterNameID = sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();

        //设置初始值
        Boolean isEnablekerberos = Boolean.FALSE;
        //查询是否开启kerberos
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



        //获取分析元数据任务参数
        List<TaskConfig> configList = bigdataCommonFegin.getTaskConfByClusterTypeAndTaskType(TaskConstants.TASK_TYPE_HDFS_MERGE_FILE, ClusterConstants.CLUSTER_TYPE_SEABOX);
        Map<String, String> confMaps = new HashMap<>();
        for (TaskConfig taskConfig : configList) {
            confMaps.put(taskConfig.getArgKey(), taskConfig.getArgValue());
        }

        StringBuffer shellSb = new StringBuffer();

        //任务保存路径
        String taskSavePath = confMaps.get(TaskConstants.TASK_KEY_TASK_SAVE_PATH);
        //日期
        Date date = new Date();
        SimpleDateFormat sdfDay = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYY_MM_DD);
        String day = sdfDay.format(date);
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISS);
        String dayTime = sdf.format(date);
        //shell文件及日志文件:路径+文件名
        String taskFilePath = taskSavePath
                .concat("/")
                .concat(clusterNameID)
                .concat("/")
                .concat(day)
                .concat("/");

        //开启kerberos,增加注销kerberos
        if(isEnablekerberos){
            shellSb.append("kdestroy").append("\n");
        }

        String filePrefix = TaskConstants.TASK_TYPE_HDFS_MERGE_FILE.concat("_").concat(dayTime);

        //解决无法拿到jar的返回值【步骤1】
        shellSb.append("touch").append(" ").append(taskFilePath).append(filePrefix).append(".log").append("\n");
        shellSb.append("tail -f").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" 2>&1 &").append("\n");
        shellSb.append("tailpid=\\$!").append("\n");

        //拼接【spark-submit路径】
        shellSb.append(confMaps.get(TaskConstants.SPARK_TASK_KEY_SUBMIT_PATH)).append(" ");
        //拼接【spark-提交任务名】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.TASK_KEY_NAME).append(" ")
                .append(confMaps.get(TaskConstants.TASK_KEY_NAME)).append(" ");
        //拼接【spark执行任务主类】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CLASS).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_CLASS)).append(" ");
        //拼接【spark提交任务方式】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_MASTER).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_MASTER)).append(" ");
        //拼接【spark提交任务队列】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_QUEUE).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_QUEUE)).append(" ");
        //拼接【spark任务部署方式】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_DEPLOY_MODE).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_DEPLOY_MODE)).append(" ");
        //拼接【driver内存】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_DRIVER_MEMORY).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_DRIVER_MEMORY)).append(" ");
        //拼接【启动executor数量】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_NUM_EXECUTORS).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_NUM_EXECUTORS)).append(" ");
        //拼接【每个executor的内存】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_EXECUTOR_MEMORY).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_EXECUTOR_MEMORY)).append(" ");
        //拼接【每个executor的核数】
        shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_EXECUTOR_CORES).append(" ")
                .append(confMaps.get(TaskConstants.SPARK_TASK_KEY_EXECUTOR_CORES)).append(" ");

        //开启kerberos,增加spark提交任务参数
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
            //增加spark kerberos的principal和keytab参数
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CONF).append(" ")
                    .append("spark.yarn.principal=").append(sparkUserKerberosPrincipal).append(" ");
            shellSb.append(TaskConstants.SPARK_SUBMIT_PARAM_PREFIX).append(TaskConstants.SPARK_TASK_KEY_CONF).append(" ")
                    .append("spark.yarn.keytab=").append(sparkUserKeytabFile).append(" ");
        }


        //拼接【HDFS元数据分析jar包路径】
        shellSb.append(confMaps.get(TaskConstants.SPARK_TASK_KEY_JAR_PATH)).append(" ");

        //拼接jar参数【集群ID】
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_ID).append(" ")
                .append("\'").append(clusterId).append("\'").append(" ");
        //拼接jar参数【合并文件的目录】
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_SOURCE_PATH).append(" ")
                .append("\'").append(sourcePath).append("\'").append(" ");
        //拼接jar参数【压缩格式】
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_CODEC).append(" ")
                .append("\'").append(codec).append("\'").append(" ");
        //拼接jar参数【文件格式】
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_FORMAT).append(" ")
                .append("\'").append(format).append("\'").append(" ");

        //拼接jar参数【集群节点IP】
        String clusterHostConf = StringUtils.replace(sdpsCluster.getClusterHostConf(), "\"", "\\\"");
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_IP_HOST).append(" ")
                .append("\'").append(clusterHostConf).append("\'").append(" ");


        //获取海盒服务,集群配置API接口的HOST和IP
        SpringClientFactory springClientFactory = SpringUtil.getBean(SpringClientFactory.class);
        ILoadBalancer loadBalancerSeabox = springClientFactory.getLoadBalancer(ServiceNameConstants.SEABOX_PROXY_SERVICE);
        List<Server> serversSeabox = loadBalancerSeabox.getReachableServers();
        String seaboxProxyHostIp = serversSeabox.get(0).getHostPort();
        String clusterConfApiUrl = "http://".concat(seaboxProxyHostIp).concat("/seabox/getServerConfByConfName");

        //设置集群配置API
        String clusterConfApi = clusterConfApiUrl.concat("?clusterId=").concat(String.valueOf(sdpsCluster.getClusterId()))
                .concat("&confStrs=").concat("core-site,hdfs-site,hadoop-env").concat("&serverName=").concat("HDFS");
        if(isEnablekerberos){
            String downloadKrb5Api = "http://".concat(seaboxProxyHostIp).concat("/seaboxKeytab/downloadKrb5");
            //设置集群配置API
            shellSb.append("-").append(TaskConstants.TASK_PARAM_DOWNLOAD_KRB5_API).append(" ")
                    .append("\'").append(downloadKrb5Api).append("\'").append(" ");
        }

        //拼接jar参数【集群配置API地址】
        shellSb.append("-").append(TaskConstants.TASK_PARAM_CLUSTER_CONF_API).append(" ")
                .append("\'").append(clusterConfApi).append("\'").append(" ");

        //拼接jar参数【合并文件真实用户】
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_USER).append(" ")
                .append("\'").append(userName).append("\'").append(" ");
        //拼接jar参数【合并文件真实用户组】
        shellSb.append("-").append(TaskConstants.HDFS_MERGE_FILE_TASK_KEY_MERGE_REAL_GROUP).append(" ")
                .append("\'").append("hdfs").append("\'").append(" ");
        //拼接更新任务表的主键任务ID
        shellSb.append("-").append(TaskConstants.SDPS_JOB_ID).append(" ")
                .append("\'").append(jobId).append("\'").append(" ");
//        //拼接更新合并文件数据信息表主键ID
//        shellSb.append("-").append(TaskConstants.SDPS_MERGE_SUBMIT_ID).append(" ")
//                .append("\'").append(submitId).append("\'").append(" ");

        //拼接更新合并文件数据信息表主键ID
        String mergeDataIdPathMapJson = StringUtils.replace(JSONObject.toJSONString(mergeDataIdPathMap), "\"", "\\\"");
        shellSb.append("-").append(TaskConstants.SDPS_MERGE_DATA_ID_PATH_JSON).append(" ")
                .append("\'").append(mergeDataIdPathMapJson).append("\'");

        //远程执行
        //拼接打印日志
        shellSb.append(" ").append(">").append(" ").append(taskFilePath).append(filePrefix).append(".log").append(" ").append("2>&1").append("\n");

        //解决无法拿到jar的返回值【步骤2】
        shellSb.append("statue=\\$?").append("\n");
        shellSb.append("kill \\$tailpid").append("\n");
        shellSb.append("exit \\$statue");

        //获取密码
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

        //更新任务信息表
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
            log.info("启动合并小文件,成功更新任务表数据:"+sdpsTaskInfo.toString());
        }
        //更新合并文件提交信息表
        SdpsMergeSubmitInfo mergeSubmitInfo = SdpsMergeSubmitInfo.builder()
                .id(submitId)
                .taskId(Long.valueOf(jobId))
                .updateTime(new Date())
                .build();
        int updateMergeSubmitCount = sdpsMergeSubmitInfoMapper.updateById(mergeSubmitInfo);
        if(updateMergeSubmitCount > 0){
            log.info("启动合并小文件,成功更新合并文件提交信息表数据:"+mergeSubmitInfo.toString());
        }
        //更新合并文件数据信息表
        for (String mergeDataInfoId : mergeDataIdPathMap.keySet()) {
            SdpsMergeDataInfo sdpsMergeDataInfo = SdpsMergeDataInfo.builder()
                    .id(Long.valueOf(mergeDataInfoId))
                    .taskId(Long.valueOf(jobId))
                    .updateTime(new Date())
                    .build();
            int updateMergeDataInfoCount = sdpsMergeDataInfoMapper.updateById(sdpsMergeDataInfo);
            if(updateMergeDataInfoCount > 0 ){
                log.info("启动合并小文件,成功更新合并文件数据信息表数据:"+sdpsMergeDataInfo.toString());
            }
        }

        try {
            //远程集群创建目录
            RemoteShellExecutorUtil execMakeDir = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            execMakeDir.exec("mkdir -p ".concat(taskSavePath).concat("/").concat(clusterNameID).concat("/").concat(day));

            //远程集群生成shell脚本
            RemoteShellExecutorUtil execGenerateShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            execGenerateShell.exec("echo \"".concat(shellSb.toString()).concat("\" > ").concat(taskFilePath).concat(filePrefix).concat(".sh"));

            //远程集群执行shell脚本
            RemoteShellExecutorUtil execShell = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(), sdpsServerInfo.getUser(), sdpsServerInfo.getPasswd());
            int i = execShell.exec2("sh ".concat(taskFilePath).concat(filePrefix).concat(".sh"));
            if(i == 1){
                XxlJobHelper.handleFail("合并小文件任务失败");
            }else {
                flag = true;
            }
            XxlJobHelper.log("执行任务:",flag.toString());

        }catch (Exception e){
            log.error("合并HDFS文件脚本拼接、发送、执行异常:", e);
            XxlJobHelper.handleFail("分析HDFS元数据任务失败");
        }


        return flag;
    }

}
