package com.seaboxdata.sdps.seaboxProxy.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.TaskConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.merge.MergeSummaryInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeDataInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeSubmitInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeBlockSizeRanking;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeNumRanking;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileRankingTopN;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import com.seaboxdata.sdps.seaboxProxy.bean.HdfsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.feign.UserCenterFegin;
import com.seaboxdata.sdps.seaboxProxy.feign.XxlJobFegin;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsHdfsFileStatsMapper;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsMergeDataInfoMapper;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsMergeSubmitInfoMapper;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxFileMergeService;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;

@Slf4j
@RestController
@RequestMapping("/seabox/merge")
public class SeaBoxFileMergeController {

    @Autowired
    UserCenterFegin userCenterFegin;
    @Autowired
    BigdataCommonFegin bigdataCommonFegin;
    @Autowired
    BigdataVirtualHost bigdataVirtualHost;
    @Autowired
    ISeaBoxFileMergeService seaBoxFileMergeService;

    @Autowired
    XxlJobFegin xxlJobFegin;

    @Autowired
    SdpsMergeSubmitInfoMapper sdpsMergeSubmitInfoMapper;

    @Autowired
    SdpsMergeDataInfoMapper sdpsMergeDataInfoMapper;

    @Autowired
    SdpsHdfsFileStatsMapper sdpsHdfsFileStatsMapper;

    @Autowired
    SdpsClusterMapper sdpsClusterMapper;
    /**
     * 资源管理 -> 小文件合并 -> 校验表合并
     * @param fileMergeRequest 请求参数
     * @return
     */
    @GetMapping("/checkTableMerge")
    public HiveTableDTO checkTableMerge(FileMergeRequest fileMergeRequest) {
        return seaBoxFileMergeService.checkTableMerge(fileMergeRequest);
    }


    /**
     * 执行合并小文件
     * @param dispatchJobRequest
     * @return
     */
    @PostMapping("/mergeHdfsFileExec")
    public Result mergeHdfsFileExec(@RequestBody DispatchJobRequest dispatchJobRequest) {
        Boolean flag = false;
        Date date = new Date();
        SdpsMergeSubmitInfo sdpsMergeSubmitInfo = SdpsMergeSubmitInfo.builder()
                .clusterId(dispatchJobRequest.getClusterId())
                .sourcePath(dispatchJobRequest.getSourcePath())
                .formatType(dispatchJobRequest.getFormat())
                .codecType(dispatchJobRequest.getCodec())
                .createTime(date)
                .updateTime(date)
                .build();
        try {
            String mergeType = dispatchJobRequest.getType();
            if(mergeType.equalsIgnoreCase(TaskConstants.MERGE_FILE_TASK_TYPE_HDFS)){
                sdpsMergeSubmitInfo.setType(TaskConstants.MERGE_FILE_TASK_TYPE_HDFS);
            }else if(mergeType.equalsIgnoreCase(TaskConstants.MERGE_FILE_TASK_TYPE_HIVE)){
                sdpsMergeSubmitInfo.setType(TaskConstants.MERGE_FILE_TASK_TYPE_HIVE);
                sdpsMergeSubmitInfo.setDbName(dispatchJobRequest.getDatabaseName());
                sdpsMergeSubmitInfo.setTableName(dispatchJobRequest.getTableName());
            }else if(mergeType.equalsIgnoreCase(TaskConstants.MERGE_FILE_TASK_TYPE_HIVE_EXTERNAL)){
                sdpsMergeSubmitInfo.setType(TaskConstants.MERGE_FILE_TASK_TYPE_HIVE_EXTERNAL);
                sdpsMergeSubmitInfo.setDbName(dispatchJobRequest.getDatabaseName());
                sdpsMergeSubmitInfo.setTableName(dispatchJobRequest.getTableName());
            }else {
                return Result.failed("运行合并小文件任务失败,合并文件类型无效:"+dispatchJobRequest.getType());
            }

            dispatchJobRequest.setUserName("hdfs");
            int insertCount = sdpsMergeSubmitInfoMapper.insert(sdpsMergeSubmitInfo);
            if(insertCount > 0){
                //设置虚拟DNS
                bigdataVirtualHost.setVirtualHost(dispatchJobRequest.getClusterId());
                HdfsUtil hdfsUtil = new HdfsUtil(dispatchJobRequest.getClusterId());

                Map<String, String> mergeDataIdPathMap = new HashMap<>();
                String sourcePaths = dispatchJobRequest.getSourcePath();
                String[] paths = sourcePaths.split(",");
                for (String path : paths) {
                    HdfsDirPathInfo hdfsDirPathInfo = hdfsUtil.getHdfsDirAndSmallFileInfo(path, true);
                    SdpsMergeDataInfo sdpsMergeDataInfo = SdpsMergeDataInfo.builder()
                            .clusterId(dispatchJobRequest.getClusterId())
                            .submitId(sdpsMergeSubmitInfo.getId())
                            .sourcePath(dispatchJobRequest.getSourcePath())
                            .path(hdfsDirPathInfo.getDirPath())
                            .mergeBeforeTotalFileNum(hdfsDirPathInfo.getTotalFileNum())
                            .mergeBeforeTotalFileSize(hdfsDirPathInfo.getBytes())
                            .mergeBeforeTotalFileBlockSize(hdfsDirPathInfo.getTotalBlockBytes())
                            .mergeBeforeTotalSmallFileNum(hdfsDirPathInfo.getTotalSmallFileNum())
                            .createTime(date)
                            .updateTime(date)
                            .build();

                    int insertMergeCount = sdpsMergeDataInfoMapper.insert(sdpsMergeDataInfo);
                    if(insertMergeCount > 0){
                        mergeDataIdPathMap.put(sdpsMergeDataInfo.getId().toString(),sdpsMergeDataInfo.getPath());
                        dispatchJobRequest.setSubmitId(sdpsMergeSubmitInfo.getId());
                    }else {
                        return Result.failed("运行合并小文件任务失败,插入合并文件数据信息表失败.");
                    }
                }
                hdfsUtil.closeFs();
                dispatchJobRequest.setIsSdpsJob(Boolean.TRUE.toString());
                dispatchJobRequest.setMergeDataIdPathMap(mergeDataIdPathMap);
                //设置集群名称
                String clusterName = sdpsClusterMapper.selectById(dispatchJobRequest.getClusterId()).getClusterName();
                dispatchJobRequest.setClusterName(clusterName);

                flag = xxlJobFegin.execMergeFile(dispatchJobRequest);
                return Result.succeed(flag,"运行合并HDFS小文件任务成功");

            }else {
                return Result.failed("运行合并小文件任务失败,插入提交信息表失败.");
            }


        }catch (Exception e){
            log.error("运行合并小文件任务异常:",e);
            return Result.failed("运行合并小文件任务失败");
        }
    }

    /**
     * 重试合并小文件
     * @param clusterId
     * @param submitId
     * @return
     */
    @GetMapping("/mergeHdfsFileRetry")
    public Result mergeHdfsFileRetry(@RequestParam("clusterId") Integer clusterId,
                                    @RequestParam("submitId") Integer submitId){
        Boolean flag = false;
        try {
            QueryWrapper<SdpsMergeSubmitInfo> queryWrapper = new QueryWrapper<SdpsMergeSubmitInfo>()
                    .eq("id", submitId);
            SdpsMergeSubmitInfo mergeSubmitInfo = sdpsMergeSubmitInfoMapper.selectOne(queryWrapper);
            DispatchJobRequest dispatchJobRequest = DispatchJobRequest.builder()
                    .isSdpsJob("false")
                    .clusterId(clusterId)
                    .sourcePath(mergeSubmitInfo.getSourcePath())
                    .codec(mergeSubmitInfo.getCodecType())
                    .format(mergeSubmitInfo.getFormatType())
                    .isUpdateLogId("true")
                    .sdpsJobId(String.valueOf(mergeSubmitInfo.getTaskId()))
                    .submitId(Long.valueOf(submitId))
                    .build();

            Map<String, String> mergeDataIdPathMap = new HashMap<>();
            String[] SourcePathArr = mergeSubmitInfo.getSourcePath().split(",");
            for (String path : SourcePathArr) {
                QueryWrapper<SdpsMergeDataInfo> sdpsMergeDataInfoQueryWrapper = new QueryWrapper<SdpsMergeDataInfo>()
                        .eq("submit_id",submitId)
                        .eq("path",path);
                SdpsMergeDataInfo sdpsMergeDataInfo = sdpsMergeDataInfoMapper.selectOne(sdpsMergeDataInfoQueryWrapper);
                mergeDataIdPathMap.put(sdpsMergeDataInfo.getId().toString(),path);
            }
            dispatchJobRequest.setMergeDataIdPathMap(mergeDataIdPathMap);

            //运行Jobexecutor
            flag = xxlJobFegin.execMergeFile(dispatchJobRequest);
            return Result.succeed(flag,"重试合并小文件任务成功");
        }catch (Exception e){
            log.error("重试合并小文件任务异常:",e);
            return Result.failed("重试合并小文件任务失败");
        }
    }

    /**
     * 重试分析HDFS元数据
     * @param clusterId
     * @param taskId
     * @return
     */
    @GetMapping("/analyseHdfsMetaDataRetry")
    public Result analyseHdfsMetaDataRetry(@RequestParam("clusterId") Integer clusterId,
                                     @RequestParam("taskId") Integer taskId){
        Boolean flag = false;
        try {
            DispatchJobRequest dispatchJobRequest = DispatchJobRequest.builder()
                    .isSdpsJob("false")
                    .clusterId(clusterId)
                    .isUpdateLogId("true")
                    .sdpsJobId(String.valueOf(taskId))
                    .build();

            //运行Jobexecutor
            flag = xxlJobFegin.execAnalyseHdfsMetaData(dispatchJobRequest);
            return Result.succeed(flag,"重试分析HDFS元数据任务成功");
        }catch (Exception e){
            log.error("重试分析HDFS元数据任务异常:",e);
            return Result.failed("重试分析HDFS元数据任务失败");
        }
    }

    /**
     * 获取合并汇总信息
     * @param clusterId
     * @return
     */
    @GetMapping("/getMergeSummaryInfo")
    public MergeSummaryInfo getMergeSummaryInfo(@RequestParam("clusterId") Integer clusterId){
        MergeSummaryInfo mergeSummaryInfo = new MergeSummaryInfo();
        try {
            //以合并小文件数量
            Long completeMergeSmallFileSum = sdpsMergeDataInfoMapper.getCompleteMergeSmallFileSum(clusterId);
            //以合并小文件块数量
            Long completeMergeSmallFileBlockSum = sdpsMergeDataInfoMapper.getCompleteMergeSmallFileBlockSum(clusterId);
            //小文件总量
            QueryWrapper<HdfsFileStats> queryWrapper = new QueryWrapper<HdfsFileStats>()
                    .orderByDesc("update_time")
                    .eq("type_key","SIZE_64M")
                    .last("limit 1");
            HdfsFileStats hdfsFileStats = sdpsHdfsFileStatsMapper.selectOne(queryWrapper);

            SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);
            mergeSummaryInfo.setClusterId(clusterId);
            mergeSummaryInfo.setClusterName(sdpsCluster.getClusterName());
            mergeSummaryInfo.setDiffFileNum(completeMergeSmallFileSum);
            mergeSummaryInfo.setDiffFileSize(completeMergeSmallFileBlockSum);
            if(hdfsFileStats != null){
                mergeSummaryInfo.setTotalSmallFileNum(hdfsFileStats.getTypeValueNum());
            }else {
                mergeSummaryInfo.setTotalSmallFileNum(0L);
            }

        }catch (Exception e){
            log.error("获取合并小文件汇总信息异常:",e);
        }
        return mergeSummaryInfo;
    }

    /**
     * 获取合并文件TopN
     * @param clusterId
     * @param topN
     * @param day
     * @return
     */
    @GetMapping("/getMergeFileTopN")
    public SmallFileRankingTopN getMergeFileTopN(@RequestParam("clusterId") Integer clusterId,
                                                 @RequestParam("topN") Integer topN,
                                                 @RequestParam("day") Integer day){
        SmallFileRankingTopN smallFileRankingTopN = new SmallFileRankingTopN();
        try {
            List<SmallFileMergeNumRanking> mergeFileNumTopN = sdpsMergeDataInfoMapper.getMergeFileNumTopN(clusterId,day,topN);
            List<SmallFileMergeBlockSizeRanking> mergeFileBlockNumTopN = sdpsMergeDataInfoMapper.getMergeFileBlockNumTopN(clusterId,day,topN);

            smallFileRankingTopN.setClusterId(clusterId);
            smallFileRankingTopN.setMergeNumRankingList(mergeFileNumTopN);
            smallFileRankingTopN.setMergeBlockSizeRankingList(mergeFileBlockNumTopN);

        }catch (Exception e){
            log.error("小文件合并TopN接口异常:",e);
        }

        return smallFileRankingTopN;
    }

    /**
     * 资源管理 -> 小文件合并 -> 回显hdfs路径信息
     * @param fileMergeDetailRequest
     * @return
     */
    @GetMapping("/getFileMergeDetail")
    public FileMergeDTO getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest) {
        return seaBoxFileMergeService.getFileMergeDetail(fileMergeDetailRequest);
    }

}
