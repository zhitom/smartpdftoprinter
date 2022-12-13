/**
 * 
 */
package com.smartsnow.smartpdftoprinter;

import com.smartsnow.smartpdftoprinter.utils.OptionUtil;

/**
 * 应用框架类,这里只定义开放给普通模块实现的通用接口
 * @author Shandy
 *
 */
public interface ModuleFrameInterface {
	
    /// 是否仅仅运行一次
    boolean getOnlyOne();
    
    /// 设置仅仅运行一次
    void setOnlyOne(boolean left);
    
    //进程退出
    void setStopFlag(boolean b);
    ////////////////////////////////////////////////////////////////
    /**
	 * 基础初始化,处理命令行选项
	 * */
	void setArgs(String[] args);
	
	/**
	 * 基础初始化,一般读取配置的基础功能类.
	 * */
	void initialize();
	
	/**
	 * 业务初始化,一些复杂性的初始化操作,和initialize的区别:两者可能不在一个进程空间初始化
	 * 当beforeLoop在子进程里边调用时,父进程可能已经退出,后续所有的接口都在子进程调用.
	 * */
	void beforeLoop();
	
	/**
	 * 循环调用
	 * */
	void loopProcess();
	
	/**
	 * 结束后的执行
	 * */
	void end();
    ////////////////////////////////////////////////////////////////
	
	/**
	 * 返回模块名
	 * */
	String getModuleName();
	
	/**
	 * 返回模块名,一般是完整的类名,用于业务日志和告警的配置.
	 * */
	String getModuleFullName();
	
	/**
	 * 返回模块实例标识信息,逻辑上的通道
	 * */
	String getModuleInstance();
	
	/**
	 * 返回模块进程实例标识信息,一个通道可以有多个进程处理
	 * */
	String getModuleSubInstance();
	
	/**
	 * 返回配置文件名的路径
	 * */
	String getConfigureFile();
	
	/**
	 * 返回运行功能名称,不一定是模块名,默认为模块名
	 * */
	String getRunName();
	
	/**
	 * 返回是否退出标记
	 * */
	boolean isStop();
	
	/**
	 * 返回选项工具对象
	 * */
	OptionUtil getOptionUtil();
}
