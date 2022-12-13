package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsnow.smartpdftoprinter.utils.ExecutorsUtil.ThreadWorkerPool;


/**
 * 对路由队列和线程对象进一步封装.
 * K是事件类;K是路由标识字段的类型
 * */
public class RouteGroupQueueThreadPool<K,V> {
	private RouteGroupQueue<K,V> routeGroupQueue=null;
	private ThreadWorkerPool threadWorkerPool=null;
	private RouteGroupQueueWorker<V>[] subConsumers=null;
	private TimeUnit pollTimeUnit=TimeUnit.SECONDS;
	private long pollTime=5;
	
	public static interface RouteGroupQueueWorker<E> {
		void setRouteId(int routeId);
		void doEvent(E e) throws Exception;
		void end();
		default public boolean isNeedNotify() {
			return false;
		}
	}
	public void setPollTimeOut(long pollTime,TimeUnit pollTimeUnit) {
		this.pollTime=pollTime;
		this.pollTimeUnit=pollTimeUnit;	
	}
	public void stop() {
		if(threadWorkerPool!=null) {
			threadWorkerPool.stop();
			threadWorkerPool=null;
		}
	}
	public RouteGroupQueueWorker<V>[] getRouteGroupThreads(){
		return subConsumers;
	}
	public void start(RouteGroupQueueWorker<V>[] workers,String threadName) {
		start(workers, threadName, true);
	}
	
	/**
	 * @param workers 事件处理封装好的对象 
	 * @param threadName 线程名前缀
	 * @param isRunForOneThread 一个线程时是否继续创建线程池
	 * */
	private void start(RouteGroupQueueWorker<V>[] workers,String threadName,boolean isRunForOneThread) {
		this.subConsumers=workers;
		if(!isRunForOneThread&&this.subConsumers.length<=1)
			return;
		if(routeGroupQueue==null) {
			routeGroupQueue=new RouteGroupQueue<K, V>(this.subConsumers.length);
		}
		threadWorkerPool=ExecutorsUtil.getPool(threadName, this.subConsumers.length);
		threadWorkerPool.start();
		SubConsumer<K,V> sConsumer=null;
		for(int i=0;i<this.subConsumers.length;i++) {
			this.subConsumers[i].setRouteId(i);
			sConsumer=new SubConsumer<>();
			sConsumer.bind(this.subConsumers[i],routeGroupQueue, i);
			sConsumer.setPollTimeOut(pollTime, pollTimeUnit);
			threadWorkerPool.submit(sConsumer);
		}
//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if(threadWorkerPool!=null) {
//					threadWorkerPool.stop();
//				}
//			}
//		}));
	}
	public RouteGroupQueue<K,V> getQueue(){
		return routeGroupQueue;
	}
	public boolean offerToQueue(K key,V t) {
		return routeGroupQueue.offer(key, t);
	}
	public boolean offerToQueue(K key,V t,long timeout, TimeUnit unit) {
		return routeGroupQueue.offer(key, t,timeout,unit);
	}
	private static class SubConsumer<K,E> implements SafeRunnable{
		private static Logger logger = LoggerFactory.getLogger(SubConsumer.class);
		private RouteGroupQueueWorker<E> subConsumer=null;
		private volatile boolean isExit=false;
		private volatile int routeId=-1;
		private RouteGroupQueue<K,E> routeGroupQueue=null;
		private TimeUnit pollTimeUnit;
		private long pollTime;
		
		public void setPollTimeOut(long pollTime,TimeUnit pollTimeUnit) {
			this.pollTime=pollTime;
			this.pollTimeUnit=pollTimeUnit;	
		}
		/**
		 * 需要绑定处理类,路由分组队列,路由标识
		 * */
		public void bind(RouteGroupQueueWorker<E> subConsumer,
				RouteGroupQueue<K,E> routeGroupQueue,
				int routeId) {
			this.subConsumer=subConsumer;
			this.routeId=routeId;
			this.routeGroupQueue=routeGroupQueue;
		}
		@Override
		public void run() {
			E event=null;
			while(true) {
				try {
					event=routeGroupQueue.poll(routeId,pollTime,pollTimeUnit);
				} catch (InterruptedException e1) {
					logger.error("EEEEEEEEEEEEEEEEEEEE:InterruptedException",e1);
					continue;
				}
				if(isExit&&event==null) {//必须将队列里边的数据都处理空才退出
					break;
				}
				if(event==null)
					continue;
				if(isExit) {
					logger.warn("Waiting queue-data[{}] to be dealed over for exit...",routeGroupQueue.size(routeId));
				}
				try {
					subConsumer.doEvent(event);
				} catch (Exception e) {
					logger.error("",e);
				}finally {
					if(subConsumer.isNeedNotify()) {
						synchronized (event) {
							event.notify();
						}							
					}
				}
			}
			if(subConsumer!=null) {
				subConsumer.end();
			}			
		}

		@Override
		public void setExit(boolean isToExit) {
			isExit=isToExit;
		}
		
	}
}
