package com.seaboxdata.sdps.bigdataProxy.bean;

import lombok.Data;

import java.text.DecimalFormat;

@Data
public class SdpsCoresMBInfoDTO extends SdpsCoresMBInfo {
    /**
     * 资源使用率中位数
     */
    private Double medianPercent;

    public SdpsCoresMBInfoDTO(String nodeId, Integer clusterId, Double medianPercent) {
        super(nodeId, clusterId);
        this.medianPercent = medianPercent;
    }
}
