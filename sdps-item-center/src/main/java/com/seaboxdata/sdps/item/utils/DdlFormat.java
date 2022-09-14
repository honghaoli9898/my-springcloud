package com.seaboxdata.sdps.item.utils;

import com.seaboxdata.sdps.item.model.SdpsTable;

/**
 * @author breezes_y@163.com
 * @date 2021/1/30 16:48
 * @description
 */
public interface DdlFormat {
	public String buildDdlFieldScript(SdpsTable table);
}
