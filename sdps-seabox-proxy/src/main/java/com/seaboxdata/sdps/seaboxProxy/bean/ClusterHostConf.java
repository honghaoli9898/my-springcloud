package com.seaboxdata.sdps.seaboxProxy.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class ClusterHostConf {
    private String host;
    private String ip;
}
