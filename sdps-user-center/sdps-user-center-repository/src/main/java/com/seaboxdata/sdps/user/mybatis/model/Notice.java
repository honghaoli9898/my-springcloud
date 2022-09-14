package com.seaboxdata.sdps.user.mybatis.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.List;
import lombok.Data;

/**
 * @author pengsong
 */
@Data
@TableName("sdps_notice")
public class Notice{
    private Long id;
    private Integer interTime;
    private Integer interPeriod;
    @TableField("inter_user")
    private String interUser;
    private Integer emailTime;
    private Integer emailPeriod;
    @TableField("email_user")
    private String emailUser;
}
