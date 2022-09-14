package com.seaboxdata.sdps.bigdataProxy.mapper;

import java.util.List;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.yarn.SdpsYarnInfo;

/**
 * @author: Denny
 * @date: 2021/10/13 18:53
 * @desc:
 */
public interface SdpsYarnInfoMapper extends SuperMapper<SdpsYarnInfo> {

    /**
     * 根据yarn id 查询yarn主节点&端口信息
     * @param clusterId yarn ID
     * @return yarn 主节点&端口信息
     */
    //@Cacheable(cacheNames = "sdpsYarnInfo")
    //SdpsYarnInfo selectYarnInfoById(Integer clusterId);

    /**
     * 查询clusterId
     * @return clusterId 列表
     */
    //List<SdpsYarnInfo> selectClusterId();


}
