package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 集群服务表
 *
 * @author jiaohongtao
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sdps_cluster_service")
@Data
public class SdpsClusterService extends SuperEntity<SdpsClusterService> {
	private static final long serialVersionUID = -8260435958181127291L;
	/** 名称 */
    private String name;
    /** 集群ID */
    @TableField(value = "cluster_id")
    private Integer clusterId;
    /** 服务安装主机IP */
    private String host;
    /** 版本 */
    private String version;
    /** 描述 */
    private String remark;
}