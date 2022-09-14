package com.seaboxdata.sdps.common.framework.bean.ranger.resource;

import lombok.Data;

import java.util.List;

@Data
public class HiveOperateResource {
    private List<String> database;
    private List<String> table;
    private List<String> column;
}
