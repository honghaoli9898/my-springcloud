package com.seaboxdata.sdps.common.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 基础工具类
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/10
 */
public class MetaTool {
    /**
     * 驼峰转换
     * eq: clusterId -> cluster_id
     *
     * @param filed 字段
     */
    public static String fieldToColumn(String filed) {
        if (StringUtils.isEmpty(filed)) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder(filed);
            int count = 0;
            builder.replace(0, 1, (filed.charAt(0) + "").toLowerCase());

            for (int i = 1; i < filed.length(); ++i) {
                char c = filed.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    builder.replace(i + count, i + count + 1, (c + "").toLowerCase());
                    builder.insert(i + count, "_");
                    ++count;
                }
            }

            return builder.toString();
        }
    }
}
