package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class SdpsTableObj implements Serializable {

	private static final long serialVersionUID = 4691601163042741047L;
	private Long typeId;
    /**
     * 数据表英文名称
     */
    private String enName;
    /**
     * 数据表中文名称
     */
    private String cnName;
    /**
     * 关联项目id
     */
    private Long itemId;
    /**
     * 责任人id
     */
    private Long owner;
    /**
     * 关联数据库id
     */
    private Long databaseId;
    /**
     * 表描述
     */
    private String description;
    /**
     * 存活周期
     */
    private Integer lifeTime;
    /**
     * 是否外表
     */
    private Boolean isExternal;
    /**
     * 业务描述
     */
    private String busDesc;
    /**
     * 所属集群id
     */
    private Long clusterId;
    /**
     * 高级配置
     */
    private String senior;
    /**
     * 是否手动创建
     */
    private Boolean createMode;
    /**
     * sql
     */
    private String fieldSql;

}