package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于私有内存队列实现多个消费者根据分组进行消费的功能,保证同一个key被同一个消费者消费
 * */
public class RouteGroupQueue<K,V> {
	private static Logger logger = LoggerFactory.getLogger(RouteGroupQueue.class);
	private GroupQueue<V>[] queues=null;
	private int consumerSize=1;
	
	private static class GroupQueue<V>{
		private LinkedTransferQueue<V> queue=new LinkedTransferQueue<>();
		@SuppressWarnings("unused")
		public boolean isEmpty() {
			return queue.isEmpty();
		}
		public int size() {
			return queue.size();
		}
		public V poll(long timeout, TimeUnit unit) throws InterruptedException {
			return queue.poll(timeout,unit);
		}
		public V poll() {
			return queue.poll();
		}
		public V take() throws InterruptedException {
			return queue.take();
		}
		public boolean offer(V t) {
			return queue.offer(t);
		}
		public boolean offer(V t,long timeout, TimeUnit unit) {
			return queue.offer(t,timeout,unit);
		}
	}
	@SuppressWarnings("unchecked")
	public RouteGroupQueue(int consumerSize){
		this.consumerSize=consumerSize;
		queues=new GroupQueue[consumerSize];
		for(int i=0;i<consumerSize;i++) {
			queues[i]=new GroupQueue<V>();
		}
	}
	public V take(int pos) throws InterruptedException {
		// Preconditions.checkArgument(pos>=0&&pos<consumerSize);
		return queues[pos].take();
	}
	public V poll(int pos) {
		// Preconditions.checkArgument(pos>=0&&pos<consumerSize);
		return queues[pos].poll();
	}
	public V poll(int pos,long timeout, TimeUnit unit) throws InterruptedException {
		// Preconditions.checkArgument(pos>=0&&pos<consumerSize);
		return queues[pos].poll(timeout,unit);
	}
	public boolean offer(K routeId,V t) {
//		logger.warn("QUEUE:routeId={},absPos={},pos={}",routeId,Math.abs(routeId.hashCode()%consumerSize),
//				(routeId.hashCode() & 0x7FFFFFFF)%consumerSize);
		int pos=0;
		pos=(HashUtil.selfHash(routeId==null?"":routeId))%consumerSize;	
		if(pos<0||pos>=consumerSize) {
			logger.error("routeId[{}].hashCode()={},consumerSize={},pos={},t={}",
					routeId,routeId.hashCode(),consumerSize,pos,t);
			throw new RuntimeException("routeId="+routeId+",pos="+pos+",t="+t);
		}
		return queues[pos].offer(t);
	}
	public boolean offerRandom(V t) {
		int pos=0;
		pos=SysUtil.getRandomValue(0,Integer.MAX_VALUE)%consumerSize;	
		if(pos<0||pos>=consumerSize) {
			logger.error("consumerSize={},pos={},t={}",consumerSize,pos,t);
			throw new RuntimeException("pos="+pos+",t="+t);
		}
		return queues[pos].offer(t);
	}
	public boolean offer(K routeId,V t,long timeout, TimeUnit unit) {
		int pos=0;
		pos=(HashUtil.selfHash(routeId==null?"":routeId))%consumerSize;
		if(pos<0||pos>=consumerSize) {
			logger.error("routeId[{}].hashCode()={},consumerSize={},pos={},t={}",
					routeId,routeId.hashCode(),consumerSize,pos,t);
			throw new RuntimeException("routeId="+routeId+",pos="+pos+",t="+t);
		}
		return queues[pos].offer(t,timeout,unit);
	}
	public boolean offerRandom(V t,long timeout, TimeUnit unit) {
		int pos=0;
		pos=SysUtil.getRandomValue(0,Integer.MAX_VALUE)%consumerSize;	
		if(pos<0||pos>=consumerSize) {
			logger.error("consumerSize={},pos={},t={}",consumerSize,pos,t);
			throw new RuntimeException("pos="+pos+",t="+t);
		}
		return queues[pos].offer(t,timeout,unit);
	}
	public int size(int pos) {
		// Preconditions.checkArgument(pos>=0&&pos<consumerSize);
		return queues[pos].size();
	}
	public int length() {
		return queues.length;
	}
}
