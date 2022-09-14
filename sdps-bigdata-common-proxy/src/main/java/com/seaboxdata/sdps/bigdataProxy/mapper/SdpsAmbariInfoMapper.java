package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.ambari.SdpsAmbariInfo;


/**
 * @author: Denny
 * @date: 2021/10/28 10:32
 * @desc:
 */
public interface SdpsAmbariInfoMapper extends SuperMapper<SdpsAmbariInfo> {

    /**
     * 根据clusterId查询ambari-server节点信息。
     * @param clusterId 集群id
     * @return ambari-server的IP和端口。
     */
    SdpsAmbariInfo selectAmbariInfoById(Integer clusterId);
}
