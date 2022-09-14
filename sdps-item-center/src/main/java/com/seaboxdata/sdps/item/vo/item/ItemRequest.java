package com.seaboxdata.sdps.item.vo.item;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.item.vo.Request;

@Data
public class ItemRequest extends Request implements Serializable {
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
	private Boolean clusterIdNotNull;
	private String username;
	private Long userId;
	private List<YarnQueueConfInfo> infos;
}
