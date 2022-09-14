package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 集群服务信息模板表
 *
 * @author jiaohongtao
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sdps_cluster_service_template")
@Data
public class SdpsClusterServiceTemplate extends SuperEntity<SdpsClusterServiceTemplate> {
	private static final long serialVersionUID = -8388541554337883320L;
	/** 名称 */
    private String name;
    /** 集群版本 eg: sdp7.0 */
    private String clusterVersion;
    /** 版本 */
    private String version;
    /** 描述 */
    private String remark;

    @TableField("is_used")
    private Boolean used;

    @TableField(exist = false)
    List<SdpsClusterServiceTemplateComponent> components;

    /**
     * [ { "zoo_env": [{},..] },... ]
     */
    @TableField(exist = false)
    List<Map<String, List<SdpsClusterServiceTemplateConfig>>> configs;
}