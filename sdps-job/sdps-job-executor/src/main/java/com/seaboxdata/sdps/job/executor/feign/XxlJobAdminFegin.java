package com.seaboxdata.sdps.job.executor.feign;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.job.core.biz.model.ReturnT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = ServiceNameConstants.XXL_JOB_ADMIN_SERVER)
public interface XxlJobAdminFegin {

    @PostMapping("/xxl-job-admin/jobinfo/trigger")
    public ReturnT<String> execMergeFile(@RequestParam("id") Integer id,
                                         @RequestParam("executorParam") String executorParam,
                                         @RequestParam("addressList") String addressList);

}
