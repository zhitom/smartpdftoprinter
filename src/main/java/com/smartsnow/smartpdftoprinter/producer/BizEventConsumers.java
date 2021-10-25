package com.smartsnow.smartpdftoprinter.producer;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.aivanlink.common.base.metrics.MetricsUtil;
import com.aivanlink.common.base.metrics.MetricsUtil.MetricsReporter;
import com.aivanlink.common.base.thread.SerialConsumersRouteGroupQueueThreadPool;
import com.smartsnow.smartpdftoprinter.AppConfig;
import com.smartsnow.smartpdftoprinter.bean.EventConsumerEnum;
import com.smartsnow.smartpdftoprinter.bean.EventTypeEnum;
import com.smartsnow.smartpdftoprinter.consumer.EventConsumerBase;
import com.smartsnow.smartpdftoprinter.consumer.EventConsumerFactory;
import com.smartsnow.smartpdftoprinter.event.in.InEventWrapper;
import com.smartsnow.smartpdftoprinter.event.in.InJobInfo;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DependsOn("springContextUtils")
public class BizEventConsumers {
	private SerialConsumersRouteGroupQueueThreadPool<String,InEventWrapper> bizConsumers = null ;
	private MetricsReporter reporter = MetricsUtil.newReporter("biz-consumer-CALLER-STAT", log.getClass(),LoggingLevel.WARN);
	@Autowired
	private EventConsumerFactory eventConsumerFactory;
	@Autowired
	private AppConfig appConfig;
	
	@PostConstruct
	public void startup() {
		EventConsumerBase[][] workHandlersAllConsumers=
				new EventConsumerBase[EventConsumerEnum.size()][];
		EventConsumerBase[] workHandlers;
		EventConsumerEnum consumerEnum = null;
		Timer timer = null;
		
		for (int i = 0; i < workHandlersAllConsumers.length; i++) {
			consumerEnum = EventConsumerEnum.values()[i];

			timer = reporter.newTimer("biz-consumer-CALLER("+consumerEnum.name()+")");
			workHandlers = eventConsumerFactory.newConsumer(consumerEnum);
			for (int j = 0; j < workHandlers.length; j++) {
				workHandlers[j].setTimer(timer);
			}
			workHandlersAllConsumers[i]=workHandlers;				
		}
		if(bizConsumers==null) {
			bizConsumers = new SerialConsumersRouteGroupQueueThreadPool<>("BizConsumers",workHandlersAllConsumers);// LiteBlockingWaitStrategy
		}
		bizConsumers.run();
		reporter.start(60, TimeUnit.SECONDS);
		log.warn("BizConsumers IS START DONE.");
	}
    public void putEvent(InJobInfo inJobInfo,EventTypeEnum eventTypeEnum) {
    	InEventWrapper rawEvent = new InEventWrapper();
    	rawEvent.reset(inJobInfo);
		rawEvent.setEventType(eventTypeEnum);
		//等待消息处理完
		while(bizConsumers.remainSize()>=appConfig.getInBatchSize()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log.error("",e);
				Thread.currentThread().interrupt();
			}
		}
		bizConsumers.publish(rawEvent.getFirstConsumerKey(), rawEvent);
    }
    
    public boolean isEmpty() {
    	return bizConsumers.remainSize()==0;
    }
    public void shutdown() {
		if (bizConsumers != null) {
			bizConsumers.shutdown();
			bizConsumers=null;
		}
	}
}
