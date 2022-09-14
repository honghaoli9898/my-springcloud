package com.seaboxdata.sdps.webssh.user.mybatis.vo;

import java.util.List;
import lombok.Data;

@Data
public class MessageVo{

    private Long userId;

    private String message;

    private List<Long> ids;

    private String type;

    private Integer isRead;
}
