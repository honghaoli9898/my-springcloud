package com.seaboxdata.sdps.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 翻页请求对象
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/09
 */
@Data
public class SimplePageRequest {
    /** 第几页 */
    @NotNull(message = "每页大小不能为空")
    private Integer page;
    /** 显示个数 */
    @NotNull(message = "页数不能为空")
    private Integer size;
    /** JSON对象参数 {"name": "xxName", "status": false...} */
    private JSONObject param;
}
