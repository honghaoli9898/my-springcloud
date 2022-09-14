package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.bigdataProxy.bean.SdpsCoresMBInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: Denny
 * @date: 2021/11/12 17:47
 * @desc: 集群资源和内存使用率
 */
public interface SdpsCoresAndMBInfoMapper extends SuperMapper<SdpsCoresMBInfo> {

    ArrayList<SdpsCoresMBInfo> listSdpsCoresAndMBInfo(@Param("clusterId") Integer clusterId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    Double getMedianCurrentHour(@Param("clusterId") Integer clusterId, @Param("type") String type, @Param("nodeId") String nodeId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<String> getNodeIdByDuration(@Param("clusterId") Integer clusterId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

}
