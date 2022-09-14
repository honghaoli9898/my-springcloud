package com.seaboxdata.sdps.item.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.framework.bean.SdpsClusterHostService;

@Getter
@Setter
@ToString
@TableName(value = "sdps_cluster")
public class SdpsCluster1 implements Serializable {
    /**
     * 集群ID
     */
    @TableId(value = "cluster_id", type = IdType.AUTO)
    private Integer clusterId;
    /**
     * 集群名称(大数据集群的名称)
     */
    @TableField(value = "cluster_name")
    private String clusterName;
    /**
     * 集群展示名
     */
    @TableField(value = "cluster_show_name")
    private String clusterShowName;
    /**
     * 存储资源ID
     */
    @TableField(value = "storage_resource_id")
    private Integer storageResourceId;
    /**
     * 计算资源ID
     */
    @TableField(value = "calc_resource_id")
    private Integer calcResourceId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * 集群状态ID
     */
    @TableField(value = "cluster_status_id")
    private Integer clusterStatusId;
    /**
     * 集群来源
     */
    @TableField(value = "cluster_source")
    private String clusterSource;
    /**
     * 集群类型ID
     */
    @TableField(value = "cluster_type_id")
    private Integer clusterTypeId;
    /**
     * 责任人ID
     */
    @TableField(value = "principal_id")
    private Long principalId;
    /**
     * 是否启用
     */
    @TableField(value = "is_use")
    private Boolean isUse;
    /**
     * 集群IP
     */
    @TableField(value = "cluster_ip")
    private String clusterIp;
    /**
     * 集群端口
     */
    @TableField(value = "cluster_port")
    private Integer clusterPort;
    /**
     * 集群账户
     */
    @TableField(value = "cluster_account")
    private String clusterAccount;
    /**
     * 集群密码
     */
    @TableField(value = "cluster_passwd")
    private String clusterPasswd;
    /**
     * 集群描述
     */
    @TableField(value = "cluster_description")
    private String clusterDescription;
    /**
     * 集群Host配置
     */
    @TableField(value = "cluster_host_conf")
    private String clusterHostConf;
    /**
     * 集群配置存储路径
     */
    @TableField(value = "server_id")
    private String serverId;
    /**
     * 集群配置存储路径
     */
    @TableField(value = "cluster_conf_save_path")
    private String clusterConfSavePath;

    @TableField
    private Long createrId;
    @TableField
    private String remoteUrl;
    @TableField
    private Long menderId;
    @TableField
    private Date updateTime;
    @TableField("is_running")
    private Boolean running;

    @TableField(exist = false)
    private String runStatus;
    @TableField(exist = false)
    private List<SdpsClusterHostService> hostAndServiceList;

    @TableField(exist = false)
    private Long performOperationCount;
    @TableField(exist = false)
    private Long alarmCount;

    @TableField(exist = false)
    private String status;

}