package com.smartsnow.smartpdftoprinter.utils;

public interface KVCacheScanConsumer<T, U> extends KVCacheExpireConsumer<T, U>{
	/**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void scan(T t, U u);
}
