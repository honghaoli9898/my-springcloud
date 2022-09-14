package com.seaboxdata.sdps.usersync.service;

import com.seaboxdata.sdps.usersync.request.Request;

/**
 * 请求异步执行的sercice
 * 
 * @author lihonghao
 *
 */
public interface RequestAsyncProcessService {
	void process(Request request);
}
