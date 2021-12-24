package com.smartsnow.smartpdftoprinter.producer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartsnow.smartpdftoprinter.AppConfig;
import com.smartsnow.smartpdftoprinter.bean.EventTypeEnum;
import com.smartsnow.smartpdftoprinter.bean.JobTypeEnum;
import com.smartsnow.smartpdftoprinter.collectors.JobCollector;
import com.smartsnow.smartpdftoprinter.collectors.JobCollectorFactory;
import com.smartsnow.smartpdftoprinter.event.in.InJobInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 设备上报的原始消息处理,包括: 设备指令和设备固件OTA
 * */
@Slf4j
@Component
public class JobToBizEventProducer {
	
	@Autowired
	private BizEventConsumers bizEventProducer;
	
	@Autowired
	private AppConfig appConfig;
	
	private long sleepTimeMSecs=3000;
//	private RrUtil rrUtil=new RrUtil(120000);
	
	@PostConstruct
	public void startup() throws IOException {
	}
	
	public void shutdown() {
	}
	public void done(String key) {
//		rrUtil.done(key);
	}
	public int consume(boolean stopFlag){
		return consumeJobEvent(stopFlag);
	}
	
	private int consumeJobEvent(boolean stopFlag) {
		int total = 0;
		List<InJobInfo> batchRecords = new ArrayList<>();
		boolean isEmpty=true;
		JobCollector jobHandler=JobCollectorFactory.getEventHandler(JobTypeEnum.get(appConfig.getJobType()));
		if(jobHandler==null) {
			log.error("No JobHandler,EXIT!");
			System.exit(101);
		}
		jobHandler.prepare();
		while (!stopFlag) {
			try {
//				if (!rrUtil.isUnDoneEmpty()) {//消息未处理完,记录未更新,会获取重复记录
//					break;
//				}
				List<InJobInfo> records = jobHandler.getJobs();
				if (records.isEmpty()) {
					if(batchRecords.isEmpty()) {
						Thread.sleep(sleepTimeMSecs);
					}
					break;
				}
				isEmpty=true;
				for (InJobInfo record : records) {
//					if(rrUtil.exists(record.toString())) {
//						continue;
//					}
					isEmpty=false;
					//未插入,未完成
//					rrUtil.putUnDone(record.toString());
					batchRecords.add(record);
					total++;
				}
				
				if (batchRecords.size() >= appConfig.getInBatchSize()) {
					batch(batchRecords);
					batchRecords.clear();
				}
				if(isEmpty) {
					if(batchRecords.isEmpty()) {
						Thread.sleep(sleepTimeMSecs);
					}
					break;
				}
			} catch (Exception e) {
				log.error("", e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		}
		try {
			if (!batchRecords.isEmpty()) {
				batch(batchRecords);
				batchRecords.clear();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return total;
	}

	private void batch(List<InJobInfo> records){
		for (InJobInfo record : records) {
//			log.info("record.value={}",record);
			bizEventProducer.putEvent(record,EventTypeEnum.EVENT_TYPE_LOGIN_TO_JOB);
			if(appConfig.getJobInterval()>0) {
				try {
					Thread.sleep(appConfig.getJobInterval()*1000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	
}
