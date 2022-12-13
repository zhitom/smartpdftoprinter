package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.ThreadFactory;

/**
 * 守护线程会在用户线程退出后自动退出,守护线程的子线程还是属于守护线程
 * */
public class SimpleThreadFactoryBuilder {
	/**会随其他线程退出自动退出,[守护线程]*/
	public static ThreadFactory build(String name) {
		return buildWithAutoExit(name);
	}
	/**会随其他线程退出自动退出,[守护线程]*/
	public static ThreadFactory buildWithAutoExit(String name) {
		return new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName(name+"-%d");
                return t;
              }
        };
	}
	/**不会随其他线程退出自动退出,线程任务自行处理结束才会退出,[非守护线程]*/
	public static ThreadFactory buildWithNoExit(String name) {
		return new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(false);
                t.setName(name+"-%d");
                return t;
              }
        };
	}
}
