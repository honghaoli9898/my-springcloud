package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskConfig;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.task.TaskConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SdpsTaskConfigMapper extends SuperMapper<SdpsTaskConfig> {
    /**
     * 根据任务类型和集群类型查询任务配置
     *
     * @param taskType
     * @param cluster_type_en_name
     * @return
     */
    List<TaskConfig> queryTaskConfByClusterTypeAndTaskType(@Param("taskType") String taskType, @Param("cluster_type_en_name") String cluster_type_en_name);
}
