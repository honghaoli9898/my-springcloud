package com.seaboxdata.sdps.job.executor.feign;

import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = ServiceNameConstants.USER_SYNC_SERVER)
public interface UserSyncCenterFegin {
    /**
     * 从kdc节点拉取keytab文件
     * @param pathList keytab路径集合
     * @return
     */
    @GetMapping("/usersync/pullKeytabFromKdc")
    Result pullKeytabFromKdc(@RequestParam("pathList") List<String> pathList);
}
