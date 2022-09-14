package com.seaboxdata.sdps.user.vo.tenant;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
@Getter
@Setter
public class TenantResourceVo extends SdpsTenantResource{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<YarnQueueConfInfo> infos;
	
	private List<Long> userIds;

}
