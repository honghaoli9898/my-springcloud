package com.seaboxdata.sdps.common.oauth2.properties;

import java.util.List;

import lombok.Data;
import cn.hutool.core.collection.CollUtil;

@Data
public class CorsAllowProperties {
	private List<String> allows = CollUtil.newArrayList();
}
