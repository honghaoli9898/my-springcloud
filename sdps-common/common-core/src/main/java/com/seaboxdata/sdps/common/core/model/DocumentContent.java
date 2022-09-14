package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pengsong
 */
@Data
@TableName("sdps_document_content")
public class DocumentContent{
    private Integer id;
    @NotNull(message = "documentId不允许为空")
    private Integer documentId;
    private String content;
    private Date creatTime;
    private String creator;
    private Integer count;
}
