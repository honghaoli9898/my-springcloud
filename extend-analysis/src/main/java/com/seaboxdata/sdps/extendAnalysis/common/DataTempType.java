package com.seaboxdata.sdps.extendAnalysis.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTempType {
    HOT(1, "HOT"),
    WARM(2, "WARM"),
    COLD(3, "COLD"),
    FROZEN(4, "FROZEN");

    private Integer index;
    private String name;

}
