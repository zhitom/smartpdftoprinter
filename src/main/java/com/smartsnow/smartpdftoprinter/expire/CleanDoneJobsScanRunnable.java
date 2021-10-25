package com.smartsnow.smartpdftoprinter.expire;

import org.springframework.stereotype.Component;

import com.aivanlink.common.base.thread.SafeRunnable;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CleanDoneJobsScanRunnable implements SafeRunnable{
//	@Autowired 
//	private AppConfig appConfig;
	
	private volatile boolean isExit=false;
//	private long sleepTimeMSecs=3000;
	
	@Override
	public void run() {
		//需等待将积压的心跳消息处理完,否则可能会认为超时.
//		try {
//			Thread.sleep(60000);
//		} catch (InterruptedException e) {
//			log.error("",e);
//			Thread.currentThread().interrupt();
//		}
		while(!isExit) {
			try {
//				long currentAbsTimeMs=System.currentTimeMillis();
//				String expireYYYYMMDDHHMISS=DateUtil.toDateTimeStr(
//						DateUtil.getZonedDateTime(currentAbsTimeMs-appConfig.getJobDeleteDays()*24*3600*1000));
//				List<Long> expires = PdbRemoteCaller.getDeleteDoneJobs(expireYYYYMMDDHHMISS);
//				if(expires.isEmpty()) {
//					try {
//						Thread.sleep(sleepTimeMSecs);
//					} catch (InterruptedException e) {
//						log.error("",e);
//						Thread.currentThread().interrupt();
//					}
//					continue;
//				}
//				for(Long v:expires) {
//					PdbRemoteCaller.deleteDoneJobById(v.longValue());
//					PdbRemoteCaller.deleteDoneJobChildByJobId(v.longValue());
//				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					log.error("",e1);
					Thread.currentThread().interrupt();
				}
			}catch (Exception e) {
				log.error("",e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					log.error("",e1);
					Thread.currentThread().interrupt();
				}
			}
			
		}
	}
	
	@Override
	public void setExit(boolean isToExit) {
		isExit=isToExit;
	}
	
}