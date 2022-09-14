package com.seaboxdata.sdps.item.vo.item;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.common.core.model.SdpsItem;

@Getter
@Setter
@ToString
public class CreateItemVo implements Serializable {
	private static final long serialVersionUID = 6091279491827623240L;
	private SdpsItem itemInfo;
	private ItemReourceRequest resourcesInfo;
}
