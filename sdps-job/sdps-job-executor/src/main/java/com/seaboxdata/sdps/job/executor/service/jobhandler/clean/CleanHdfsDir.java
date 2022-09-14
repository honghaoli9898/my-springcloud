package com.seaboxdata.sdps.job.executor.service.jobhandler.clean;

import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.job.core.context.XxlJobHelper;
import com.seaboxdata.sdps.job.core.handler.annotation.XxlJob;
import com.seaboxdata.sdps.job.executor.feign.BigdataCommonFegin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CleanHdfsDir {
    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

    @XxlJob("CleanHdfsDir")
    public void executeCleanHdfsDir() {
        XxlJobHelper.log("cleanHdfsDir-JOB, start");
        XxlJobHelper.log("params:{}", XxlJobHelper.getJobParam());
        //查询存在生命周期的目录
        List<SdpsDirExpire> dirExpire = bigdataCommonFegin.getDirExpire();
        XxlJobHelper.log("dirExpireList:{}", dirExpire);
        //根据集群进行分组
        Map<Integer, List<SdpsDirExpire>> clusterMap = Maps.newHashMap();
        dirExpire.forEach(dir -> {
            List<SdpsDirExpire> list = clusterMap.get(dir.getClusterId());
            if (list == null) {
                list = Lists.newArrayList();
                clusterMap.put(dir.getClusterId(), list);
            }
            list.add(dir);
        });
        //按集群删除
        clusterMap.keySet().forEach(clusterId -> {
            List<SdpsDirExpire> list = clusterMap.get(clusterId);
            List<String> pathList = list.stream().map(SdpsDirExpire::getPath).collect(Collectors.toList());
            XxlJobHelper.log("delete clusterId:{} pathList:{}", clusterId, pathList);

            Result result = bigdataCommonFegin.cleanHdfsDir(clusterId, pathList);
            if (result == null || result.isFailed()) {
                XxlJobHelper.log("delete clusterId:{} failed", clusterId);
            } else {
                XxlJobHelper.log("delete clusterId:{} success", clusterId);
            }
        });

        XxlJobHelper.log("cleanHdfsDir-JOB, end");
    }
}
