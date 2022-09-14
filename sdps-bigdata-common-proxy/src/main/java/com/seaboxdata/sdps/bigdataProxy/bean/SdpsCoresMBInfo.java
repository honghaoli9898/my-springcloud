package com.seaboxdata.sdps.bigdataProxy.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Denny
 * @date: 2021/11/12 17:45
 * @desc:
 */
@Data
@TableName(value = "sdps_cores_mb_info")
@NoArgsConstructor
@AllArgsConstructor
public class SdpsCoresMBInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "resource_id",type = IdType.AUTO)
    private Integer resourceId;

    /**
     * 节点ID
     */
    @TableField(value = "node_id")
    private String nodeId;

    /**
     * 总共内存
     */
    @TableField(value = "total_memory")
    private Integer totalMemory;

    /**
     * 使用内存
     */
    @TableField(value = "used_memory")
    private Integer usedMemory;

    /**
     * 总共核数
     */
    @TableField(value = "total_cores")
    private Integer totalCores;

    /**
     * 使用核数
     */
    @TableField(value = "used_cores")
    private Integer usedCores;

    /**
     * 保存时间
     */
    @TableField(value = "save_time")
    private Long saveTime;

    /**
     * 集群ID
     */
    @TableField(value = "cluster_id")
    private Integer clusterId;

    public SdpsCoresMBInfo(String nodeId, Integer clusterId) {
        this.nodeId = nodeId;
        this.clusterId = clusterId;
    }
}
