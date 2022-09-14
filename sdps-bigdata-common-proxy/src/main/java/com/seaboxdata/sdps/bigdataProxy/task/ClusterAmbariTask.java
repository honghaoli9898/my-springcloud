package com.seaboxdata.sdps.bigdataProxy.task;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.bigdataProxy.websocket.ClusterWebSocket;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

/**
 * 集群Ambari任务处理类
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/16
 */
@Slf4j
public class ClusterAmbariTask implements Callable<Result<String>> {

    String ip;
    String name;
    String passwd;
    String domain;
    Integer clusterId;

    SdpsClusterMapper sdpsClusterMapper;

    public ClusterAmbariTask(String ip, String name, String passwd, Integer clusterId) {
        this.ip = ip;
        this.name = name;
        this.passwd = passwd;
        this.clusterId = clusterId;
    }

    @Override
    public Result<String> call() throws Exception {
        boolean exeFlag = true;

        RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(ip, name, passwd);
        // TODO　执行脚本 echo clusterInstalledOk 这个脚本可以通过远程拉取到目标主机，之后可删除
        String shScript = "sh /root/sdps/ambari_install_test.sh ";
        shScript += domain;
        int shResult = shellExecutorUtil.execShell(shScript);

        // 输出日志中可返回自定义错误信息，用于展示脚本所获取的错误信息
        StringBuffer outStrStringBuffer = shellExecutorUtil.getStdOutBuffer();
        StringBuffer outErrStringBuffer = shellExecutorUtil.getStdErrBuffer();
        boolean hasAmbariInstalledOk = outStrStringBuffer.indexOf("ambariInstalledOk") > -1;

        if (shResult == -1 || StringUtils.isNotBlank(outErrStringBuffer) || hasAmbariInstalledOk) {
            String errInfo = String.format("主机 %s 执行脚本安装Ambari失败 %s", ip, outErrStringBuffer.toString());
            log.info(errInfo);
            exeFlag = false;
        }

        // 需要手动传输？
        sdpsClusterMapper = SpringUtil.getApplicationContext().getBean(SdpsClusterMapper.class);

        SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);

        Result<String> result = Result.succeed("Ambari 安装成功");
        if (!exeFlag) {
            sdpsCluster.setClusterStatusId(SdpsCluster.Status.FAILED.getId());
            sdpsClusterMapper.updateById(sdpsCluster);
            result =  Result.failed("安装Ambari失败");
        }

        sdpsCluster.setClusterStatusId(SdpsCluster.Status.AMBARI_INSTALLED.getId());
        sdpsClusterMapper.updateById(sdpsCluster);
        // 使用Socket给前端发送 安装结果
        /*JSONObject installJson = new JSONObject();
        installJson.put("result", result);
        installJson.put("data", sdpsCluster);
        ClusterWebSocket clusterWebSocket = new ClusterWebSocket();
        clusterWebSocket.onMessage(installJson.toJSONString());*/
        return result;
        /*String returnMsg = "Ambari ";
        if (exeFlag) {
            return Result.succeed(returnMsg + "安装成功");
        } else {
            return Result.failed(returnMsg + "安装失败");
        }*/
    }
}
