package com.seaboxdata.sdps.common.framework.bean.merge;

import lombok.Data;
import lombok.ToString;

/**
 * 小文件合并排行(合并数)
 */
@Data
@ToString
public class SmallFileMergeNumRanking {
    private Long taskId;
    private String path;
    private Long alreadyMergeNum;
}
