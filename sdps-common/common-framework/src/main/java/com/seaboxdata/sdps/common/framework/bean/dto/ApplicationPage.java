package com.seaboxdata.sdps.common.framework.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPage implements Serializable {

	private static final long serialVersionUID = 1766307982548500470L;

	private List<ApplicationDTO> applicationList;

    private Integer total;
}
