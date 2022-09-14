package com.seaboxdata.sdps.common.framework.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据热度统计表实例
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "sdps_hdfs_file_stats")
public class HdfsFileStats implements Serializable {

	private static final long serialVersionUID = 2422636080731244851L;
	@TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 集群id
     */
    @TableField(value = "cluster_id")
    private Long clusterId;
    /**
     * 集群名
     */
    @TableField(value = "cluster")
    private String cluster;
    /**
     * 统计时间
     */
    @TableField(value = "day_time")
    private String dayTime;
    /**
     * 类型：1.热度(temp) 2.大小(size)
     */
    @TableField(value = "type")
    private String type;
    /**
     * 统计维度 热度(TYPE_ERROR,TEMP_ONE_MONTH, TEMP_THREE_MONTH, TEMP_HALF_YEAR, TEMP_ONE_YEAR,TEMP_TWO_YEAR,TEMP_THREE_YEAR,TEMP_OTHERS)
     * 大小(SIZE_EMPTY, SIZE_64M, SIZE_128M, SIZE_256M, SIZE_512M, SIZE_1G, SIZE_OTHERS)
     */
    @TableField(value = "type_key")
    private String typeKey;
    /**
     * 存储量，单位B
     */
    @TableField(value = "type_value_num")
    private Long typeValueNum;
    /**
     * 存储文件数
     */
    @TableField(value = "type_value_size")
    private Long typeValueSize;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;
}
