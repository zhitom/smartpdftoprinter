package com.smartsnow.smartpdftoprinter.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 大对象缓存,目标是提高性能
 * @author Shandy<shanzq@aivanlink.com>
 *
 */
public class ThreadLocalWrapper<T> {
	private static final Logger log = LoggerFactory.getLogger(ThreadLocalWrapper.class);
	
	private ThreadLocal<T> threadLocal=new ThreadLocal<>();
	
	public final T get(Supplier<T> supplier) {
    	return ThreadLocalWrapper.get(threadLocal,supplier);
    }
	public final void set(T t) {
    	ThreadLocalWrapper.set(threadLocal,t);
    }
	public final T get(Class<T> clazz) {
    	return ThreadLocalWrapper.get(threadLocal,clazz);
    }
	public final <R> T get(Class<T> clazz,Class<R> argClazz,Object arg) {
		@SuppressWarnings("unchecked")
		Class<R>[] rs=new Class[1];
		Object[] os=new Object[1];
		
		rs[0]=argClazz;
		os[0]=arg;
    	return ThreadLocalWrapper.get(threadLocal,clazz,rs,os);
    }
	public final <R1,R2> T get(Class<T> clazz,Class<R1> argClazz1,Object arg1,Class<R2> argClazz2,Object arg2) {
		Class<?>[] rs=new Class[2];
		Object[] os=new Object[2];
		
		rs[0]=argClazz1;rs[1]=argClazz2;
		os[0]=arg1;os[1]=arg2;
    	return ThreadLocalWrapper.get(threadLocal,clazz,rs,os);
    }
	public final <R1,R2,R3> T get(Class<T> clazz,Class<R1> argClazz1,Object arg1,Class<R2> argClazz2,Object arg2,Class<R3> argClazz3,Object arg3) {
		Class<?>[] rs=new Class[3];
		Object[] os=new Object[3];
		
		rs[0]=argClazz1;rs[1]=argClazz2;rs[2]=argClazz3;
		os[0]=arg1;os[1]=arg2;os[2]=arg3;
    	return ThreadLocalWrapper.get(threadLocal,clazz,rs,os);
    }
	public final <R> T get(Class<T> clazz,Class<R>[] argClazz,Object[] arg) {
    	return ThreadLocalWrapper.get(threadLocal,clazz,argClazz,arg);
    }
	private static final <AA> AA get(ThreadLocal<AA> local,Supplier<AA> supplier) {
    	AA t=local.get();
        if(t==null) {
        	local.remove();
        	local.set(supplier.get());
        }
        return t;
    }
    private static final <AA> AA get(ThreadLocal<AA> local,Class<AA> clazz) {
    	AA t=local.get();
        if(t==null) {
        	try {
				t=clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				log.error("",e);
			}
        	local.remove();
        	local.set(t);
        }
        return t;
    }
	private static final <AA> AA get(ThreadLocal<AA> local,Class<AA> clazz,Class<?>[] argClazz,Object[] arg) {
    	AA t=local.get();
        if(t==null) {
        	try {
				t=clazz.getDeclaredConstructor(argClazz).newInstance(arg);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				log.error("",e);
			}
        	local.remove();
        	local.set(t);
        }
        return t;
    }
	private static final <AA> void set(ThreadLocal<AA> local,AA a) {
		local.remove();
    	local.set(a);
    }
    
}
