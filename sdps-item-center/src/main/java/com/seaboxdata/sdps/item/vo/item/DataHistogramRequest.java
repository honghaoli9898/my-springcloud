package com.seaboxdata.sdps.item.vo.item;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataHistogramRequest {
	private String type;
	private String dateType;
	private Date startTime;
	private Date endTime;
}
