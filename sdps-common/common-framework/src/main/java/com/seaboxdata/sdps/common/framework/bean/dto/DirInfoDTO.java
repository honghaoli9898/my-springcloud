package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.seaboxdata.sdps.common.framework.bean.TbDirInfo;

@Data
@EqualsAndHashCode(callSuper=false)
public class DirInfoDTO extends TbDirInfo {
    /**
     * 对比的日期
     */
    private String diffDay;
    /**
     * diffDay那天的存储量差
     */
    private Long diffTotalFileSize;
    /**
     * diffDay那天的存储量增长率
     */
    private String diffTotalFileSizeRatio;
    /**
     * diffDay那天的文件总数
     */
    private Long diffTotalFileNum;
    /**
     * diffDay那天的文件总数增长率
     */
    private String diffTotalFileNumRatio;
    /**
     * diffDay那天的小文件总数
     */
    private Long diffTotalSmallFileNum;
    /**
     * diffDay那天的小文件总数增长率
     */
    private String diffTotalSmallFileNumRatio;
    /**
     * 文件总数
     */
    private Long sumTotalFileNum;
    /**
     * 文件总大小
     */
    private Long sumTotalFileSize;

    /**
     * block块总数
     */
    private Long sumTotalBlockNum;
    /**
     * 小文件总数
     */
    private Long sumTotalSmallFileNum;
    /**
     * 空文件总数
     */
    private Long sumTotalEmptyFileNum;
    /**
     * 库类型
     */
    private String category;
}
