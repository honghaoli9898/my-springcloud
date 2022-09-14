package com.seaboxdata.sdps.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 翻页请求对象
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
@Data
public class PageRequestBean  {
    /**
     * 第几页
     */
    @NotNull(message = "每页大小不能为空")
    private Integer page;
    /**
     * 显示个数
     */
    @NotNull(message = "页数不能为空")
    private Integer size;
    /**
     * 排序字段 ["name",...]
     */
    private List<String> sorts;
    /**
     * 对象参数List [{"name": "xxName", "value": "123", "type":"eq"},...]
     */
    private List<JSONObject> params;
}
