package com.seaboxdata.sdps.usersync.thread;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.Getter;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.seaboxdata.sdps.usersync.properties.RequestProcessorThreadPoolPoperties;
import com.seaboxdata.sdps.usersync.request.Request;
import com.seaboxdata.sdps.usersync.request.RequestQueue;

/**
 * 线程池：单例
 * 
 * @author lihonghao
 *
 */
@Getter
public class RequestProcessorThreadPool {
	private ThreadPoolTaskExecutor threadPool;

	public RequestProcessorThreadPool(ThreadPoolTaskExecutor threadPool,
			RequestProcessorThreadPoolPoperties properties) {
		this.threadPool = threadPool;
		RequestQueue requestQueue = RequestQueue.getInstance();
		int threadNumber = 0;
		if (properties.getCorePoolSize() >= properties.getQueueNumber()) {
			threadNumber = properties.getCorePoolSize();
		} else if (properties.getCorePoolSize() < properties.getQueueNumber()
				&& properties.getMaxPoolSize() >= properties.getQueueNumber()) {
			threadNumber = properties.getMaxPoolSize();
		} else {
			properties.setQueueNumber(properties.getMaxPoolSize());
			threadNumber = properties.getMaxPoolSize();
		}
		threadNumber = threadNumber / properties.getQueueNumber();
		for (int i = 0; i < properties.getQueueNumber(); i++) {
			ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(
					properties.getBlockingQueueSize());
			requestQueue.addQueue(queue);
			for (int j = 0; j < threadNumber; j++) {
				this.threadPool.submit(new RequestProcessorThread(queue));
			}
		}
	}

}
