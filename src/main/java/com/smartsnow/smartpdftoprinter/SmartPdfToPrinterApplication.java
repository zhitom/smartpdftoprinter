package com.smartsnow.smartpdftoprinter;


import java.util.Arrays;
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
		if(args.length>0&&args[0].equals("setup")) {
			SmartPdfToPrinterSetupApplication.setup(Arrays.asList(args).stream().filter(v->!v.equals("setup")).toArray(String[]::new));
			System.exit(0);
		}
		new DefaultModuleApplication(
				SpringApplication.run(SmartPdfToPrinterApplication.class,args).getBean(SmartPdfToPrinterApplication.class),
				null).run();
	}
	public boolean isExit() {
		return super.isStop();
	}
	public void exit() {
		super.setStopFlag(true);
	}
	@Override
	public void beforeLoop() {
//		if(option.getValue("p")!=null) {
//			appConfig.setSetupModeFlag(true);
//			setup();
//			stopFlag=true;
//		}
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
		//????????????????????????,????????????????????????,?????????preDestroy
		expireScanTasksExecutor.shutdown();
		jobToBizEventProducer.shutdown();
		bizEventConsumers.shutdown();
	}

	@Override
	public void initialize() {
//		getOptionUtil().addOptWithNoArg("p", "printer", "???????????????", true);
	}

	@Override
	public void loopProcess() {
		int total=0;
		try {
			total=jobToBizEventProducer.consume(stopFlag);
		} catch (Exception e) {
			log.error("",e);
			try {//????????????
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
		//????????????
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
