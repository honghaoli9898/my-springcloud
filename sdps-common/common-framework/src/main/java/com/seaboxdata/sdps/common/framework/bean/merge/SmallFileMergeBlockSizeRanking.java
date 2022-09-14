package com.seaboxdata.sdps.common.framework.bean.merge;

import lombok.Data;

/**
 * 小文件合并排行(合并的块大小)
 */
@Data
public class SmallFileMergeBlockSizeRanking {
    private Long taskId;
    private String path;
    private Long alreadyMergeBlockSize;
}
