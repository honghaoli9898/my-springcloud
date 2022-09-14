package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import java.util.List;

public interface IClusterClusterService{

    /**
     * 根据用户获取集群主机地址
     * @param user
     * @return
     */
    List<SdpsServerInfo> queryClusterHosts(String user);

    /**
     * 生成6位数token
     * @param user
     * @return
     */
    String generateCode(String user);

    /**
     * 对比token信息
     * @param code
     * @param user
     * @return
     */
    Boolean compareUserToken(String code, String user);
}
