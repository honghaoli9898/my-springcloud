package com.seaboxdata.sdps.common.framework.bean.dto;

import com.seaboxdata.sdps.common.framework.bean.SdpsContentSummary;
import com.seaboxdata.sdps.common.framework.bean.SdpsFileStatus;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FileMergeDTO implements Serializable {
	private static final long serialVersionUID = 4977127550180032113L;
	private String basePath;
    /**
     * 路径
     */
    private String path;
    /**
     * 表格式
     */
    private String format;
    /**
     * 压缩算法
     */
    private String codec;
    /**
     * 库名
     */
    private String db;
    /**
     * 表名
     */
    private String table;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * hdfs目录信息
     */
    private SdpsContentSummary contentSummary;
    /**
     * 子目录信息
     */
    private List<SdpsFileStatus> fileStatusList;

    public void setTableInfo(FileMergeDetailRequest request) {
        db = request.getDbName();
        table = request.getTable();
        startTime = request.getStartTime();
        endTime = request.getEndTime();
    }
}
