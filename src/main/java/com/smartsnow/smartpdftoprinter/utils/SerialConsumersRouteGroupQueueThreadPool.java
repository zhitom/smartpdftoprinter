package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsnow.smartpdftoprinter.utils.RouteGroupQueueThreadPool.RouteGroupQueueWorker;


/**
 * 支持串行消费处理(多级消费链)的多个的分组队列线程池,
 * 一个分组队列线程池包括多个队列和每个队列绑定的线程,
 * 消息按Key进行分组分发到多个队列,并由队列绑定的线程进行处理.
 * 一个分组队列线程池就是一个消息消费者,多个分组队列线程池即多个消息消费者,串行组成一个多级消费链
 * workers:具体线程的任务实现,pools:分组线程池
 * source -> pools-1(workers-1) -> pools-2(workers-2) -> ...
 * 需要实现多个{@see RouteGroupQueueWorkerWithNextQueue}(不同消费者)和{@see ExceptionHandler}接口.
 * */
public class SerialConsumersRouteGroupQueueThreadPool<K,V> {
	private static Logger logger = LoggerFactory.getLogger(SerialConsumersRouteGroupQueueThreadPool.class);
	private String name;
	///封装消费者和多个消费者之间的衔接
	private RouteGroupQueueWorker<V>[][] serialConsumersWorkers=null;
	///原始的业务消费者
	private RouteGroupQueueWorkerWithNextKeyBase<K,V>[][] workers=null;
	private RouteGroupQueueThreadPool<K,V>[] routeGroupQueueThreadPools=null; 
	private AtomicLong remainSize=new AtomicLong(0);
	/**
	 * 多个消费者,每个消费者n个线程
	 * */
	public SerialConsumersRouteGroupQueueThreadPool(RouteGroupQueueWorkerWithNextKeyBase<K,V>[][] serialConsumersWorkers) {
		this(SerialConsumersRouteGroupQueueThreadPool.class.getSimpleName(), serialConsumersWorkers);
	}
	/**
	 * 多个消费者,每个消费者n个线程
	 * */
	@SuppressWarnings("unchecked")
	public SerialConsumersRouteGroupQueueThreadPool(String name,RouteGroupQueueWorkerWithNextKeyBase<K,V>[][] serialConsumersWorkers) {
		this.name=name;
		this.workers=serialConsumersWorkers;
		this.serialConsumersWorkers=new RouteGroupQueueWorkerWithNextQueue[workers.length][];
		this.routeGroupQueueThreadPools=new RouteGroupQueueThreadPool[workers.length];
	}
	public long remainSize() {
		return remainSize.get();
	}
	public String getName() {
		return name;
	}
	public RouteGroupQueueWorkerWithNextKeyBase<K,V>[][] getWorkers(){
		return workers;
	}
	@SuppressWarnings("unchecked")
	public void run() {
		// Preconditions.checkArgument(workers!=null&&workers.length>0,"RouteGroupQueueWorker must be configured");
		RouteGroupQueue<K, V> nextQueue=null;
		RouteGroupQueueWorkerWithNextQueue<K,V> workerWithTargetQueue;
		
		//实例化对象
		for(int i=0;i<workers.length;i++) {
			routeGroupQueueThreadPools[i]=new RouteGroupQueueThreadPool<>();
			this.serialConsumersWorkers[i]=new RouteGroupQueueWorkerWithNextQueue[workers[i].length];
			for(int j=0;j<workers[i].length;j++) {
				serialConsumersWorkers[i][j]=new RouteGroupQueueWorkerWithNextQueue<>();
			}
		}
		//初始化消费者
		for(int i=0;i<workers.length;i++) {
			for(int j=0;j<workers[i].length;j++) {
				workerWithTargetQueue=(RouteGroupQueueWorkerWithNextQueue<K, V>) serialConsumersWorkers[i][j];
				workerWithTargetQueue.setWorker(workers[i][j]);
			}
		}
		//启动线程池
		for(int i=0;i<routeGroupQueueThreadPools.length;i++) {
			routeGroupQueueThreadPools[i].start(serialConsumersWorkers[i], 
					name+"-"+i+"-"+workers[i][0].getClass().getSimpleName());
			SchedulableQueueCheckThread.newCheckQueue(routeGroupQueueThreadPools[i].getQueue(), this.getClass(),
					name+"-"+i+"-"+workers[i][0].getClass().getSimpleName());
		}
		//连接队列和消费者
		for(int i=0;i<workers.length;i++) {
			if(i<workers.length-1) {
				nextQueue = routeGroupQueueThreadPools[i+1].getQueue();
			}else {
				nextQueue=null;
			}
			for(int j=0;j<workers[i].length;j++) {
				workerWithTargetQueue=(RouteGroupQueueWorkerWithNextQueue<K, V>) serialConsumersWorkers[i][j];
				workerWithTargetQueue.setNextQueue(nextQueue);
				if(nextQueue==null) {
					workerWithTargetQueue.setRemainSize(remainSize);
				}
			}
		}
		
	}
	public boolean publish(K key,V event) {
		if(routeGroupQueueThreadPools==null||routeGroupQueueThreadPools[0]==null) {
			logger.error("No Workers,Please Check It!");
			return false;
		}
		boolean ret = routeGroupQueueThreadPools[0].offerToQueue(key, event);
		if(ret) {
			remainSize.addAndGet(1);
		}
		return ret;
	}
	public boolean publish(K key,V event,long timeout, TimeUnit unit) {
		if(routeGroupQueueThreadPools==null||routeGroupQueueThreadPools[0]==null) {
			logger.error("No Workers,Please Check It!");
			return false;
		}
		boolean ret = routeGroupQueueThreadPools[0].offerToQueue(key, event,timeout,unit);
		if(ret) {
			remainSize.addAndGet(1);
		}
		return ret;
	}
	public void shutdown() {
		if(routeGroupQueueThreadPools!=null) {
			//等待消息都处理完毕
			while(remainSize.get()>0L) {
				logger.warn("Waiting messages to be done...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("",e);
					Thread.currentThread().interrupt();
				}
			}
			for(int i=0;i<routeGroupQueueThreadPools.length;i++) {
				if(routeGroupQueueThreadPools[i]!=null) {
					routeGroupQueueThreadPools[i].stop();
				}
			}
			routeGroupQueueThreadPools=null;
		}
	}
	public static interface RouteGroupQueueWorkerWithNextKeyBase<Q,E> extends RouteGroupQueueWorker<E> {
		
	}
	//支持强制随机存放元素
	public static interface RouteGroupQueueWorkerWithRandomNextKey<Q,E> extends RouteGroupQueueWorkerWithNextKeyBase<Q,E> {
		
	}
	//支持指定key存放元素
	public static interface RouteGroupQueueWorkerWithNextKey<Q,E> extends RouteGroupQueueWorkerWithNextKeyBase<Q,E> {
		//需要发送给下一个消费者的key
		Q nextKey();
	}
	///实际的消费者,封装了业务消费者
	public static class RouteGroupQueueWorkerWithNextQueue<Q,E> implements RouteGroupQueueWorker<E> {
		protected RouteGroupQueue<Q, E> nextQueue=null;
		protected RouteGroupQueueWorkerWithNextKeyBase<Q,E> worker=null;
		protected AtomicLong remainSize=null;
		public void setNextQueue(RouteGroupQueue<Q, E> nextQueue) {
			this.nextQueue=nextQueue;
		}
		public void setWorker(RouteGroupQueueWorkerWithNextKeyBase<Q,E> worker) {
			this.worker=worker;
		}
		public void setRemainSize(AtomicLong remainSize) {
			this.remainSize=remainSize;
		}
		@Override
		public void doEvent(E e) throws Exception {
			if(worker==null) {
				if(remainSize!=null) {//最后一个消费者
					remainSize.addAndGet(-1);
				}
				return;
			}
			try {
				worker.doEvent(e);
			} finally {
				if(nextQueue!=null) {
					if(worker instanceof RouteGroupQueueWorkerWithNextKey) {
						RouteGroupQueueWorkerWithNextKey<Q,E> trueWorker=(RouteGroupQueueWorkerWithNextKey<Q, E>) worker;
						nextQueue.offer(trueWorker.nextKey(), e);
					}else if(worker instanceof RouteGroupQueueWorkerWithRandomNextKey) {
						nextQueue.offerRandom(e);
					}else{
						nextQueue.offerRandom(e);
					}
				}
				if(remainSize!=null) {//最后一个消费者
					remainSize.addAndGet(-1);
				}
			}
		}
		@Override
		public void setRouteId(int routeId) {
			if(worker==null) {
				return;
			}
			worker.setRouteId(routeId);
		}
		@Override
		public void end() {
			if(worker==null) {
				return;
			}
			worker.end();
		}
	}
}
