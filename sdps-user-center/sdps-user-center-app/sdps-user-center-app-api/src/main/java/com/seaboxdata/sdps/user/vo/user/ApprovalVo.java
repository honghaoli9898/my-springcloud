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
public class ApprovalVo implements Serializable {


    // 申请id
    @NotNull(message = "spId不能为空")
    private Long spId;

    // 申请人
    private Integer action;


    // 申请原因
    @NotEmpty
    private String spReason;

    @NotNull
    private String name;

    private String role;

    private Integer type;

    private Long roleId;



}
