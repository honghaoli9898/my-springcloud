package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pengsong
 */
@Data
@TableName("sdps_document")
public class Document{
    private Integer id;
    @NotNull(message = "父id不允许为空")
    private Integer parentId;
    @NotNull(message = "文档名称不允许为空")
    private String documentName;
    private Integer isParent;
    private Integer documentLayer;
}
