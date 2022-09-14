package com.seaboxdata.sdps.item.model;

import lombok.Data;

@Data
public class HiveDelimiter {
	private String fieldDiv;
	private String arrayDiv;
	private String mapDiv;
	private String fileFormat;
}
