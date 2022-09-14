package com.seaboxdata.sdps.seaboxProxy.constants;

import java.util.Map;

import org.apache.hadoop.security.UserGroupInformation;

import cn.hutool.core.map.MapUtil;

/**
 * 数据源类型
 */
public class CommonConstants {
    /**
     * 反引号
     */
    public static final String BACK_QUOTE = "`";

    /**
     * 双引号
     */
    public static final String DOUBLE_QUOTATION = "\"";
    
    public static final Map<Integer, UserGroupInformation> kerberosMap = MapUtil
			.newConcurrentHashMap();
}
