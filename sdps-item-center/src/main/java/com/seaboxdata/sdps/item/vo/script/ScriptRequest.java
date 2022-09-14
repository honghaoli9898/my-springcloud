package com.seaboxdata.sdps.item.vo.script;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptRequest implements Serializable {
	private static final long serialVersionUID = -3473131568131639127L;
	private Integer clusterId;
	private Long itemId;
	private String fileName;
	private String serverType;
	private String type;
	private List<Long> ids;
	private Long userId;
}
