package com.smartsnow.smartpdftoprinter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 一个缺省的本地的KV缓冲实现,使用强引用,注意:如果内存不足时,缓存不会释放.
 * 如果超时时间<=0,则缓存会永久生效
 */
public class DefaultKVCache implements KVCache{
	private static Logger logger = LoggerFactory.getLogger(DefaultKVCache.class);

	private static Map<String, DefaultKVCache> kvCacheMap = new ConcurrentHashMap<>();
	
	private Map<String, Value> cacheMap = new ConcurrentHashMap<>();
	private AtomicLong sz=new AtomicLong(0L);
	private ExpireScan runnable = new ExpireScan();
	private ScheduledExecutorService expireExecutor = null;
	private String kvCacheId="default";

	private static class Value {
		private Object object = null;
		private long insertTimeMS = 0;

		public Value(Object obj) {
			object = obj;
			insertTimeMS = System.currentTimeMillis();
		}
		public void refreshInsertTime() {
			insertTimeMS = System.currentTimeMillis();
		}
		public boolean isExpire(long milliSecs) {
			return milliSecs>0&&System.currentTimeMillis() - insertTimeMS >= milliSecs;
		}

		public Object get() {
			return object;
		}
	}
	 
	/**默认 10分钟超时,每1分钟扫描一次*/
	public static KVCache build(String kvCacheId) {
		return build(kvCacheId,EXPIRE_MILLISECS,SCHEDULED_SECONDS,null);
	}
	public static KVCache build(String kvCacheId,long expireMilliSecs,long scheduledTimeSecs) {
		return build(kvCacheId,expireMilliSecs,scheduledTimeSecs,null);
	}

	public static KVCache build(String kvCacheId,long expireMilliSecs,long scheduledTimeSecs,KVCacheScanConsumer<String,Object> consumer) {
		DefaultKVCache kvCache = kvCacheMap.get(kvCacheId);
		if (kvCache != null) {
			return kvCache;
		}
		kvCache = new DefaultKVCache();
		kvCacheMap.put(kvCacheId, kvCache);
		kvCache.init(kvCacheId,expireMilliSecs,scheduledTimeSecs,consumer);
		return kvCache;
	}

	private DefaultKVCache() {
	}

	private void init(String kvCacheId, long expireMilliSecs,long scheduledTimeSecs,KVCacheScanConsumer<String,Object> consumer) {
		this.kvCacheId=kvCacheId;
		if(expireMilliSecs>0) {
			runnable.setExpireMilliSecs(expireMilliSecs);
			runnable.setCacheData(cacheMap,sz);
			runnable.setConsumer(consumer);
			expireExecutor = Executors.newScheduledThreadPool(1,
					SimpleThreadFactoryBuilder.buildWithAutoExit("KVCache-expire-scan-" + kvCacheId));
			expireExecutor.scheduleWithFixedDelay(runnable, 0, scheduledTimeSecs, TimeUnit.SECONDS);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					expireExecutor.shutdown();
				}
			});
		}		
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		Value obj = cacheMap.get(key);
		if (obj == null)
			return null;
		return (T) obj.get();
	}
	@Override
	public boolean exists(String key) {
		return cacheMap.get(key)!=null;
	}
	/**大概值,因为数据在不停的变化*/
	@Override
	public long size() {
		return sz.longValue();//cacheMap.size();
	}
	@Override
	public <T> void put(String key, T obj) {
		if(!cacheMap.containsKey(key)) {
			sz.incrementAndGet();
		}
		if(logger.isDebugEnabled()) {
			logger.debug("[{}]PUT: key={},value={},sz={}",kvCacheId,key,obj,sz.longValue());
		}
		cacheMap.put(key, new Value(obj));		
	}
	@Override
	public void refreshInsertTime(String key) {
		if(logger.isDebugEnabled()) {
			logger.debug("[{}]refreshInsertTime: key={},sz={}",kvCacheId,key,sz.longValue());
		}
		Value obj = cacheMap.get(key);
		if(obj!=null) {
			obj.refreshInsertTime();
		}
	}
	@Override
	public void remove(String key) {
		Value value = cacheMap.remove(key);
		sz.decrementAndGet();
		if(logger.isDebugEnabled()) {
			logger.debug("[{}]REMOVE: key={},value={},sz={}",kvCacheId,key,value.get(),sz.longValue());
		}
	}
	@Override
	public String toString() {
		return cacheMap.toString();
	}
	// 清除Cache内的全部内容
//	public void clear() {
//		cleanExpireKeys();
//		cacheMap.clear();
//		System.gc();
//		System.runFinalization();
//	}

	private static class ExpireScan implements Runnable {
		private volatile long expireMilliSecs = DefaultKVCache.EXPIRE_MILLISECS;
		private Map<String, Value> cacheMap = null;
		private AtomicLong sz=null;
		private KVCacheScanConsumer<String,Object> consumer=null;
		@Override
		public void run() {
			List<String> removeKeys=new ArrayList<>();
			Value value;
			String key;
			for (Entry<String, Value> entry : cacheMap.entrySet()) {
				key=entry.getKey();
				value = entry.getValue();
				if(consumer!=null) {
					consumer.scan(key, value);
				}
				if (value != null && value.isExpire(expireMilliSecs)) {
					removeKeys.add(key);
					if(consumer!=null) {
						consumer.expire(key, value);
					}
				}
			}
			removeKeys.forEach(v -> {				
				cacheMap.remove(v);
				sz.decrementAndGet();
				});
			removeKeys.clear();
		}
		public void setCacheData(Map<String, Value> cacheMap, AtomicLong sz) {
			this.cacheMap = cacheMap;
			this.sz=sz;
		}

		public void setExpireMilliSecs(long expireMilliSecs) {
			this.expireMilliSecs = expireMilliSecs;
		}
		public void setConsumer(KVCacheScanConsumer<String,Object> consumer) {
			this.consumer=consumer;
		}
	}
}
