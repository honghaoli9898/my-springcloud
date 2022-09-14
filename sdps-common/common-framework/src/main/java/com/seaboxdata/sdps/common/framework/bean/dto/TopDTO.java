package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDTO implements Serializable {

	private static final long serialVersionUID = 5410096868207127883L;
	private Long statValue;
    /**
     * 租户名
     */
    private String tenant;
    /**
     * 统计日期
     */
    private String dayTime;
}
