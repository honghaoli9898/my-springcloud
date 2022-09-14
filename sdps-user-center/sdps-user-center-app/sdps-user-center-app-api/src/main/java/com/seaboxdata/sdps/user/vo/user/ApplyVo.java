package com.seaboxdata.sdps.user.vo.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class ApplyVo implements Serializable {

    // 申请内容
    @NotNull
    private Integer sqId;

    // 申请原因
    @NotEmpty
    private String applyReason;

}
