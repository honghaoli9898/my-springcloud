package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 集群服务组件信息模板表
 *
 * @author jiaohongtao
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sdps_cluster_service_template_config")
@Data
public class SdpsClusterServiceTemplateConfig extends SuperEntity<SdpsClusterServiceTemplateConfig> {
	private static final long serialVersionUID = 6568879328485358371L;
	/** 名称 */
    private String name;
    /** 值 */
    private String value;
    /** 描述 */
    private String remark;
    /** 属性类型 */
    private String type;
    /** xml名称 */
    private String configName;
    /** 服务模板ID */
    private Long serviceTemplateId;

    /** 展示名 */
    private String displayName;
    /** 是否自定义字段：用于展示原Ambari的 Custom 属性，和传输中 properties 里的数据 */
    @TableField("is_custom")
    private Boolean custom;
    /** 是否Final字段：用于展示原Ambari的 Final 属性，和传输中 properties_attributes 里的数据 */
    private Boolean isFinal;
}