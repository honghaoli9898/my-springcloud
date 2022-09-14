package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StorageDTO implements Serializable {
	private static final long serialVersionUID = -4140410128790608105L;
	/**
     * hdfs路径
     */
    private String path;
    /**
     * 所属人
     */
    private String owner;
    /**
     * 所属组
     */
    private String group;
    /**
     * 存储量，单位：B
     */
    private Long storageSize;
    /**
     * 空间配额
     */
    private Long dsQuota;
    /**
     * 逻辑配额
     */
    private Long nsQuota;
    /**
     * 过期天数
     */
    private Integer expire;
    /**
     * 权限
     */
    private String permission;
    /**
     * 修改时间
     */
    private String modifyTime;
}
