package com.seaboxdata.sdps.common.framework.bean.merge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MergeSummaryInfo {
    /**
     * 集群ID
     */
    private Integer clusterId;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 总的小文件数
     */
    private Long totalSmallFileNum;
    /**
     * 已合并小文件数量
     */
    private Long diffFileNum;
    /**
     * 已节约存储量
     */
    private Long diffFileSize;
}
