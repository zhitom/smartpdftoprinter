package com.smartsnow.smartpdftoprinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsnow.smartpdftoprinter.utils.OptionUtil;

/**
 * 模块从此类继承实现,并增加下面的main方法实现主类.
 * public class YourModule extends ModuleFrame{
 *  public static void main(String[] args) {
		new DefaultModuleApplication(new YourModule()).run(args);
	}
   }
 * */
public abstract class ModuleFrame implements ModuleFrameInterface {
    private static Logger logger = LoggerFactory.getLogger(DefaultModuleApplication.class);
	protected boolean isOnlyOne=false;
	protected boolean stopFlag=false;
	
	protected String moduleName;
	protected String moduleFullName;
	
	protected String moduleInstance="1";
	protected String subChannelId;
	protected String configureFile;
	protected String runName;
	
	protected String[] args;
	protected OptionUtil option=new OptionUtil();
	
	protected Logger svcLogger=logger;
	
	/**
	 * @param moduleName 模块名,全英文,可以带数字或其他,首字母必须是字母
	 * @param moduleInstance 模块进程的实例标识信息,逻辑上的实例标识信息,可以是全数字.
	 * */
	public ModuleFrame(String moduleFullName,String moduleName) {
		this.moduleFullName=moduleFullName;
		this.moduleName=moduleName;	
	}
	
	@Override
	public boolean getOnlyOne() {
		return isOnlyOne;
	}

	@Override
	public void setOnlyOne(boolean left) {
		isOnlyOne=left;
	}

	/**
	 * 基础初始化,处理命令行选项
	 * @param svcLogger 
	 * */
	@Override
	public void setArgs(String[] args) {
		this.args=args;
		option.parseOpt(moduleName, this.args);
		moduleInstance=option.getChannelId("1");
		subChannelId=option.getSubChannelId("1");
		runName=option.getRunName(moduleName);
		configureFile=option.getConfigureFile(runName+".properties");
		isOnlyOne=option.isExecuteOnce(false);
	}
	
	@Override
	public abstract void initialize();

	@Override
	public abstract void beforeLoop();

	@Override
	public abstract void loopProcess();

	@Override
	public abstract void end();

	/**
	 * 返回模块名
	 * */
	@Override
	public String getModuleName() {
		return moduleName;
	}
	
	/**
	 * 返回模块名,一般是完整的类名,用于业务日志和告警的配置.
	 * */
	@Override
	public String getModuleFullName() {
		return moduleFullName;
	}
	
	/**
	 * 返回模块实例标识信息
	 * */
	@Override
	public String getModuleInstance() {
		return moduleInstance;
	}
	
	/**
	 * 返回模块进程实例标识信息,一个通道可以有多个进程处理
	 * */
	@Override
	public String getModuleSubInstance() {
		return subChannelId;
	}
	
	/**
	 * 返回配置文件名的路径
	 * */
	@Override
	public String getConfigureFile() {
		return configureFile;
	}
	
	/**
	 * 返回运行功能名称,不一定是模块名,默认为模块名
	 * */
	@Override
	public String getRunName() {
		return runName;
	}
	
	/**
	 * 返回是否退出标记
	 * */
	@Override
	public boolean isStop() {
		return stopFlag;
	}
	
	@Override
	public void setStopFlag(boolean b) {
		stopFlag=b;
	}
	
	/**
	 * 返回选项工具对象
	 * */
	@Override
	public OptionUtil getOptionUtil() {
		return option;
	}
}
