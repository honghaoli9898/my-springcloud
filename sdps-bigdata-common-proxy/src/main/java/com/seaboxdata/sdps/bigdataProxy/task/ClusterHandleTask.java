package com.seaboxdata.sdps.bigdataProxy.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.websocket.ClusterWebSocket;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHost;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHostService;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 集群任务处理类
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/16
 */
@Slf4j
public class ClusterHandleTask implements Callable<String> {

    SdpsClusterMapper sdpsClusterMapper;

    List<SdpsClusterHostService> hostAndServiceList;

    Integer clusterId;

    public ClusterHandleTask(List<SdpsClusterHostService> hostAndServiceList, Integer clusterId) {
        this.hostAndServiceList = hostAndServiceList;
        this.clusterId = clusterId;
    }

    @Override
    public String call() throws Exception {
        boolean exeFlag = true;
        // TODO　分 master 和 slave 安装，可有顺序，可无顺序 可使用子任务执行，使用全局变量传输前节点给到的 信息
        // 分 一般 是先执行 master 将 master 信息给到 slave
        // 不分 master 和 slave 异步同时执行
        for (SdpsClusterHostService hostAndService : hostAndServiceList) {
            SdpsClusterHost host = hostAndService.getHost();
            List<SdpsClusterService> hostServices = hostAndService.getServices();

            RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(host.getIp(), host.getName(), host.getPasswd());
            // TODO　执行脚本 echo clusterInstalledOk 这个脚本可以通过远程拉取到目标主机，之后可删除
            String shScript = "sh /root/sdps/cluster_install_test.sh ";
            shScript += JSONUtil.toJsonStr(hostServices);
            int shResult = shellExecutorUtil.exec2(shScript);

            // 输出日志中可返回自定义错误信息，用于展示脚本所获取的错误信息
            StringBuffer outStrStringBuffer = shellExecutorUtil.getStdOutBuffer();
            StringBuffer outErrStringBuffer = shellExecutorUtil.getOutErrStringBuffer();
            boolean hasClusterInstalledOk = outStrStringBuffer.indexOf("clusterInstalledOk") > 0;


            if (shResult == -1 || StringUtils.isNotBlank(outErrStringBuffer) || hasClusterInstalledOk) {
                String errInfo = String.format("主机 %s 执行脚本失败 %s", host.getIp(), outErrStringBuffer.toString());
                log.info(errInfo);

                // TODO 执行失败情况
                // 第一台执行失败 直接返回执行失败
                // 第二台执行失败 第一台的数据 需要手动移除？
                exeFlag = false;
                break;
                // return errInfo;
            }
            log.info(hostAndService.getType() + " " + host.getIp() + " 安装成功");
        }

        // 需要手动传输？
        sdpsClusterMapper = SpringUtil.getApplicationContext().getBean(SdpsClusterMapper.class);

        SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);

        String result;
        if (exeFlag) {
            sdpsCluster.setClusterStatusId(SdpsCluster.Status.OK.getId());
            sdpsCluster.setRunning(SdpsCluster.RunStatus.START.getStatus());
            result = "安装失败";
        } else {
            sdpsCluster.setClusterStatusId(SdpsCluster.Status.FAILED.getId());
            result = "安装成功";
        }

        sdpsClusterMapper.updateById(sdpsCluster);

        // 使用Socket给前端发送 安装结果
        JSONObject installJson = new JSONObject();
        installJson.put("result", result);
        installJson.put("data", sdpsCluster);
        ClusterWebSocket clusterWebSocket = new ClusterWebSocket();
        clusterWebSocket.onMessage(installJson.toJSONString());
        return result;
    }
}
