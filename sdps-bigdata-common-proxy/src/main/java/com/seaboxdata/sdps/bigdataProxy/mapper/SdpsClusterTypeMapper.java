package com.seaboxdata.sdps.bigdataProxy.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.seaboxdata.sdps.bigdataProxy.bean.SdpsClusterType;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;

/**
 * 集群状态Mapper
 *
 * @author jiaohongtao
 */
@Mapper
public interface SdpsClusterTypeMapper extends SuperMapper<SdpsClusterType> {
}
