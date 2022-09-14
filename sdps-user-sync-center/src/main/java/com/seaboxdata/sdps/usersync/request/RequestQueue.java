package com.seaboxdata.sdps.usersync.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求内存队列
 * 
 * @author lihonghao
 *
 */
public class RequestQueue {
	private List<ArrayBlockingQueue<Request>> queues = new ArrayList<ArrayBlockingQueue<Request>>();

	private Map<Integer, Boolean> flagMap = new ConcurrentHashMap<Integer, Boolean>();

	private RequestQueue() {

	}

	/**
	 * 添加一个内存队列
	 */
	public void addQueue(ArrayBlockingQueue<Request> queue) {
		queues.add(queue);
	}

	/**
	 * 静态内部类实现单例
	 * 
	 * @author lihonghao
	 *
	 */
	private static class Singleton {
		private static RequestQueue instance;

		/*
		 * static{ instance = new RequestProcessorThreadPool(); }
		 */
		public static RequestQueue getInstance() {
			if (instance == null) {
				instance = new RequestQueue();
				return instance;
			} else {
				return instance;
			}
		}
	}

	public static RequestQueue getInstance() {
		return Singleton.getInstance();
	}

	/**
	 * 获取内存队列的数量
	 * 
	 * @return
	 */
	public int queueSize() {
		return queues.size();
	}

	public ArrayBlockingQueue<Request> getQueue(int index) {
		return queues.get(index);
	}

	public Map<Integer, Boolean> getFlagMap() {
		return flagMap;
	}
}
