package com.seaboxdata.sdps.common.core.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页实体类
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = -275582248840137389L;
    /**
     * 总数
     */
    private Long count;
    /**
     * 是否成功：0 成功、1 失败
     */
    private int code;
    /**
     * 当前页结果集
     */
    private List<T> data;
    
    private String msg;

    @SuppressWarnings("unchecked")
    public static PageResult<?> success(Long count, List<?> data) {
        return builder().code(0).count(count).msg("操作成功").data((List<Object>) data).build();
    }
}
