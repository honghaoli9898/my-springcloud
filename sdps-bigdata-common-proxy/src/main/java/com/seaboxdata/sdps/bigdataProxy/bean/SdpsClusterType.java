package com.seaboxdata.sdps.bigdataProxy.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "sdps_cluster_type")
@Data
public class SdpsClusterType implements Serializable {
    /**
     * 集群类型ID
     */
    @TableId(value = "cluster_type_id",type = IdType.AUTO)
    private Integer clusterTypeId;

    /**
     * 集群类型英文名称
     */
    @TableField(value = "cluster_type_en_name")
    private String clusterTypeEnName;

    /**
     * 集群类型中文名称
     */
    @TableField(value = "cluster_type_cn_name")
    private String clusterTypeCnName;

    private static final long serialVersionUID = 1L;

}