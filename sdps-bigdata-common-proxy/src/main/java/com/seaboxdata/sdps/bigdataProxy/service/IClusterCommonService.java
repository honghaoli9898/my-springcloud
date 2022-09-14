package com.seaboxdata.sdps.bigdataProxy.service;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.core.utils.SimplePageRequest;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;

import java.util.List;

/**
 * 集群管理Service
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
public interface IClusterCommonService extends ISuperService<SdpsCluster> {

    /**
     * 集群翻页
     *
     * @param requestBean 请求对象
     */
    PageResult<SdpsCluster> clusters(PageRequest<JSONObject> requestBean);

    /**
     * 远程集群创建
     *
     * @param user        当前用户
     * @param sdpsCluster 远程集群
     */
    Result<SdpsCluster> remoteClusterSave(SysUser user, SdpsCluster sdpsCluster) throws Exception;

    /**
     * 集群更新
     *
     * @param user        用户
     * @param id          id
     * @param sdpsCluster 实体
     */
    Result<SdpsCluster> update(SysUser user, Long id, SdpsCluster sdpsCluster);

    /**
     * 集群删除
     *
     * @param user 用户
     * @param id   id
     */
    Result<SdpsCluster> delete(SysUser user, Long id);

    /**
     * 新建集群
     *
     * @param user        用户
     * @param sdpsCluster 集群
     */
    Result<SdpsCluster> clusterSave(SysUser user, SdpsCluster sdpsCluster) throws Exception;

    /**
     * 批量删除
     *
     * @param user 用户
     * @param ids  ids
     */
    Result<SdpsCluster> batchDelete(SysUser user, List<Long> ids);

    /**
     * 集群启用
     *
     * @param user 用户
     * @param id   id
     * @param usernameAndPasswd 远程集群主节点账号密码
     */
    Result<SdpsCluster> startCluster(SysUser user, Long id, JSONObject usernameAndPasswd) throws Exception;

    /**
     * 集群停用
     *
     * @param user 用户
     * @param id   id
     * @param usernameAndPasswd 远程集群主节点账号密码
     */
    Result<SdpsCluster> stopCluster(SysUser user, Long id, JSONObject usernameAndPasswd) throws Exception;

    /**
     * 获取集群执行操作内容
     *
     * @param id   集群ID
     */
    JSONObject performOperation(Integer id);

    /**
     * 获取集群执行操作内容
     *
     * @param id     集群ID
     * @param nodeId 节点ID
     */
    JSONObject performOperationDetail(Integer id, Integer nodeId);

    /**
     * 告警消息
     *
     * @param id 集群ID
     */
    JSONObject alarmMsg(Integer id);

    /**
     * 查询开启了kerberos的集群id
     *
     * @return
     */
    List<Integer> getEnableKerberosClusters();
}
