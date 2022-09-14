package com.seaboxdata.sdps.bigdataProxy.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ItemRequest implements Serializable {
	private static final long serialVersionUID = -3473131568131639127L;
	private String name;
	private Long id;
	private String iden;
	private String desc;
	private Date startTime;
	private Date endTime;
	private Long clusterId;
	private Boolean isDel;
	private List<Long> ids;
	private List<Long> roleIds;
	private Integer enabled;
}
