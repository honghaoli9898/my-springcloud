package com.seaboxdata.sdps.bigdataProxy.util;

import java.util.List;

/**
 * List工具
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/08
 */
public class ListUtil {
    public static boolean isEmpty(List<?> list) {
        return !isNotEmpty(list);
    }

    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
