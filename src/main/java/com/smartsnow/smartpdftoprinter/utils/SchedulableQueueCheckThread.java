package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulableQueueCheckThread {
	private static Logger logger = LoggerFactory.getLogger(SchedulableQueueCheckThread.class);
	
	private SchedulableQueueCheckThread() {}
	public static <T,Q> void newCheckQueue(LinkedTransferQueue<Q> queue,Class<T> clazz) {
		newCheckQueue(queue, clazz,clazz.getSimpleName());
	}
	public static <T,Q> void newCheckQueue(LinkedTransferQueue<Q> queue,Class<T> clazz,String threadName) {
		ScheduledExecutorService scheduledLogger=Executors.newSingleThreadScheduledExecutor(
				SimpleThreadFactoryBuilder.buildWithAutoExit("CheckQueueSize-"+threadName));
		long threadId=Thread.currentThread().getId();
		scheduledLogger.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				int sz=queue.size();
				if(sz>0)
					logger.warn("{}-{} size={}",clazz.getSimpleName(),threadId,sz);
			}
		}, 0, 10, TimeUnit.SECONDS);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				scheduledLogger.shutdownNow();
			}
		}));
	}
	public static <E,K,N> void newCheckQueue(RouteGroupQueue<E, K> queue,Class<N> clazz) {
		newCheckQueue(queue,clazz,clazz.getSimpleName());
	}
	public static <E,K,N> void newCheckQueue(RouteGroupQueue<E, K> queue,Class<N> clazz,String threadName) {
		ScheduledExecutorService scheduledLogger=Executors.newSingleThreadScheduledExecutor(
				SimpleThreadFactoryBuilder.buildWithAutoExit("CheckQueueSize-"+threadName));
		long threadId=Thread.currentThread().getId();
		scheduledLogger.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				for(int i=0;i<queue.length();i++) {
					int sz=queue.size(i);
					if(sz>0)
						logger.warn("{}-{} size[{}]={}",clazz.getSimpleName(),threadId,i,sz);
				}
			}
		}, 0, 10, TimeUnit.SECONDS);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				scheduledLogger.shutdownNow();
			}
		}));
	}
}
