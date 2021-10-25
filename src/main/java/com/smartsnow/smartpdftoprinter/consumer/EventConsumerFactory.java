package com.smartsnow.smartpdftoprinter.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartsnow.smartpdftoprinter.AppConfig;
import com.smartsnow.smartpdftoprinter.bean.EventConsumerEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventConsumerFactory {

	@Autowired
	private AppConfig appConfig;
	
	private EventConsumerFactory() {}
	public EventConsumerBase[] newConsumer(EventConsumerEnum consumer) {
		
		int threadNum=appConfig.getConsumerThreadNum(consumer);
		if (threadNum <= 0)
			return null;
		
		switch (consumer) {
		case EVENT_CONSUMER_JOB:
			return initArray(new EventConsumerJob[threadNum],EventConsumerJob.class);
		default:
			break;
		}
		
		return null;
	}
	
	private <T extends EventConsumerBase> EventConsumerBase[] initArray(EventConsumerBase[] threads,Class<T> clazz) {
		for (int i = 0; i < threads.length; i++) {
			if (threads[i] == null)
				try {
					threads[i] = clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					log.error("", e);
				}
		}
		return threads;
	}
	
}
