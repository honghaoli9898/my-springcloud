package com.seaboxdata.sdps.job.executor.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.constant.TaskConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeSubmitInfo;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskConfig;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.job.executor.feign.XxlJobAdminFegin;
import com.seaboxdata.sdps.job.executor.mybatis.dto.XxlJobInfo;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsMergeSubmitInfoMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsTaskConfigMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.SdpsTaskInfoMapper;
import com.seaboxdata.sdps.job.executor.mybatis.mapper.XxlJobInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/merge")
public class MergerFileController {


    @Autowired
    XxlJobAdminFegin xxlJobAdminFegin;

    @Autowired
    SdpsTaskConfigMapper sdpsTaskConfigMapper;

    @Autowired
    XxlJobInfoMapper xxlJobInfoMapper;

    @Autowired
    SdpsTaskInfoMapper sdpsTaskInfoMapper;

    @Autowired
    SdpsMergeSubmitInfoMapper sdpsMergeSubmitInfoMapper;

    /**
     * 执行合并小文件
     * @param fileMergeExeRequest
     * @return
     */
    @PostMapping("/execMergeFile")
    public Boolean execMergeFile(@RequestBody DispatchJobRequest fileMergeExeRequest) {
        Boolean flag = false;
        try {
                QueryWrapper<SdpsTaskConfig> queryWrapper = new QueryWrapper<SdpsTaskConfig>()
            .eq("cluster_type_id", ClusterConstants.CLUSTER_TYPE_ID_SEABOX)
            .eq("task_type", TaskConstants.TASK_TYPE_HDFS_MERGE_FILE)
            .eq("arg_key", TaskConstants.TASK_KEY_XXL_JOB_MAP_ID);
            SdpsTaskConfig sdpsTaskConfig = sdpsTaskConfigMapper.selectOne(queryWrapper);

//            //更新任务信息表(更新submitId)
//            SdpsTaskInfo sdpsTaskInfoBefore = SdpsTaskInfo.builder()
//                    .id(Long.valueOf(dispatchJobRequest.getSdpsJobId()))
//                    .submitId(dispatchJobRequest.getSubmitId())
//                    .build();
//            int updateCountBefore = sdpsTaskInfoMapper.updateById(sdpsTaskInfoBefore);
//            if(updateCountBefore > 0){
//                log.info("启动合并小文件,成功更新submitId在任务表数据:"+sdpsTaskInfoBefore.toString());
//            }

            //更新任务信息表(更新submitId)
            SdpsTaskInfo sdpsTaskInfoBefore = SdpsTaskInfo.builder()
                    .clusterId(fileMergeExeRequest.getClusterId())
                    .clusterName(fileMergeExeRequest.getClusterName())
                    .submitId(fileMergeExeRequest.getSubmitId())
                    .createTime(DateUtil.date())
                    .ext0(fileMergeExeRequest.getSourcePath())
                    .build();
            sdpsTaskInfoMapper.insert(sdpsTaskInfoBefore);
            Long insertId = sdpsTaskInfoBefore.getId();

            SdpsMergeSubmitInfo sdpsMergeSubmitInfo = SdpsMergeSubmitInfo.builder()
                    .id(fileMergeExeRequest.getSubmitId())
                    .taskId(insertId)
                    .build();
            sdpsMergeSubmitInfoMapper.updateById(sdpsMergeSubmitInfo);

            fileMergeExeRequest.setSdpsJobId(insertId.toString());
            //taskInfo表已插入数据，后续无需插入(创建了新数据则设为true)
            fileMergeExeRequest.setCreateTaskInfo(true);

            xxlJobAdminFegin.execMergeFile(Integer.valueOf(sdpsTaskConfig.getArgValue()),
                    JSONObject.toJSONString(fileMergeExeRequest),"");
            flag = true;
        }catch (Exception e){
            log.error("执行合并小文件异常:",e);
        }
        return flag;
    }

    /**
     * 执行分析HDFS元数据
     * @param fileMergeExeRequest
     * @return
     */
    @PostMapping("/execAnalyseHdfsMetaData")
    public Boolean execAnalyseHdfsMetaData(@RequestBody DispatchJobRequest fileMergeExeRequest) {
        Boolean flag = false;
        try {
            QueryWrapper<XxlJobInfo> wrapper = new QueryWrapper<XxlJobInfo>()
                    .eq("executor_handler","AnalysisHdfsMetaDataDispatch");
            List<XxlJobInfo> xxlJobInfoList = xxlJobInfoMapper.selectList(wrapper);
            for (XxlJobInfo xxlJobInfo : xxlJobInfoList) {
                String executorParam = xxlJobInfo.getExecutorParam();
                Object object = JSON.parse(executorParam);
                if( object instanceof JSONObject){
                    int clusterId = ((JSONObject) object).getIntValue("clusterId");
                    if(clusterId == fileMergeExeRequest.getClusterId()){
                        xxlJobAdminFegin.execMergeFile(Integer.valueOf(xxlJobInfo.getId()),
                        JSONObject.toJSONString(fileMergeExeRequest),"");
                        flag = true;
                    }
                }

            }

        }catch (Exception e){
            log.error("执行合并小文件异常:",e);
        }
        return flag;
    }
}
