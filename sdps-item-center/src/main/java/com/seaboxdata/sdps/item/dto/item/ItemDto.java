package com.seaboxdata.sdps.item.dto.item;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.common.core.model.SysUser;

@Getter
@Setter
@ToString
public class ItemDto implements Serializable {
	private static final long serialVersionUID = 7708869432626444637L;
	private Long id;
	private String name;
	private String iden;
	private List<SysUser> itemManagers;
	private Integer members;
	private Long clusterId;
	private Date createTime;
	private Long userId;
	private List<ItemDto> datas;
	private String nickname;
	private Long groupId;
	private String clusterName;
	private String desc;
	private Boolean isSecurity;
	private List<Long> ids;
	private Integer userCnt;
	private Long tenantId;
	private String tenantName;
}
