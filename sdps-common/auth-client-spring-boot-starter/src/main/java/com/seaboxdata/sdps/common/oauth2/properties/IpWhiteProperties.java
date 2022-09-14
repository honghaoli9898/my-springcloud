package com.seaboxdata.sdps.common.oauth2.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;
import cn.hutool.core.map.MapUtil;

@Data
public class IpWhiteProperties {
	private Map<String, List<String>> resource = MapUtil.newHashMap();
}
