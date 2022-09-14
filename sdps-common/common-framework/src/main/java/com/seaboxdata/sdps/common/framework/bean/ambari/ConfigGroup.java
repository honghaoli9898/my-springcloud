package com.seaboxdata.sdps.common.framework.bean.ambari;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConfigGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /**
     * 集群id
     */
    private Integer clusterId;
    /**
     * 注释
     */
    private String description;

    private List<String> desired_configs;
    /**
     * 组名
     */
    private String group_name;
    /**
     * 服务名
     */
    private String service_name;

    private String tag;
    /**
     * 主机
     */
    private List<Map<String, String>> hosts;
}
