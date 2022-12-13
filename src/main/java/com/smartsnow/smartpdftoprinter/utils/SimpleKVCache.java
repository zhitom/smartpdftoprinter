package com.smartsnow.smartpdftoprinter.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
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
 * 一个简单的本地的KV缓冲实现,使用软引用,注意:如果内存不足时,缓存会被清除.
 */
public class SimpleKVCache implements KVCache{
	private static Logger logger = LoggerFactory.getLogger(SimpleKVCache.class);

	private static Map<String, SimpleKVCache> kvCacheMap = new ConcurrentHashMap<>();
	
	private Map<String, ValueSoftReference> cacheMap = new ConcurrentHashMap<>();
	private ReferenceQueue<Value> referenceQueue = new ReferenceQueue<>();// 垃圾Reference的队列
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
			return System.currentTimeMillis() - insertTimeMS >= milliSecs;
		}

		public Object get() {
			return object;
		}
	}

	// 继承SoftReference，使得每一个实例都具有可识别的标识。
	private class ValueSoftReference extends SoftReference<Value> {
		private String key = "";

		public ValueSoftReference(Value em, ReferenceQueue<Value> referenceQueue, final String key) {
			super(em, referenceQueue);
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
	/**默认 10分钟超时,每1分钟扫描一次*/
	public static KVCache build(String kvCacheId) {
		return build(kvCacheId,EXPIRE_MILLISECS,SCHEDULED_SECONDS);
	}
	public static KVCache build(String kvCacheId,long expireMilliSecs,long scheduledTimeSecs) {
		SimpleKVCache kvCache = kvCacheMap.get(kvCacheId);
		if (kvCache != null) {
			return kvCache;
		}
		kvCache = new SimpleKVCache();
		kvCacheMap.put(kvCacheId, kvCache);
		kvCache.init(kvCacheId,expireMilliSecs,scheduledTimeSecs);
		return kvCache;
	}

	private SimpleKVCache() {
	}

	private void init(String kvCacheId, long expireMilliSecs,long scheduledTimeSecs) {
		this.kvCacheId=kvCacheId;
		runnable.setExpireMilliSecs(expireMilliSecs);
		runnable.setCacheData(cacheMap,referenceQueue,sz);
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

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		ValueSoftReference obj = cacheMap.get(key);
		if (obj == null)
			return null;
		Value value = obj.get();
		if (value == null)
			return null;
		return (T) value.get();
	}
	@Override
	public boolean exists(String key) {
		ValueSoftReference obj = cacheMap.get(key);
		if (obj == null)
			return false;
		Value value = obj.get();
		return (value != null);
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
		cacheMap.put(key, new ValueSoftReference(new Value(obj), referenceQueue, key));		
	}
	@Override
	public void refreshInsertTime(String key) {
		if(logger.isDebugEnabled()) {
			logger.debug("[{}]refreshInsertTime: key={},sz={}",kvCacheId,key,sz.longValue());
		}
		ValueSoftReference obj = cacheMap.get(key);
		if (obj == null)
			return ;
		Value value = obj.get();
		if(value!=null) {
			value.refreshInsertTime();
		}
	}
	@Override
	public void remove(String key) {
		ValueSoftReference value = cacheMap.remove(key);
		sz.decrementAndGet();
		if(logger.isDebugEnabled()) {
			logger.debug("[{}]REMOVE: key={},value={},sz={}",kvCacheId,key,value.get().get(),sz.longValue());
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
		private volatile long expireMilliSecs = SimpleKVCache.EXPIRE_MILLISECS;
		private Map<String, ValueSoftReference> cacheMap = null;
		private ReferenceQueue<Value> referenceQueue = null;
		private AtomicLong sz=null;
		@Override
		public void run() {
			List<String> removeKeys=new ArrayList<>();
			Value value;
			for (Entry<String, ValueSoftReference> entry : cacheMap.entrySet()) {
				if (entry.getValue() == null) {
					continue;
				}
				value = entry.getValue().get();
				if (value != null && value.isExpire(expireMilliSecs)) {
					entry.getValue().clear();
					removeKeys.add(entry.getKey());
				}
			}
			removeKeys.forEach(v -> {
				cacheMap.remove(v);
				sz.decrementAndGet();
				});
			removeKeys.clear();
			cleanExpireKeys();
		}
		private void cleanExpireKeys() {
			ValueSoftReference ref = null;
			while ((ref = (ValueSoftReference) referenceQueue.poll()) != null) {
				cacheMap.remove(ref.getKey());
				sz.decrementAndGet();
			}
		}
		public void setCacheData(Map<String, ValueSoftReference> cacheMap, ReferenceQueue<Value> referenceQueue,AtomicLong sz) {
			this.cacheMap = cacheMap;
			this.referenceQueue=referenceQueue;
			this.sz=sz;
		}

		public void setExpireMilliSecs(long expireMilliSecs) {
			this.expireMilliSecs = expireMilliSecs;
		}

	}
}
