package com.smartsnow.smartpdftoprinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单一普通模块应用的实现,需要传入另外实现ModuleFrame的应用接口
 * */
public class DefaultModuleApplication implements Application {
	private static Logger logger = LoggerFactory.getLogger(DefaultModuleApplication.class);
	public static final String MSG_START_SETARGS="START TO setArgs(args) ...";
	public static final String MSG_START_INITIALIZE="START TO initialize() ...";
	public static final String MSG_START_BEFORELOOP="START TO beforeLoop() ...";
	public static final String MSG_START_LOOPPROCESS="START TO loopProcess() ...";
	public static final String MSG_END="END TO exit ...";
	public static final String MSG_RUN_ONCE="RUN ONLY YONCE,TO END!";
	public static final String MSG_FAIL_START_SETARGS="FAILED TO setArgs(args) ...";
	public static final String MSG_FAIL_START_INITIALIZE="FAILED TO initialize() ...";
	public static final String MSG_FAIL_START_BEFORELOOP="FAILED TO beforeLoop() ...";
	public static final String MSG_FAIL_START_LOOPPROCESS="FAILED TO loopProcess() ...";
	public static final String MSG_FAIL_END="FAILED TO end() ...";
	protected ModuleFrame moduleFrame;
	protected String[] args;
	protected Logger svcLogger=logger;
	protected volatile boolean isCalledEnd=false;
	/**
	 * 单一普通模块应用的实现
	 * @param moduleFrame 另外实现ModuleFrame的应用接口
	 * @param args 命令行入参
	 * */
	public DefaultModuleApplication(ModuleFrame moduleFrame,String[] args) {
		this.moduleFrame=moduleFrame;
		this.args=args;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				end();
			}
		}));
	}
	@Override
	public void end() {
		if(!isCalledEnd) {
			svcLogger.warn(MSG_END);
			try {
				moduleFrame.setStopFlag(true);
				moduleFrame.end();
				isCalledEnd=true;
			}catch (Exception e) {
				svcLogger.warn(MSG_FAIL_END, e);
				logger.warn(MSG_FAIL_END,e);
				isCalledEnd=true;
			}
		}
	}
	@Override
	public int run() {
		svcLogger.warn(MSG_START_SETARGS);
		try {
			moduleFrame.setArgs(args);
		}catch (Exception e) {
			svcLogger.warn(MSG_FAIL_START_SETARGS, e);
			logger.error(MSG_FAIL_START_SETARGS,e);
			return -201;
		}
		svcLogger.warn(MSG_START_INITIALIZE);
		try {
			moduleFrame.initialize();
		}catch (Exception e) {
			svcLogger.warn(MSG_FAIL_START_INITIALIZE, e);
			logger.error(MSG_FAIL_START_INITIALIZE,e);
			return -201;
		}
		svcLogger.warn(MSG_START_BEFORELOOP);
		try {
			moduleFrame.beforeLoop();
		}catch (Exception e) {
			svcLogger.warn(MSG_FAIL_START_BEFORELOOP, e);
			logger.error(MSG_FAIL_START_BEFORELOOP,e);
			return -202;
		}
		
		svcLogger.warn(MSG_START_LOOPPROCESS);
		try {
			while(!moduleFrame.isStop()) {
				moduleFrame.loopProcess();
				if(moduleFrame.getOnlyOne()) {
					svcLogger.warn(MSG_RUN_ONCE);
					logger.warn(MSG_RUN_ONCE);
					break;
				}
			}
		}catch (Exception e) {
			svcLogger.warn(MSG_FAIL_START_LOOPPROCESS, e);
			logger.error(MSG_FAIL_START_LOOPPROCESS,e);
			return -203;
		}
		if(!isCalledEnd) {
			svcLogger.warn(MSG_END);
			try {
				moduleFrame.end();
				isCalledEnd=true;
			}catch (Exception e) {
				svcLogger.warn(MSG_FAIL_END, e);
				logger.warn(MSG_FAIL_END,e);
				isCalledEnd=true;
				return -204;
			}
		}		
		return 0;
	}
}
