package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件统计Mapper
 *
 * @author yangqiang
 */
@Mapper
public interface HdfsFileStatsMapper extends SuperMapper<HdfsFileStats> {
    List<HdfsFileStats> selectByType(@Param("clusterId") Integer clusterId, @Param("type") String type);
}
