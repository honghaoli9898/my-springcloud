package com.seaboxdata.sdps.common.core.model;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author pengsong
 */
@Data
public class UrlVo{
    private Long id;
    private String username;
    private Long roleId;
    private String url;
    private String name;
    private String description;
    private Integer verifyStatus;
    private Date createTime;
}
