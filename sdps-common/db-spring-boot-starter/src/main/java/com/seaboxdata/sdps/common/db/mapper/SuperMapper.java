package com.seaboxdata.sdps.common.db.mapper;

import java.util.Collection;

import com.github.yulichang.base.MPJBaseMapper;

/**
 * mapper 父类，注意这个类不要让 mp 扫描到！！
 */
public interface SuperMapper<T> extends MPJBaseMapper<T> {
	// 这里可以放一些公共的方法
	Integer insertBatchSomeColumn(Collection<T> entityList);
}
