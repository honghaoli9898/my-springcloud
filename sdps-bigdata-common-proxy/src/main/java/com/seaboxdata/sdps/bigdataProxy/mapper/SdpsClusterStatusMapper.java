package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 集群状态Mapper
 *
 * @author jiaohongtao
 */
@Mapper
public interface SdpsClusterStatusMapper extends SuperMapper<SdpsClusterStatus> {
}
