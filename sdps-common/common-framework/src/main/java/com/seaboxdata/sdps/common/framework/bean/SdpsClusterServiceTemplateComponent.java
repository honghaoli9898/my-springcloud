package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 集群服务组件信息模板表
 *
 * @author jiaohongtao
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sdps_cluster_service_template_component")
@Data
public class SdpsClusterServiceTemplateComponent extends SuperEntity<SdpsClusterServiceTemplateComponent> {
	private static final long serialVersionUID = 6980788280190859428L;
	/** 名称 */
    private String name;
    /** 描述 */
    private String remark;
    /** 服务模板ID */
    private Long serviceTemplateId;

    private String displayName;

    /** 用于区分是否为 client 组件 */
    @TableField("is_client")
    private Boolean client;

    /** 用于区分组件分配类型 master、slave */
    private String type;

    @TableField(exist = false)
    private List<String> hosts;
}