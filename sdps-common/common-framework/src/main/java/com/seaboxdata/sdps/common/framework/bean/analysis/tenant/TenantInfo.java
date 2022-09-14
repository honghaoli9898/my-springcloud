package com.seaboxdata.sdps.common.framework.bean.analysis.tenant;

import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import lombok.Data;

import java.io.Serializable;

@Data
public class TenantInfo implements Serializable {
    private static final long serialVersionUID = 1571946196453929968L;
    private String name;
    private DirFileType type;
    private String typeValue;

}
