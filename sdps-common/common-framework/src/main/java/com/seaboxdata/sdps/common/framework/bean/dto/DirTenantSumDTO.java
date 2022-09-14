package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Getter;
import lombok.Setter;

import com.seaboxdata.sdps.common.framework.bean.TbDirInfo;
@Getter
@Setter
public class DirTenantSumDTO extends TbDirInfo {

    private String diffDay;

    private Long diffFileSize;

    private Double diffFileSizeRatio;

    private Long diffFileNum;

    private Double diffFileNumRatio;

    private Long diffSmallFileNum;

    private Double diffSmallFileNumRatio;
}
