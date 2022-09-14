package com.seaboxdata.sdps.item.vo.item;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;

@Getter
@Setter
@ToString
public class ItemReourceRequest extends SdpsTenantResource {

	private static final long serialVersionUID = 1L;
	private List<YarnQueueConfInfo> infos;

}
