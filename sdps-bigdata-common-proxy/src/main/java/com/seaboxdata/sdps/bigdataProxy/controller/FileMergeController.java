package com.seaboxdata.sdps.bigdataProxy.controller;

import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;
import com.seaboxdata.sdps.bigdataProxy.service.IFileMergeService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.merge.MergeSummaryInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileRankingTopN;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/merge")
public class FileMergeController {
    @Autowired
    private IFileMergeService fileMergeService;
    @Autowired
    CommonBigData commonBigData;

    /**
     * 资源管理 -> 小文件合并 -> 校验表合并
     * @param fileMergeRequest 请求参数
     * @return
     */
    @GetMapping("/checkTableMerge")
    public Result checkTableMerge(FileMergeRequest fileMergeRequest) {
        return commonBigData.checkTableMerge(fileMergeRequest);
    }

    /**
     * 资源管理 -> 小文件合并 -> 回显hdfs路径信息
     * @param fileMergeDetailRequest
     * @return
     */
    @GetMapping("/getFileMergeDetail")
    public Result getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest) {
        return commonBigData.getFileMergeDetail(fileMergeDetailRequest);
    }

    /**
     * 获取合并小文件信息
     * @param clusterId
     * @return
     */
    @GetMapping("/getMergeSummaryInfo")
    public Result getMergeSummaryInfo(@RequestParam("clusterId") Integer clusterId) {
        try {
            MergeSummaryInfo mergeSummaryInfo = commonBigData.getMergeSummaryInfo(clusterId);
            return Result.succeed(mergeSummaryInfo,"获取合并小文件信息成功");
        }catch (Exception e){
            log.error("获取合并小文件信息异常:",e);
            return Result.failed("获取合并小文件信息失败:",e.getMessage());
        }
    }

    /**
     * 获取小文件合并TopN
     * @param clusterId
     * @param topN
     * @return
     */
    @GetMapping("/getMergeFileTopN")
    public Result getMergeFileTopN(@RequestParam("clusterId") Integer clusterId,
                                   @RequestParam("topN") Integer topN,
                                   @RequestParam("day") Integer day){
        try {
            SmallFileRankingTopN mergeFileTopN = commonBigData.getMergeFileTopN(clusterId, topN,day);
            return Result.succeed(mergeFileTopN,"获取合并小文件信息成功");
        }catch (Exception e){
            log.error("获取小文件合并TopN异常:",e);
            return Result.failed("获取小文件合并TopN失败:",e.getMessage());
        }
    }

    /**
     * 执行合并小文件
     * @param dispatchJobRequest
     * @return
     */
    @PostMapping("/mergeHdfsFileExec")
    public Result mergeHdfsFileExec(@RequestBody DispatchJobRequest dispatchJobRequest) {
        return commonBigData.mergeHdfsFileExec(dispatchJobRequest);
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
        return commonBigData.mergeHdfsFileRetry(clusterId,submitId);
    }

    /**
     * 重试合并小文件
     * @param clusterId
     * @param taskId
     * @return
     */
    @GetMapping("/analyseHdfsMetaDataRetry")
    public Result analyseHdfsMetaDataRetry(@RequestParam("clusterId") Integer clusterId,
                                           @RequestParam("taskId") Integer taskId){
        return commonBigData.analyseHdfsMetaDataRetry(clusterId,taskId);
    }

}
