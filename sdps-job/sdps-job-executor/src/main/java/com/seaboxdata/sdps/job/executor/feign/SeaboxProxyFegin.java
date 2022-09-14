package com.seaboxdata.sdps.job.executor.feign;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Component
@FeignClient(name = ServiceNameConstants.SEABOX_PROXY_SERVICE)
public interface SeaboxProxyFegin {

    @GetMapping("/seabox/getServerConfByConfName")
    String getServerConfByConfName(@RequestParam("clusterId") Integer clusterId,
                                   @RequestParam("serverName") String serverName,
                                   @RequestParam("confStrs") ArrayList<String> confStrs);

    @GetMapping("/seabox/execFetchAndExtractHdfsMetaData")
    Boolean execFetchAndExtractHdfsMetaData(@RequestParam("clusterId") Integer clusterId);

    @GetMapping("/seaboxKeytab/checkKeytab")
    Result checkKeytab(@RequestParam("keytabs") List<String> keytabs, @RequestParam("clusterIds") List<Integer> clusterIds);

    /**
     * 获取集群最新配置
     *
     * @param clusterId
     * @param serverName
     * @param confStrs
     * @return
     */
    @GetMapping("/seabox/getServerConfByConfName")
    String getServerConfByConfName(
            @RequestParam("clusterId") Integer clusterId,
            @RequestParam("serverName") String serverName,
            @RequestParam("confStrs") List<String> confStrs);

    /**
     * 获取ambari的kerberos列表
     * @param clusterId 集群id
     * @return
     */
    @GetMapping("/seabox/getServerKeytabs")
    Result getServerKeytabs(@RequestParam("clusterId") Integer clusterId);

    /**
     * 更新keytab文件
     * @param list
     * @return
     */
    @PostMapping("/seaboxKeytab/updateKeytab")
    Result updateKeytab(@RequestBody List<SdpServerKeytab> list);
}
