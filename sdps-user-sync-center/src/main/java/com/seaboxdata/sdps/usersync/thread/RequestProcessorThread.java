package com.seaboxdata.sdps.usersync.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

import com.seaboxdata.sdps.usersync.request.Request;

/**
 * 执行请求的工作线程
 * 
 * @author lihonghao
 *
 */
@Slf4j
public class RequestProcessorThread implements Callable<Boolean> {
	/**
	 * 自己监控的内存队列
	 */
	private ArrayBlockingQueue<Request> queue;

	public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
		this.queue = queue;
	}

	@Override
	public Boolean call() throws Exception {
		Request request = null;
		try {
			while (true) {
				if (queue.size() > 0) {
					request = queue.take();
					log.info("工作线程处理请求,用户ID=" + request.getUserId());
					request.process();
				} else {
					Thread.sleep(100);
				}
			}
		} catch (Exception e) {
			log.error("监听队列异常", e);
		}

		return true;
	}

	public ArrayBlockingQueue<Request> getQueue() {
		return queue;
	}

	public void setQueue(ArrayBlockingQueue<Request> queue) {
		this.queue = queue;
	}

}
