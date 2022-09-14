package com.seaboxdata.sdps.common.framework.bean.ambari;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Denny
 * @date: 2021/10/28 10:02
 * @desc: 调用ambari接口的实体类
 */
@Data
@TableName(value = "sdps_ambari_info")
public class SdpsAmbariInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ambariId标识
     */
    @TableId(value = "ambari_id",type= IdType.AUTO)
    private Integer ambariId;

    /**
     * ambari-server 主节点
     */
    @TableField(value = "ambari_host")
    private String ambariHost;

    /**
     * ambari-server 端口
     */
    @TableField(value = "ambari_port")
    private String ambariPort;

    /**
     * ambari 用户名
     */
    @TableField(value = "ambari_admin_user")
    private String ambariAdminUser;

    /**
     * ambari 密码
     */
    @TableField(value = "ambari_admin_password")
    private String ambariAdminPassword;

    /**
     * ambari主节点对应的集群ID
     */
    @TableField(value = "cluster_id")
    private Integer clusterId;
}
