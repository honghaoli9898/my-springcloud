package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data
@ToString
public class HiveTableDTO implements Serializable {
	private static final long serialVersionUID = 5946636666275181504L;
	/**
     * 外表分区字段前缀
     */
    private static final String EXTERNAL_HIVE_PARTITION_KEY = "p_";
    /**
     * 表名
     */
    private String tableName;
    /**
     * 库名
     */
    private String dbName;
    /**
     * 所属用户
     */
    private String owner;
    /**
     * 创建时间
     */
    private Integer createTime;
    /**
     * 上一次访问时间
     */
    private Integer lastAccessTime;
    /**
     * 过期时间
     */
    private Integer retention;
    /**
     * 表所在hdfs路径
     */
    private String pathLocation;
    /**
     * 分区列集合
     */
    private List<String> partKeys;
    /**
     * 第一个分区字段
     */
    private String partKey;
    /**
     * 是否以日期作为分区
     */
    private Boolean datePartition = false;
    /**
     * 日期格式
     */
    private String dateFormat;
    /**
     *
     */
    private String datePattern;
    private List<String> tablePaths = new ArrayList();
    /**
     * 表数据输入类型
     */
    private String inputFormat;
    /**
     * 表数据输出类型
     */
    private String outputFormat;
    /**
     * 表格式
     */
    private String format;
    /**
     * 压缩算法
     */
    private String codec;

    public String getFormat() {
        String format = null;
        String formatClass = this.outputFormat;
        if (formatClass.contains("HiveIgnoreKeyTextOutputFormat")) {
            format = "TEXT";
        }

        if (formatClass.contains("OrcOutputFormat")) {
            format = "ORC";
        }

        if (formatClass.contains("HiveSequenceFileOutputFormat")) {
            format = "SEQ";
        }

        if (formatClass.contains("AvroContainerOutputFormat")) {
            format = "AVRO";
        }

        if (formatClass.contains("MapredParquetOutputFormat")) {
            format = "PARQUET";
        }

        return format;
    }

    public String getParKeyPrefix(String category) {
        String partitionPrefix = null;
        if ("hive".equalsIgnoreCase(category) && CollectionUtils.isNotEmpty(this.getPartKeys())) {
            partitionPrefix = this.getPartKey() + "=";
        }

        if ("hive_external".equalsIgnoreCase(category)) {
            partitionPrefix = "p_";
        }

        return partitionPrefix;
    }
}
