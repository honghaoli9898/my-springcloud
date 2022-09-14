package com.seaboxdata.sdps.common.framework.bean.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TableDtoObj extends SdpsTableObj implements Serializable {

    private static final long serialVersionUID = -2625486355260474959L;
    private Long id;
    private String type;
    private String assItemName;
    private String databaseName;
    private String assItemIden;
}
