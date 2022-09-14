package com.seaboxdata.sdps.seaboxProxy.bean.cluster;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class Configuration {
    @JSONField(name = "Config")
    private Map<String,String> config;

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "tag")
    private String tag;

    @JSONField(name = "version")
    private Integer version;

    @JSONField(name = "properties")
    private Map<String,String> properties;
}
