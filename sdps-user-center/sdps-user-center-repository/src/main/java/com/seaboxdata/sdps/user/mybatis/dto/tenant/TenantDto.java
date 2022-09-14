package com.seaboxdata.sdps.user.mybatis.dto.tenant;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import cn.hutool.core.collection.CollUtil;

import com.seaboxdata.sdps.common.core.model.SdpsTenant;
import com.seaboxdata.sdps.common.core.model.SysUser;

@Getter
@Setter
@ToString
public class TenantDto extends SdpsTenant {
	private static final long serialVersionUID = 1L;
	
	private List<TenantDto> subDto;

	private Set<String> userIdSet = CollUtil.newHashSet();

	private Set<String> itemIdSet = CollUtil.newHashSet();
	
	private String userIds;

	private String itemIds;

	private String users;

	private SdpsTenant parentTenant;

	private Long userId;
	
	private Integer userCnt = 0;

	private Integer itemCnt = 0;

	private Long assClusterId;

	private String assClusterName;

	private String hdfsDir;

	private Long spaceQuota;

	private Long fileNumQuota;

	private String resourceFullName;

	private String resourceName;

	private Integer maxCore;

	private Integer maxMemory;

	private Integer maxAppNum;

	private Integer maxAppMasterRatio;

	private Integer weight;

	private String type;
	
	private String resourceType;

	private Long itemId;

	private Long itemSpaceSum;
	
	private Long usedSpace = 0L;
	
	private Integer usedCore = 0;
	
	private Long resourceId;

}
