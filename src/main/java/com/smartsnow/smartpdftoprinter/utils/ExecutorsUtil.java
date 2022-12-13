package com.smartsnow.smartpdftoprinter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * start(n);
 * submit(runtask);
 * stop();
 * 
 * stopThreads();
 * resize(m);//important
 * submit(runtask);
 * */
public class ExecutorsUtil {
	private static Logger logger = LoggerFactory.getLogger(ExecutorsUtil.class);
	private static Map<String, ThreadWorkerPool> allPools=new ConcurrentHashMap<>();
	
	public static ThreadWorkerPool getPool(String name) {
		return getPool(name,0);
	}
	public static ThreadWorkerPool getPool(String name,int threadNum) {
		if(name==null||name.isEmpty()) {
			return null;
		}
		ThreadWorkerPool foundPool=allPools.get(name);
		if(foundPool==null) {
			foundPool=new ThreadWorkerPool(name);
			allPools.put(name, foundPool);
			foundPool=allPools.get(name);//因为可能并发,需要重新获取一遍.
		}
		if(threadNum>0) {
			foundPool.stopThreads();
			foundPool.resize(threadNum);
		}
		return foundPool;
	}
	public static class ThreadWorkerPool{
		protected ThreadPoolExecutor oneService=null;
		protected List<Exitable> taskList=new ArrayList<>();
	    protected List<Future<?>> futureList=new ArrayList<>();
		protected String name;
	    protected int threadWaitingSleepMillSecs=10;
	    protected int maxThreadWaitingSleepMillSecs=5000;
	    protected int threadNum=1;
	    
	    protected boolean isNotSubmitTask=false;
		
		public ThreadPoolExecutor getThreadPoolExecutor() {
			return oneService;
		}
		public ThreadWorkerPool(String name) {
			this.name=name;
		}
		public void setThreadSleepMillSecs(int threadSleepMillSecs) {
	        this.threadWaitingSleepMillSecs = threadSleepMillSecs;
	    }
		public void setMaxThreadSleepMillSecs(int maxThreadSleepMillSecs) {
	        this.maxThreadWaitingSleepMillSecs = maxThreadSleepMillSecs;
	    }
		public void start() {
			// Preconditions.checkArgument(this.threadNum>0);
			start(this.threadNum);
		}
		public void start(int threadNum) {
			this.threadNum=threadNum;
			if(oneService==null) {
				oneService=(ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum,
						SimpleThreadFactoryBuilder.buildWithAutoExit(name));
				oneService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
			}
			clear();
		}
		public void resize(int threadNum) {
			if(oneService==null) {
				start(threadNum);
			}else {
				this.threadNum=threadNum;
				oneService.setCorePoolSize(threadNum);
			}
		}
		public boolean isDone() {
	        return oneService.getActiveCount()==0;
	    }
	    public boolean blockWaitDone() {
	        while(oneService.getActiveCount()>0) {
	            logger.info("TaskCount={},CompletedTaskCount={},ActiveCount Thread={},And Sleep({}ms)...",
	            		oneService.getTaskCount(),oneService.getCompletedTaskCount(),oneService.getActiveCount(),
	            		threadWaitingSleepMillSecs);
	            try {
	                Thread.sleep(threadWaitingSleepMillSecs);
	            } catch (InterruptedException e) {
	                logger.error("ERROR:",e);
	                //Thread.currentThread().interrupt();
	                return false;
	            }
	        }
	        clear();
	        return true;
	    }
	    public void stopThreads() {
			taskList.stream().forEach(t -> t.setExit(true));
			while(oneService!=null&&oneService.getActiveCount()>0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			clear();
		}
		///停止所有线程,并且清空线程对象列表
		public void stop() {
			if(oneService==null) {
				return;
			}
			stopThreads();
			oneService.shutdown();
			// 关闭线程池
	        logger.info("Start to waiting tasks shutdown...");
	        int waitms=0;
	        try {
	            while (!oneService.isTerminated()) {
	                logger.info("TaskCount={},CompletedTaskCount={},ActiveCount Thread={},And Sleep({}ms)...",oneService.getTaskCount(),oneService.getCompletedTaskCount(),oneService.getActiveCount(),threadWaitingSleepMillSecs);
	                Thread.sleep(threadWaitingSleepMillSecs);
	                waitms+=threadWaitingSleepMillSecs;
	                if(waitms>=maxThreadWaitingSleepMillSecs) {
	                	logger.warn("[WARN]exceed maxThreadWaitingSleepMillSecs({})",maxThreadWaitingSleepMillSecs);
	                	break;
	                }
	            }
	        } catch (InterruptedException e) {
	            logger.error("ERROR:",e);
	        }
//			try {
//				oneService.awaitTermination(maxThreadWaitingSleepMillSecs, TimeUnit.MILLISECONDS);
//			} catch (InterruptedException ex) {
//				logger.info("Interrupted while awaiting termination", ex);
//			}
			oneService.shutdownNow();
		}

		public void submit(Exitable run) {
			if(isNotSubmitTask) {
	            taskList.add(run);
	            return;
	        }
			taskList.add(run);
			if(run instanceof Runnable) {
				Runnable task=(Runnable) run;
				oneService.submit(task);
			}else if(run instanceof Callable) {
				Callable<?> task=(Callable<?>) run;
				futureList.add(oneService.submit(task));
			}else {
				throw new RuntimeException("type error:"+run) ;
			}
		}
		
		public void submit(List<Exitable> runs) {
			if(isNotSubmitTask) {
	            taskList.addAll(runs);
	            return;
	        }
			runs.stream().forEach(r -> submit(r));
		}
	    private void clear() {
	        futureList.clear();
	        taskList.clear();
	    }
	    
		public boolean isNotSubmitTask() {
	        return isNotSubmitTask;
	    }
		
	    public void setNotSubmitTask(boolean isNotSubmitTask) {
	        this.isNotSubmitTask = isNotSubmitTask;
	    }
	    
	    public List<Object> getResultList() throws Exception {
	        List<Object> resultList=new ArrayList<>();
	        for (Future<?> f : futureList) {
	            resultList.add(f.get());
	        }
	        return resultList;
	    }
	    
	    public List<Exitable> getTaskList() {
	        return taskList;
	    }

	    public List<Future<?>> getFutureList() {
	        return futureList;
	    }
	    
	    public int getThreadNum() {
	        return threadNum;
	    }
	}
	
}
