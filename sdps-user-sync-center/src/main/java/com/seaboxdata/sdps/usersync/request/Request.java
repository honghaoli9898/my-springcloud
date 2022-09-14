package com.seaboxdata.sdps.usersync.request;

/**
 * 请求接口
 * 
 * @author lihonghao
 *
 */
public interface Request {

	void process();

	Long getUserId();
}
