package com.seaboxdata.sdps.job.executor.feign;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = ServiceNameConstants.BIGDATA_PROXY_SERVICE)
public interface BigdataCommonFegin {

    @GetMapping(value = "/bigdataCommon/selectClusterList")
    Result<List<SdpsCluster>> getClusterList();

    @GetMapping("/bigdataCommon/getTaskConfByClusterTypeAndTaskType")
    List<TaskConfig> getTaskConfByClusterTypeAndTaskType(@RequestParam("taskType") String taskType,
                                                         @RequestParam("cluster_type") String cluster_type);

    @GetMapping("/clusterCommon/querySdpsClusterById")
    SdpsCluster querySdpsClusterById(@RequestParam("clusterId") Integer clusterId);

    @GetMapping("/storageManager/getDirExpire")
    List<SdpsDirExpire> getDirExpire();

    @DeleteMapping("/bigdataCommon/cleanHdfsDir")
    Result cleanHdfsDir(@RequestParam("clusterId") Integer clusterId,
                                     @RequestParam("hdfsPathList") List<String> hdfsPathList);


    @GetMapping("/bigdataCommon/getServerConfByConfName")
    Result<String> getServerConfByConfName(
            @RequestParam("clusterId") Integer clusterId,
            @RequestParam("serverName") String serverName,
            @RequestParam("confStrs") List<String> confStrs);

    /**
     * 根据集群和服务获取组件和节点的关系
     * @param clusterId   集群id
     * @param serviceName 服务名
     * @return
     */
    @GetMapping("/config/getComponentAndHost")
    Result getComponentAndHost(@RequestParam("clusterId") Integer clusterId, @RequestParam("serviceName") String serviceName);
}
