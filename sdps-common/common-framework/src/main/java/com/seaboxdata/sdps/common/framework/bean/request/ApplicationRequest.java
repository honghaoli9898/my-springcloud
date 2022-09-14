package com.seaboxdata.sdps.common.framework.bean.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class ApplicationRequest implements Serializable {
	private static final long serialVersionUID = 2321709901093263548L;
	@NotNull
    private Integer clusterId;
    /**
     * 任务状态
     */
    private List<String> states;
    /**
     * 用户
     */
    private String user;

    private Integer pageSize = 20;

    private Integer pageNo = 1;
}
