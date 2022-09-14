package com.seaboxdata.sdps.common.framework.bean.yarn;

import java.io.Serializable;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author: Denny
 * @date: 2021/10/13 17:53
 * @desc: 实体类
 */
@Data
@TableName(value = "sdps_yarn_info")
public class SdpsYarnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "yarn_id",type = IdType.AUTO)
    private Integer yarnId;

    /**
     * 队列名
     */
    @TableField(value = "yarn_host")
    private String yarnHost;

    /**
     * 队列容量
     */
    @TableField(value = "yarn_port")
    private Integer yarnPort;

    /**
     * 用户容量限制
     */
    @TableField(value = "yarn_admin_user")
    private Integer yarnAdminUser;

    /**
     * 集群ID
     */
    @TableField(value = "cluster_id")
    private Integer clusterId;

}
