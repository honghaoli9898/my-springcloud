package com.seaboxdata.sdps.common.framework.bean.merge;


import lombok.Data;

import java.util.List;

/**
 * 小文件合并TopN
 */
@Data
public class SmallFileRankingTopN {
    private Integer clusterId;
    private List<SmallFileMergeNumRanking> mergeNumRankingList;
    private List<SmallFileMergeBlockSizeRanking> mergeBlockSizeRankingList;
}
