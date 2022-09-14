package com.seaboxdata.sdps.bigdataProxy.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DevOpsRequest implements Serializable {
	private static final long serialVersionUID = -7659634385902508886L;
	private List<Integer> clusterIds;
	private Long clusterId;
	private String type;
}
