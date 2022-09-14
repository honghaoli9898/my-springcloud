package com.seaboxdata.sdps.common.framework.bean.ranger.resource;

import lombok.Data;

import java.util.List;

@Data
public class HbaseOperateResource {
    private List<String> table;
    private List<String> columnFamily;
    private List<String> column;
}
