package com.smartsnow.smartpdftoprinter.consumer;

import java.io.File;

import com.smartsnow.smartpdftoprinter.AppConfig;
import com.smartsnow.smartpdftoprinter.bean.EventTypeEnum;
import com.smartsnow.smartpdftoprinter.bean.JobTypeEnum;
import com.smartsnow.smartpdftoprinter.collectors.JobCollector;
import com.smartsnow.smartpdftoprinter.collectors.JobCollectorFactory;
import com.smartsnow.smartpdftoprinter.event.in.InEventWrapper;
import com.smartsnow.smartpdftoprinter.event.in.InJobInfo;
import com.smartsnow.smartpdftoprinter.producer.JobToBizEventProducer;
import com.smartsnow.smartpdftoprinter.utils.SpringContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventConsumerJob extends EventConsumerBase {
	
	private JobToBizEventProducer jobToBizEventProducer=SpringContextUtils.getBean(JobToBizEventProducer.class);
	private AppConfig appConfig=SpringContextUtils.getBean(AppConfig.class);
	private JobCollector jobHandler=null;
	
	@Override
	public void consumer(InEventWrapper event) throws Exception {
//		if(event.getEventStatus()!=EventStatusEnum.EVENT_STATUS_SUCCESS) {
//			log.info("SKIPPED EVENT FOR NOT-SUCCESS!!!");
//			return;
//		}
		InJobInfo inJobInfo = null;
		try {
			// 解析和读取输入事件内容
			event.parse();
			log.info("do event...{}", event);
			if (event.getInEvent().getContent() == null) {
				log.error("SKIPPED,error event={}", event);
				return;
			}
			if(event.getEventType()!=EventTypeEnum.EVENT_TYPE_LOGIN_TO_JOB) {
				return;
			}
			inJobInfo = (InJobInfo) event.getInEvent().getContent();
			//处理job
			handlerJob(inJobInfo);
		} catch (Exception e) {
			log.error("MESSAGE WILL BE LOSS:event={}", event, e);
		}finally {
			if(inJobInfo!=null) {
				jobToBizEventProducer.done(String.valueOf(inJobInfo.getId()));
			}
		}
	}

	/**
	 * @param inJobInfo
	 */
	private void handlerJob(InJobInfo inJobInfo) {
		File f=(File) inJobInfo.getJob();
		if(jobHandler==null) {
			jobHandler=JobCollectorFactory.getEventHandler(JobTypeEnum.get(appConfig.getJobType()));
		}
		jobHandler.apply(f);
	}
}
