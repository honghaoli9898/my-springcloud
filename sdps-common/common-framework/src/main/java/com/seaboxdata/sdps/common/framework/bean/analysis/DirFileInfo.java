package com.seaboxdata.sdps.common.framework.bean.analysis;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DirFileInfo implements Serializable {
    private static final long serialVersionUID = 6809240806161676197L;
    private String cluster;
    private String path;
    private Long totalFileNum;
    private Long totalFileSize;
    private Double avgFileSize;
    private Long totalBlockNum;
    private Long totalSmallFileNum;
    private Long totalEmptyFileNum;
    private Integer pathIndex;
    private Integer type;
    private String typeValue = "";
    private String tenant = "";
    /**
     * 修改时间
     */
    private String modificationTime;
    /**
     * 访问时间
     */
    private String accessTime;
    /**
     * 热度
     */
    private Integer temperature;
    /**
     * 日期
     */
    private String dayTime;
}
