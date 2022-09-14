package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@TableName(value = "sdps_cluster")
@Data
public class SdpsCluster implements Serializable {
    private static final long serialVersionUID = 1L;
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
    
    @TableField(value = "cluster_type",exist = false)
    private String clusterType;
    
    @TableField(value = "kerberos")
    private Boolean kerberos;
    
    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public void setRunStatus(Boolean runStatus) {
        this.runStatus = runStatus ? RunStatus.START.getStatusName() : RunStatus.STOP.getStatusName();
    }

    /** 集群状态 */
    public enum Status {
        /** 不可用 */
        NOT_USE(1, 0, "不可用"),
        /** 正常 */
        OK(2, 1, "正常"),
        /** 失败 */
        FAILED(3, 2, "失败"),
        /** 正在初始化 */
        INIT(4, 3, "正在初始化"),
        /** 配置失败 */
        CONFIG_FAILED(6, 5, "配置失败"),
        /** 正在部署 */
        DEPLOYING(5, 4, "正在部署"),
        /** 正在删除 */
        DELETING(7, 6, "正在删除"),
        /** 删除失败 */
        DELETE_FAILED(8, 7, "删除失败"),
        AMBARI_INSTALLED(9, 8, "部署成功"),
        AMBARI_FAILED(10, 9, "部署失败");

        int id;
        int code;
        String name;

        Status(int id, int code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /** 集群类型 */
    public enum Type {
        /** 海盒大数据平台 */
        SEABOX(1, "seabox", "海盒大数据平台"),
        /** 腾讯大数据平台 */
        TBDS(1, "tbds", "腾讯大数据平台"),
        /** 华为大数据平台 */
        FUSION_INSIGHT(1, "fusionInsight", "华为大数据平台");
        private int id;
        private String enName;
        private String cnName;

        Type(int id, String enName, String cnName) {
            this.id = id;
            this.enName = enName;
            this.cnName = cnName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public String getCnName() {
            return cnName;
        }

        public void setCnName(String cnName) {
            this.cnName = cnName;
        }
    }

    /** 运行状态 */
    public enum RunStatus {
        /** 启用 */
        START(true, "启用"),
        /** 停用 */
        STOP(false, "停用");

        private Boolean status;
        private String statusName;

        RunStatus(Boolean status, String statusName) {
            this.status = status;
            this.statusName = statusName;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public String getStatusName() {
            return statusName;
        }

        public void setStatusName(String statusName) {
            this.statusName = statusName;
        }
    }
}