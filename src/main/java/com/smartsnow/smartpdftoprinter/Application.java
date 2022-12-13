/**
 * 
 */
package com.smartsnow.smartpdftoprinter;

/**
 * 应用的实现接口
 * 
 * @author Shandy
 *
 */
public interface Application {
	/**
	 * 执行接口,args为入参信息
	 * */
	int run();
	/**应用退出*/
	void end();
}
