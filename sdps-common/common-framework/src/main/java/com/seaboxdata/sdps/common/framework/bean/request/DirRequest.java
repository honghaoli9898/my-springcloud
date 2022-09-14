package com.seaboxdata.sdps.common.framework.bean.request;

import com.seaboxdata.sdps.common.framework.enums.QueryEnum;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class DirRequest {
    /**
     * 集群id
     */
    @NonNull
    private Integer clusterId;
    /**
     * 集群名
     */
    private String clusterName;
    /**
     * 时间范围
     */
    private Integer durationDay;
    /**
     * 时间范围，week，month，year
     */
    private String period;
    /**
     * top N
     */
    private Integer topNum;
    /**
     * 1：文件容量   2：文件数量  3：小文件数量
     */
    private Integer type;
    /**
     * 存储类型集合
     */
    private List<Integer> storageTypeList;
    /**
     * 文件容量：TOTAL_FILE_SIZE 文件数量：TOTAL_FILE_NUM 小文件数量：TOTAL_SMALL_FILE_NUM
     */
    private String metric;
    /**
     * 排序的列
     */
    private String orderColumn;
    /**
     * 正序倒序，默认倒序
     */
    private Boolean desc = true;
    /**
     * 开始日期
     */
    private String startDay;
    /**
     * 结束日期
     */
    private String endDay;
    /**
     * 租户名集合
     */
    private List<String> tenants;
    /**
     * 是否统计全量
     */
    private Boolean isTotal;

    private Integer pageSize;

    private Integer pageNo;
    /**
     * 项目名
     */
    private String tenant;
    /**
     * 路径名
     */
    private String path;
    /**
     * 库名
     */
    private String dbName;
    /**
     * 表名
     */
    private String table;
    /**
     *
     */
    private Integer pathIndex;
    /**
     * 路径深度
     */
    private Integer pathDepth;
    /**
     * 子路径
     */
    private String subPath;
    /**
     * 路径、库、表
     */
    private QueryEnum storageType;
    /**
     * 库类型：hive、hive外表、hbase
     */
    private String category;
    
    private Boolean isStartDay;
    
    private List<String> paths;

    public Integer getPathIndex() {
        int index = StringUtils.isBlank(this.path) ? 0 : this.path.split("/").length;
        return this.pathIndex != null ? this.pathIndex : index;
    }

    /**
     * 是否获取当前最近的有效时间，默认为false
     */
    private Boolean getCurrentDate = false;
}
