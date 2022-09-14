package com.seaboxdata.sdps.common.framework.bean;

import lombok.Data;

import java.io.Serializable;
@Data
public class SdpsContentSummary implements Serializable {
	private static final long serialVersionUID = -7469607353582130889L;
	/**
     * 已存储的文件长度
     */
    private Long length = 0L;
    /**
     * 文件个数
     */
    private Long fileCount = 0L;
    /**
     * 目录个数
     */
    private Long directoryCount = 0L;
    /**
     * 目录数+文件数的配额
     */
    private Long quota = 0L;
    /**
     * 已使用空间
     */
    private Long spaceConsumed = 0L;
    /**
     * 空间配额
     */
    private Long spaceQuota = 0L;

    public void sum(SdpsContentSummary others) {
        length = length + others.getLength();
        fileCount = fileCount + others.getFileCount();
        directoryCount = directoryCount + others.getDirectoryCount();
        spaceConsumed = spaceConsumed + others.getSpaceConsumed();
        if (others.getQuota() != -1L) {
            quota = quota + others.getQuota();
        }

        if (others.getSpaceQuota() != -1L) {
            spaceQuota = spaceQuota + others.getSpaceQuota();
        }

    }
}
