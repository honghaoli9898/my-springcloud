package com.seaboxdata.sdps.webssh.user.mybatis.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import java.util.List;
import lombok.Data;

@Data
public class NoticeInterVo{
    private Integer interTime;
    private Integer interPeriod;
    private List<Long> interUser;

}
