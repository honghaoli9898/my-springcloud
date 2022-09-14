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

import java.util.List;

@Component
@FeignClient(name = ServiceNameConstants.ITEM_CENTER)
public interface ItemDataSourceFegin {
    /**
     * 根据集群id获取hive数据源开启kerberos时的principal
     * @param clusterId 集群id
     * @return
     */
    @GetMapping("/MC30/getHivePrincipal")
    String getHivePrincipal(@RequestParam("clusterId") Integer clusterId);

    /**
     * 更新keytab文件
     *
     * @param list
     * @return
     */
    @PostMapping("/updateKeytab")
    public Result updateKeytab(@RequestBody List<SdpServerKeytab> list);
}
