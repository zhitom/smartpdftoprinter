package com.smartsnow.smartpdftoprinter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.aivanlink.common.frame.DefaultModuleApplication;
import com.aivanlink.common.frame.ModuleFrame;
import com.smartsnow.smartpdftoprinter.expire.ExpireScanTasksExecutor;
import com.smartsnow.smartpdftoprinter.producer.BizEventConsumers;
import com.smartsnow.smartpdftoprinter.producer.JobToBizEventProducer;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Shandy
 *
 */
@Slf4j
@SpringBootApplication
public class SmartPdfToPrinterApplication extends ModuleFrame {

	@Autowired
	private BizEventConsumers bizEventConsumers;

	@Autowired
	private JobToBizEventProducer jobToBizEventProducer;

	@Autowired
	private ExpireScanTasksExecutor expireScanTasksExecutor;
	
	@Autowired
	private AppConfig appConfig;
	
	public SmartPdfToPrinterApplication() {
		super(SmartPdfToPrinterApplication.class.getName(),"smartpdf");
	}
	public static void main(String[] args) {
		new DefaultModuleApplication(
				SpringApplication.run(SmartPdfToPrinterApplication.class,args).getBean(SmartPdfToPrinterApplication.class),
				null).run();
	}

	@Override
	public void beforeLoop() {
		try {
//			bizEventProducer.startup();
//			kafkaProducer.startup();
//			kafkaToBizEventProducer.startup();
//			thingExpireScanExecutor.startup();
		} catch (Exception e) {
			log.error("",e);
		}
	}
	@Override
	public void end() {
		//需要保证停的顺序,所以采用显式调用,而不用preDestroy
		expireScanTasksExecutor.shutdown();
		jobToBizEventProducer.shutdown();
		bizEventConsumers.shutdown();
	}

	@Override
	public void initialize() {
	}

	@Override
	public void loopProcess() {
		int total=0;
		try {
			total=jobToBizEventProducer.consume(stopFlag);
		} catch (Exception e) {
			log.error("",e);
			try {//错误睡眠
				//log.info("SLEEP({})...",50);
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				log.error("",e1);
				Thread.currentThread().interrupt();
			}
			return;
		}
		if(isOnlyOne) {
			return;
		}
		//空闲睡眠
		if(total<=0) {
			if(total==0&&appConfig.isOnceFlag()) {
				stopFlag=true;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				log.error("",e);
				Thread.currentThread().interrupt();
			}
		}	
	}
}
