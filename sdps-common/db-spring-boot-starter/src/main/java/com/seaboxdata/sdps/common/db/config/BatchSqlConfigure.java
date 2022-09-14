package com.seaboxdata.sdps.common.db.config;

import java.util.List;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

public class BatchSqlConfigure extends DefaultSqlInjector {
	@Override
	public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
		// 防止父类方法不可用
		List<AbstractMethod> methodList = super.getMethodList(mapperClass,tableInfo);
		methodList.add(new InsertBatchSomeColumn());
		return methodList;
	}
}