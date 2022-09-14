package com.seaboxdata.sdps.seaboxProxy.feign;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.framework.bean.request.DispatchJobRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServiceNameConstants.XXL_JOB_EXECUTOR_SERVER)
public interface XxlJobFegin {

    /**
     * 执行合并小文件
     * @param dispatchJobRequest
     * @return
     */
    @PostMapping("/merge/execMergeFile")
    public Boolean execMergeFile(@RequestBody DispatchJobRequest dispatchJobRequest);

    /**
     * 执行分析HDFS元数据
     * @param dispatchJobRequest
     * @return
     */
    @PostMapping("/merge/execAnalyseHdfsMetaData")
    public Boolean execAnalyseHdfsMetaData(@RequestBody DispatchJobRequest dispatchJobRequest);

}
