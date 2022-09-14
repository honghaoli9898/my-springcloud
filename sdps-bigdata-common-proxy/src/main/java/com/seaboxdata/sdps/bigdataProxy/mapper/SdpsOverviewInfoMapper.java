package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.bigdataProxy.bean.SdpsOverviewInfo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

/**
 * @author: Denny
 * @date: 2021/11/12 17:46
 * @desc: 集群概览信息
 */
public interface SdpsOverviewInfoMapper extends SuperMapper<SdpsOverviewInfo> {

    ArrayList<SdpsOverviewInfo> listSdpsOverviewInfo(@Param("clusterId") Integer clusterId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

}
