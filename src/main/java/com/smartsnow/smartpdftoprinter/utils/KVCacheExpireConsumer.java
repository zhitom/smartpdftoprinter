package com.smartsnow.smartpdftoprinter.utils;
/**
 * 处理超时消息的消费接口
 * */

public interface KVCacheExpireConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void expire(T t, U u);

}
