package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;

@Data
@EqualsAndHashCode(callSuper=false)
public class FileStatsDTO extends HdfsFileStats {
	private static final long serialVersionUID = 2039843027881188181L;
	/**
     * 数量占比
     */
    private String percentNum;
    /**
     * 大小占比
     */
    private String percentSize;
}
