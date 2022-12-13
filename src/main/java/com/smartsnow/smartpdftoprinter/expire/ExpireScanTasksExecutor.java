package com.smartsnow.smartpdftoprinter.expire;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartsnow.smartpdftoprinter.utils.ExecutorsUtil;
import com.smartsnow.smartpdftoprinter.utils.SafeRunnable;
import com.smartsnow.smartpdftoprinter.utils.ExecutorsUtil.ThreadWorkerPool;


/**将来可以有多个超时扫描任务*/
@Component
public class ExpireScanTasksExecutor {
	private static final String EXPIRE_SCAN_TASKS_THREAD_NAME="expire-scan-tasks";
	private ThreadWorkerPool threadWorkerPool=null;
	
	@Autowired
	private CleanDoneJobsScanRunnable cleanDoneJobsScanRunnable;
	
	private List<SafeRunnable> safeRunnables=new ArrayList<>();
	
	@PostConstruct
	public void startup() {
		safeRunnables.add(cleanDoneJobsScanRunnable);
		threadWorkerPool=ExecutorsUtil.getPool(EXPIRE_SCAN_TASKS_THREAD_NAME, safeRunnables.size());
		threadWorkerPool.start();
		safeRunnables.forEach(v->threadWorkerPool.submit(v));
	}
	public void shutdown() {
		if(threadWorkerPool!=null) {
			threadWorkerPool.stop();
			threadWorkerPool=null;
		}
	}
}
