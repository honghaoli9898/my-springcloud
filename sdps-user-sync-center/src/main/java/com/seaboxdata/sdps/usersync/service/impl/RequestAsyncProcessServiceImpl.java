package com.seaboxdata.sdps.usersync.service.impl;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.usersync.request.Request;
import com.seaboxdata.sdps.usersync.request.RequestQueue;
import com.seaboxdata.sdps.usersync.service.RequestAsyncProcessService;

/**
 * 请求异步处理的service实现
 * 
 * @author lihonghao
 *
 */
@Slf4j
@Service("requestAsyncProcessService")
public class RequestAsyncProcessServiceImpl implements
		RequestAsyncProcessService {

	@Override
	public void process(Request request) {
		try {
			ArrayBlockingQueue<Request> queue = getRoutingQueue(request
					.getUserId());
			queue.put(request);
		} catch (InterruptedException e) {
			log.error("存放用ID={},报错", request.getUserId(), e);
		}
	}

	private ArrayBlockingQueue<Request> getRoutingQueue(Long clueterId) {
		RequestQueue requestQueue = RequestQueue.getInstance();
		String key = String.valueOf(clueterId);
		int h;
		int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
		int index = (requestQueue.queueSize() - 1) & hash;
		log.info("路由内存队列,用户ID={},队列索引={}", clueterId, index);
		return requestQueue.getQueue(index);
	}
}
