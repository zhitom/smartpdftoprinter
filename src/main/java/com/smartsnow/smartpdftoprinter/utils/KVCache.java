package com.smartsnow.smartpdftoprinter.utils;

/**
 * 缓存接口,用于扩展,实现了local和redis,缓存默认10分钟.
 * */
public interface KVCache {
	public static final long EXPIRE_MILLISECS = 600000L;
	public static final long SCHEDULED_SECONDS = 60L;

	/**允许重复put,时间会重新计数*/
	public <T> void put(String key, T obj);
	public <T> T get(String key);
	public boolean exists(String key);
	///重新刷新插入时间,避免过期
	public void refreshInsertTime(String key);
	/**
	 * 直接删除
	 * */
	public void remove(String key);
	/**大概值,因为数据在不停的变化*/
	public long size();
	
}
