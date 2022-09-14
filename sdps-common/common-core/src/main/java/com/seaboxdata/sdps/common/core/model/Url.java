package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pengosng
 * url 链接管理实体
 */
@Data
@TableName("sdps_url")
public class Url extends BasicObject{
    private Long id;
    @NotNull(message = "链接名称不允许为空")
    private String name;
    @NotNull(message = "链接地址不允许为空")
    private String url;
    private String description;
    private Integer status;
    private Integer verifyStatus;
    private Integer isApply;
}
