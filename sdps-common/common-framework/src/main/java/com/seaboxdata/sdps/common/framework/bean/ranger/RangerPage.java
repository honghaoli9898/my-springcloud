package com.seaboxdata.sdps.common.framework.bean.ranger;

import lombok.Data;

@Data
public class RangerPage {
    /**
     * 开始位置
     */
    Integer startIndex;
    /**
     * 分页大小
     */
    Integer pageSize;
    /**
     * 总数
     */
    Integer totalCount;
    /**
     * 结果数量
     */
    Integer resultSize;
    /**
     * 排序类型（正序：asc，倒序：desc）；必须带排序字段才能生效
     */
    String sortType;
    /**
     * 排序字段
     */
    String sortBy;
    /**
     * 查询时间
     */
    String queryTimeMS;
}
