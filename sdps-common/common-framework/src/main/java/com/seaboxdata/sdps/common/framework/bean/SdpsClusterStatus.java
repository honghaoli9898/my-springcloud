package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 集群状态表
 *
 * @author jiaohongtao
 */
@TableName(value = "sdps_cluster_status")
@Data
public class SdpsClusterStatus implements Serializable {
	private static final long serialVersionUID = -3969236699258169218L;

	/** 集群状态ID */
    @TableId(type = IdType.AUTO)
    private Integer clusterStatusId;

    /** 集群状态码 */
    private Integer statusCode;

    /** 集群状态名 */
    private String statusName;
}