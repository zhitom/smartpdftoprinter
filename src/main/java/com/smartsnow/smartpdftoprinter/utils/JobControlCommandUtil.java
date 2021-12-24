package com.smartsnow.smartpdftoprinter.utils;

import java.io.File;

import com.smartsnow.smartpdftoprinter.AppConfig;

/**
 * @author Shandy
 *
 */
public class JobControlCommandUtil {
	private static final String PAUSE="cmd_pause";
	private static final String EXIT="cmd_exit";
//	private static final String CONTINUE="cmd_continue";
	
	private JobControlCommandUtil() {
	}
	
	public static boolean isActive(final AppConfig appConfig) {
		return appConfig.getCmdDir()!=null&&!appConfig.getCmdDir().isEmpty();
	}
	public static void clear(final AppConfig appConfig) {
		File cmdFile=new File(appConfig.getCmdDir()+EXIT);
		if(cmdFile.exists()) {
			cmdFile.delete();
		}
		cmdFile=new File(appConfig.getCmdDir()+PAUSE);
		if(cmdFile.exists()) {
			cmdFile.delete();
		}
	}
	public static boolean isPause(final AppConfig appConfig) {
		File cmdFile=new File(appConfig.getCmdDir()+PAUSE);
		return cmdFile.exists();
	}
	public static boolean isExit(final AppConfig appConfig) {
		File cmdFile=new File(appConfig.getCmdDir()+EXIT);
		return cmdFile.exists();
	}
//	public static boolean isContinue(final AppConfig appConfig) {
//		File cmdFile=new File(appConfig.getCmdDir()+CONTINUE);
//		return cmdFile.exists();
//	}
}
