package com.seaboxdata.sdps.common.framework.bean.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class FileMergeDetailRequest extends FileMergeRequest{
	private static final long serialVersionUID = 222113933777139843L;
	/**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 表类型
     */
    private String format;
    /**
     * 表压缩算法
     */
    private String codec;

    public FileMergeDetailRequest(@NonNull Integer clusterId) {
        super(clusterId);
    }
}
