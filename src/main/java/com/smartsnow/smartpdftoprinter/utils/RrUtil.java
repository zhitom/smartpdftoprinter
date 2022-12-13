package com.smartsnow.smartpdftoprinter.utils;


import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 解决拉取数据放入队列，并缓存到本地，实现减少和避免重复拉取，避免重复拉取的数据重复处理。
 * @author Shandy<shanzq@aivanlink.com>
 *
 */
@Slf4j
public class RrUtil {
	/**缓存已经完成的信息,但是拉取的时候还是旧数据,需据此判断是否已经完成,超时时间需要比sleepTimeMSecs大
	 * 解决工单处理时间较长又被重复拉取的问题*/
	private KVCache doneCache=null;
	/**缓存未完成的job,避免重复加入*/
	private LocalUnDoneCache localUnDoneCache=new LocalUnDoneCache();
	
	private static class LocalUnDoneCache{
		private KVCache ids=DefaultKVCache.build("RR-LocalUnDoneCache", 0, 0);
		public void put(String key) {
			ids.put(key,key);
		}
		public boolean exists(String key) {
			return ids.exists(key);
		}
		public void done(String key) {
			ids.remove(key);
		}
		public long size() {
			return ids.size();
		}
	}
	public RrUtil(long unDoneExpireMillSecs) {
		doneCache=SimpleKVCache.build("RR-UnDoneCache", unDoneExpireMillSecs, 60);
	}
	public void done(String key) {
		localUnDoneCache.done(key);
		doneCache.put(String.valueOf(key), key);
	}
	public void putUnDone(String key) {
		localUnDoneCache.put(key);
	}
	public boolean isUnDoneEmpty() {
		return localUnDoneCache.size()==0;
	}
	public boolean exists(String key) {
		//已经插入未完成的跳过
		if(localUnDoneCache.exists(key)) {
			log.info("localUnDoneCache.exists key={}",key);
			return true;
		}
		//未插入,再判断是否已经处理完成
		if(doneCache.exists(key)) {
			log.info("doneCache.exists key={}",key);
			return true;
		} 
		return false;
	}
}
