package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.LinkedTransferQueue;

import com.aivanlink.common.base.thread.RouteGroupQueue;
import com.aivanlink.common.base.thread.SchedulableQueueCheckThread;

public class CheckQueueSize {
	private CheckQueueSize() {}
	public static <T,Q> void newCheckQueue(LinkedTransferQueue<Q> queue,Class<T> clazz) {
		newCheckQueue(queue, clazz,clazz.getSimpleName());
	}
	public static <T,Q> void newCheckQueue(LinkedTransferQueue<Q> queue,Class<T> clazz,String threadName) {
		SchedulableQueueCheckThread.newCheckQueue(queue, clazz,threadName);
	}
	public static <E,K,N> void newCheckQueue(RouteGroupQueue<E, K> queue,Class<N> clazz) {
		newCheckQueue(queue,clazz,clazz.getSimpleName());
	}
	public static <E,K,N> void newCheckQueue(RouteGroupQueue<E, K> queue,Class<N> clazz,String threadName) {
		SchedulableQueueCheckThread.newCheckQueue(queue,clazz,threadName);
	}
//	public static <T,N> void newCheckQueue(RingBuffer<T> ringBuffer, Class<N> clazz) {
//		newCheckQueue(ringBuffer, clazz,clazz.getSimpleName());
//	}
//	public static <T,N> void newCheckQueue(RingBuffer<T> ringBuffer, Class<N> clazz, String threadName) {
//		SchedulableQueueCheckThreadForDisruptor.newCheckQueue(ringBuffer, clazz,threadName);
//	}
//	public static <T extends RingEventBase<R>,R,N> void newCheckQueue(DisruptorWrapper<T, R> disruptor, Class<N> clazz) {
//		newCheckQueue(disruptor, clazz,clazz.getSimpleName());
//	}
//	public static <T extends RingEventBase<R>,R,N> void newCheckQueue(DisruptorWrapper<T, R> disruptor,Class<N> clazz, String threadName) {
//		SchedulableQueueCheckThreadForDisruptor.newCheckQueue(disruptor, clazz,threadName);
//	}
}
