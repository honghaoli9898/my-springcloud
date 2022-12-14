package com.seaboxdata.sdps.common.ribbon.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

@Slf4j
@Primary
@ConditionalOnProperty(prefix = "feign.hystrix", name = "enabled", havingValue = "true")
public class CustomFeignHystrixConcurrencyStrategy extends
		HystrixConcurrencyStrategy {

	private HystrixConcurrencyStrategy hystrixConcurrencyStrategy;

	public CustomFeignHystrixConcurrencyStrategy() {
		try {
			this.hystrixConcurrencyStrategy = HystrixPlugins.getInstance()
					.getConcurrencyStrategy();
			if (this.hystrixConcurrencyStrategy instanceof CustomFeignHystrixConcurrencyStrategy) {
				return;
			}
			HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins
					.getInstance().getCommandExecutionHook();
			HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance()
					.getEventNotifier();
			HystrixMetricsPublisher metricsPublisher = HystrixPlugins
					.getInstance().getMetricsPublisher();
			HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins
					.getInstance().getPropertiesStrategy();
			this.logCurrentStateOfHystrixPlugins(eventNotifier,
					metricsPublisher, propertiesStrategy);
			HystrixPlugins.reset();
			HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
			HystrixPlugins.getInstance().registerCommandExecutionHook(
					commandExecutionHook);
			HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
			HystrixPlugins.getInstance().registerMetricsPublisher(
					metricsPublisher);
			HystrixPlugins.getInstance().registerPropertiesStrategy(
					propertiesStrategy);
		} catch (Exception e) {
			log.error("Failed to register Sleuth Hystrix Concurrency Strategy",
					e);
		}
	}

	private void logCurrentStateOfHystrixPlugins(
			HystrixEventNotifier eventNotifier,
			HystrixMetricsPublisher metricsPublisher,
			HystrixPropertiesStrategy propertiesStrategy) {
		if (log.isDebugEnabled()) {
			log.info("Current Hystrix plugins configuration is ["
					+ "concurrencyStrategy [" + this.hystrixConcurrencyStrategy
					+ "]," + "eventNotifier [" + eventNotifier + "],"
					+ "metricPublisher [" + metricsPublisher + "],"
					+ "propertiesStrategy [" + propertiesStrategy + "]," + "]");
			log.info("Registering Sleuth Hystrix Concurrency Strategy.");
		}
	}

	@Override
	public <T> Callable<T> wrapCallable(Callable<T> callable) {
		RequestAttributes requestAttributes = RequestContextHolder
				.getRequestAttributes();
		return new WrappedCallable<>(callable, requestAttributes);
	}

	@Override
	public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
			HystrixProperty<Integer> corePoolSize,
			HystrixProperty<Integer> maximumPoolSize,
			HystrixProperty<Integer> keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		return this.hystrixConcurrencyStrategy.getThreadPool(threadPoolKey,
				corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
			HystrixThreadPoolProperties threadPoolProperties) {
		return this.hystrixConcurrencyStrategy.getThreadPool(threadPoolKey,
				threadPoolProperties);
	}

	@Override
	public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
		return this.hystrixConcurrencyStrategy.getBlockingQueue(maxQueueSize);
	}

	@Override
	public <T> HystrixRequestVariable<T> getRequestVariable(
			HystrixRequestVariableLifecycle<T> rv) {
		return this.hystrixConcurrencyStrategy.getRequestVariable(rv);
	}

	static class WrappedCallable<T> implements Callable<T> {
		private final Callable<T> target;
		private final RequestAttributes requestAttributes;

		public WrappedCallable(Callable<T> target,
				RequestAttributes requestAttributes) {
			this.target = target;
			this.requestAttributes = requestAttributes;
		}

		/**
		 * feign opens the fuse (hystrix): feign.hystrix.enabled=ture, and uses
		 * the default signal isolation level, The HttpServletRequest object is
		 * independent of each other in the parent thread and the child thread
		 * and is not shared. So the HttpServletRequest data of the parent
		 * thread used in the child thread is null, naturally it is impossible
		 * to obtain the token information of the request header In a
		 * multithreaded environment, call before the request, set the context
		 * before the call
		 *
		 * @return T
		 * @throws Exception
		 *             Exception
		 */
		@Override
		public T call() throws Exception {
			try {
				// Set true to share the parent thread's HttpServletRequest
				// object setting
				RequestContextHolder.setRequestAttributes(requestAttributes,
						true);
				return target.call();
			} finally {
				RequestContextHolder.resetRequestAttributes();
			}
		}
	}
}