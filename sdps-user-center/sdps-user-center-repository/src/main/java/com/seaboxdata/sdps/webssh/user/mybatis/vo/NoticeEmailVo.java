package com.seaboxdata.sdps.webssh.user.mybatis.vo;

import java.util.List;
import lombok.Data;

@Data
public class NoticeEmailVo{
    private Integer emailTime;
    private Integer emailPeriod;
    private List<String> emailUser;
}
