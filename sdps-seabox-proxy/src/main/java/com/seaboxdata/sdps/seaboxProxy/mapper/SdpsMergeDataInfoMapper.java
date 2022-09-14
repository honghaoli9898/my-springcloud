package com.seaboxdata.sdps.seaboxProxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeDataInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeBlockSizeRanking;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeNumRanking;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SdpsMergeDataInfoMapper extends BaseMapper<SdpsMergeDataInfo> {

    Long getCompleteMergeSmallFileSum(@Param("clusterId") Integer clusterId);

    Long getCompleteMergeSmallFileBlockSum(@Param("clusterId") Integer clusterId);

    List<SmallFileMergeNumRanking> getMergeFileNumTopN(@Param("clusterId") Integer clusterId,
                                                       @Param("inDay") Integer inDay,
                                                       @Param("topN")Integer topN);

    List<SmallFileMergeBlockSizeRanking> getMergeFileBlockNumTopN(@Param("clusterId") Integer clusterId,
                                                                  @Param("inDay") Integer inDay,
                                                                  @Param("topN") Integer topN);
}
